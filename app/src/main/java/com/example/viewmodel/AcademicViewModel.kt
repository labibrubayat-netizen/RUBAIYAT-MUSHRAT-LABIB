package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AcademicRepository
import com.example.model.AcademicSettings
import com.example.model.ClassItem
import com.example.model.DayOverride
import com.example.model.DayResolution
import com.example.model.DaySchedule
import com.example.model.DayStatus
import com.example.model.QuickPeriodType
import com.example.util.ICalendarExporter
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class TodayUiState(
  val today: LocalDate = LocalDate.now(),
  val todayResolution: DayResolution? = null,
  val inProgressClass: ClassItem? = null,
  val nextClass: ClassItem? = null,
  val secondsUntilNextClass: Long? = null,
  val secondsLeftInClass: Long? = null,
  val totalClassDurationSeconds: Long? = null,
  val countdownText: String = "",
  val upcomingNonOffDays: List<DayResolution> = emptyList()
)

data class CalendarUiState(
  val currentYearMonth: YearMonth = YearMonth.now(),
  val selectedDate: LocalDate? = null,
  val editingResolution: DayResolution? = null,
  val isEditorOpen: Boolean = false,
  val isQuickPeriodDialogOpen: Boolean = false,
  val semesterMonths: List<YearMonth> = emptyList()
)

data class AcademicUiState(
  val settings: AcademicSettings = AcademicSettings(),
  val weeklyRoutine: List<DaySchedule> = emptyList(),
  val overrides: Map<String, DayOverride> = emptyMap(),
  val todayState: TodayUiState = TodayUiState(),
  val calendarState: CalendarUiState = CalendarUiState(),
  val activeScreen: AppScreen = AppScreen.TODAY,
  val notificationMessage: String? = null
)

enum class AppScreen(val title: String, val icon: String) {
  TODAY("Today", "📅"),
  CALENDAR("Calendar", "🗓️"),
  SETTINGS("Settings", "⚙️"),
  ABOUT("About", "ℹ️")
}

class AcademicViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = AcademicRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(AcademicUiState())
  val uiState: StateFlow<AcademicUiState> = _uiState.asStateFlow()

  init {
    loadAllData()
    startClockTicker()
  }

  fun setScreen(screen: AppScreen) {
    _uiState.update { it.copy(activeScreen = screen) }
  }

  fun loadAllData() {
    val settings = repository.getSettings()
    val routine = repository.getWeeklyRoutine()
    val overrides = repository.getOverrides()

    val semesterMonths = computeSemesterMonths(settings)
    val now = LocalDate.now()
    val currentYm = YearMonth.of(now.year, now.month)
    val initialYm = if (semesterMonths.contains(currentYm)) currentYm else semesterMonths.firstOrNull() ?: currentYm

    _uiState.update { current ->
      current.copy(
        settings = settings,
        weeklyRoutine = routine,
        overrides = overrides,
        calendarState = current.calendarState.copy(
          currentYearMonth = initialYm,
          semesterMonths = semesterMonths
        )
      )
    }

    refreshTodayState()
  }

  private fun computeSemesterMonths(settings: AcademicSettings): List<YearMonth> {
    val list = mutableListOf<YearMonth>()
    try {
      var start = YearMonth.of(settings.startYear, settings.startMonth)
      val end = YearMonth.of(settings.endYear, settings.endMonth)
      var count = 0
      while (!start.isAfter(end) && count < 24) {
        list.add(start)
        start = start.plusMonths(1)
        count++
      }
    } catch (e: Exception) {
      val now = YearMonth.now()
      for (i in 0..5) {
        list.add(now.plusMonths(i.toLong()))
      }
    }
    return if (list.isNotEmpty()) list else listOf(YearMonth.now())
  }

  fun resolveDay(date: LocalDate): DayResolution {
    val dateKey = date.toString()
    val override = _uiState.value.overrides[dateKey]

    if (override != null) {
      return DayResolution(
        date = date,
        dayOfWeek = date.dayOfWeek,
        status = override.status,
        classes = override.classes,
        note = override.note,
        isOverridden = true
      )
    }

    // Default to weekly schedule
    val dayOfWeekVal = date.dayOfWeek.value // 1..7 (Mon..Sun)
    val daySchedule = _uiState.value.weeklyRoutine.firstOrNull { it.dayOfWeek == dayOfWeekVal }

    return if (daySchedule != null && daySchedule.isClassDay) {
      DayResolution(
        date = date,
        dayOfWeek = date.dayOfWeek,
        status = DayStatus.CLASS_DAY,
        classes = daySchedule.classes,
        note = "",
        isOverridden = false
      )
    } else {
      DayResolution(
        date = date,
        dayOfWeek = date.dayOfWeek,
        status = DayStatus.OFF_DAY,
        classes = emptyList(),
        note = "",
        isOverridden = false
      )
    }
  }

  private fun computeUpcomingDays(today: LocalDate): List<DayResolution> {
    val upcoming = mutableListOf<DayResolution>()
    var scanDate = today.plusDays(1)
    var daysChecked = 0
    while (upcoming.size < 5 && daysChecked < 30) {
      val res = resolveDay(scanDate)
      if (res.status != DayStatus.OFF_DAY) {
        upcoming.add(res)
      }
      scanDate = scanDate.plusDays(1)
      daysChecked++
    }
    return upcoming
  }

  fun refreshTodayState() {
    val today = LocalDate.now()
    val todayRes = resolveDay(today)
    val upcoming = computeUpcomingDays(today)
    val nowTime = LocalTime.now()

    var inProgress: ClassItem? = null
    var next: ClassItem? = null
    var secondsUntilNext: Long? = null
    var secondsLeftInCurrent: Long? = null
    var totalDurationSecs: Long? = null
    var countdown = ""

    if (todayRes.status == DayStatus.CLASS_DAY || todayRes.status == DayStatus.EXAM) {
      val sortedClasses = todayRes.classes.sortedBy { it.startTime }

      for (c in sortedClasses) {
        val start = parseTimeOrNull(c.startTime) ?: continue
        val end = parseTimeOrNull(c.endTime) ?: start.plusMinutes(90)

        if (!nowTime.isBefore(start) && !nowTime.isAfter(end)) {
          inProgress = c
          val secsLeft = ChronoUnit.SECONDS.between(nowTime, end).coerceAtLeast(0)
          val totalSecs = ChronoUnit.SECONDS.between(start, end).coerceAtLeast(1)
          secondsLeftInCurrent = secsLeft
          totalDurationSecs = totalSecs
          val minsLeft = secsLeft / 60
          countdown = if (minsLeft > 0) "$minsLeft min left" else "Ending now"
          break
        } else if (nowTime.isBefore(start)) {
          if (next == null) {
            next = c
            val secsUntil = ChronoUnit.SECONDS.between(nowTime, start).coerceAtLeast(0)
            secondsUntilNext = secsUntil
            val totalSecs = ChronoUnit.SECONDS.between(start, end).coerceAtLeast(1)
            totalDurationSecs = totalSecs

            val hours = secsUntil / 3600
            val remainMins = (secsUntil % 3600) / 60
            val remainSecs = secsUntil % 60

            countdown = if (hours > 0) {
              "Starts in ${hours}h ${remainMins}m"
            } else if (remainMins > 0) {
              "Starts in ${remainMins}m ${remainSecs}s"
            } else {
              "Starts in ${remainSecs}s"
            }
          }
        }
      }

      if (inProgress == null && next == null && sortedClasses.isNotEmpty()) {
        countdown = "All done for today! 🎉"
      }
    }

    _uiState.update { current ->
      current.copy(
        todayState = TodayUiState(
          today = today,
          todayResolution = todayRes,
          inProgressClass = inProgress,
          nextClass = next,
          secondsUntilNextClass = secondsUntilNext,
          secondsLeftInClass = secondsLeftInCurrent,
          totalClassDurationSeconds = totalDurationSecs,
          countdownText = countdown,
          upcomingNonOffDays = upcoming
        )
      )
    }
  }

  private fun updateCountdownTick() {
    val currentTodayState = _uiState.value.todayState
    val todayRes = currentTodayState.todayResolution ?: return
    if (todayRes.status != DayStatus.CLASS_DAY && todayRes.status != DayStatus.EXAM) {
      return
    }
    if (todayRes.classes.isEmpty()) {
      return
    }

    val nowTime = LocalTime.now()
    var inProgress: ClassItem? = null
    var next: ClassItem? = null
    var secondsUntilNext: Long? = null
    var secondsLeftInCurrent: Long? = null
    var totalDurationSecs: Long? = null
    var countdown = ""

    val sortedClasses = todayRes.classes.sortedBy { it.startTime }

    for (c in sortedClasses) {
      val start = parseTimeOrNull(c.startTime) ?: continue
      val end = parseTimeOrNull(c.endTime) ?: start.plusMinutes(90)

      if (!nowTime.isBefore(start) && !nowTime.isAfter(end)) {
        inProgress = c
        val secsLeft = ChronoUnit.SECONDS.between(nowTime, end).coerceAtLeast(0)
        val totalSecs = ChronoUnit.SECONDS.between(start, end).coerceAtLeast(1)
        secondsLeftInCurrent = secsLeft
        totalDurationSecs = totalSecs
        val minsLeft = secsLeft / 60
        countdown = if (minsLeft > 0) "$minsLeft min left" else "Ending now"
        break
      } else if (nowTime.isBefore(start)) {
        if (next == null) {
          next = c
          val secsUntil = ChronoUnit.SECONDS.between(nowTime, start).coerceAtLeast(0)
          secondsUntilNext = secsUntil
          val totalSecs = ChronoUnit.SECONDS.between(start, end).coerceAtLeast(1)
          totalDurationSecs = totalSecs

          val hours = secsUntil / 3600
          val remainMins = (secsUntil % 3600) / 60
          val remainSecs = secsUntil % 60

          countdown = if (hours > 0) {
            "Starts in ${hours}h ${remainMins}m"
          } else if (remainMins > 0) {
            "Starts in ${remainMins}m ${remainSecs}s"
          } else {
            "Starts in ${remainSecs}s"
          }
        }
      }
    }

    if (inProgress == null && next == null && sortedClasses.isNotEmpty()) {
      countdown = "All done for today! 🎉"
    }

    if (currentTodayState.countdownText != countdown ||
        currentTodayState.secondsUntilNextClass != secondsUntilNext ||
        currentTodayState.secondsLeftInClass != secondsLeftInCurrent ||
        currentTodayState.inProgressClass?.id != inProgress?.id ||
        currentTodayState.nextClass?.id != next?.id) {
      _uiState.update { current ->
        current.copy(
          todayState = current.todayState.copy(
            inProgressClass = inProgress,
            nextClass = next,
            secondsUntilNextClass = secondsUntilNext,
            secondsLeftInClass = secondsLeftInCurrent,
            totalClassDurationSeconds = totalDurationSecs,
            countdownText = countdown
          )
        )
      }
    }
  }

  private fun parseTimeOrNull(timeStr: String): LocalTime? {
    return try {
      val parts = timeStr.trim().split(":")
      if (parts.size >= 2) {
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
      } else null
    } catch (e: Exception) {
      null
    }
  }

  private fun startClockTicker() {
    viewModelScope.launch(Dispatchers.Default) {
      // 1200ms initial delay allows Android WindowManager and SurfaceSyncGroup
      // to complete initial surface transaction cleanly without main-thread contention.
      delay(1200)
      while (isActive) {
        val today = LocalDate.now()
        val currentToday = _uiState.value.todayState.today
        if (today != currentToday) {
          withContext(Dispatchers.Main) {
            refreshTodayState()
          }
        } else {
          updateCountdownTick()
        }
        delay(1000)
      }
    }
  }

  // CALENDAR ACTIONS
  fun selectMonth(month: YearMonth) {
    _uiState.update { it.copy(calendarState = it.calendarState.copy(currentYearMonth = month)) }
  }

  fun openDateEditor(date: LocalDate) {
    val resolution = resolveDay(date)
    _uiState.update {
      it.copy(
        calendarState = it.calendarState.copy(
          selectedDate = date,
          editingResolution = resolution,
          isEditorOpen = true
        )
      )
    }
  }

  fun closeDateEditor() {
    _uiState.update {
      it.copy(calendarState = it.calendarState.copy(isEditorOpen = false, editingResolution = null))
    }
  }

  fun saveDateOverride(
    date: LocalDate,
    status: DayStatus,
    classes: List<ClassItem>,
    note: String
  ) {
    val override = DayOverride(
      date = date.toString(),
      status = status,
      classes = classes,
      note = note
    )
    repository.saveOverride(override)
    val updatedOverrides = repository.getOverrides()
    _uiState.update { current ->
      current.copy(
        overrides = updatedOverrides,
        calendarState = current.calendarState.copy(isEditorOpen = false, editingResolution = null)
      )
    }
    refreshTodayState()
  }

  fun resetDateOverride(date: LocalDate) {
    repository.removeOverride(date.toString())
    val updatedOverrides = repository.getOverrides()
    _uiState.update { current ->
      current.copy(
        overrides = updatedOverrides,
        calendarState = current.calendarState.copy(isEditorOpen = false, editingResolution = null)
      )
    }
    refreshTodayState()
  }

  fun openQuickPeriodDialog() {
    _uiState.update { it.copy(calendarState = it.calendarState.copy(isQuickPeriodDialogOpen = true)) }
  }

  fun closeQuickPeriodDialog() {
    _uiState.update { it.copy(calendarState = it.calendarState.copy(isQuickPeriodDialogOpen = false)) }
  }

  fun applyQuickPeriod(
    periodType: QuickPeriodType,
    startDate: LocalDate,
    endDate: LocalDate,
    note: String,
    classes: List<ClassItem>
  ) {
    val overridesToSave = mutableListOf<DayOverride>()
    var curr = startDate
    val end = if (endDate.isBefore(startDate)) startDate else endDate

    while (!curr.isAfter(end)) {
      overridesToSave.add(
        DayOverride(
          date = curr.toString(),
          status = periodType.defaultStatus,
          classes = classes,
          note = if (note.isNotBlank()) note else periodType.displayName
        )
      )
      curr = curr.plusDays(1)
    }

    repository.saveBatchOverrides(overridesToSave)
    val updatedOverrides = repository.getOverrides()
    _uiState.update { current ->
      current.copy(
        overrides = updatedOverrides,
        calendarState = current.calendarState.copy(isQuickPeriodDialogOpen = false),
        notificationMessage = "Applied ${periodType.displayName} across ${overridesToSave.size} days! ✨"
      )
    }
    refreshTodayState()
  }

  // SETTINGS & ROUTINE ACTIONS
  fun updateAcademicSettings(settings: AcademicSettings) {
    repository.saveSettings(settings)
    val semesterMonths = computeSemesterMonths(settings)
    _uiState.update {
      it.copy(
        settings = settings,
        calendarState = it.calendarState.copy(semesterMonths = semesterMonths)
      )
    }
    refreshTodayState()
  }

  fun updateWeeklyRoutine(routine: List<DaySchedule>) {
    repository.saveWeeklyRoutine(routine)
    _uiState.update { it.copy(weeklyRoutine = routine) }
    refreshTodayState()
  }

  fun updateDayInRoutine(daySchedule: DaySchedule) {
    val current = _uiState.value.weeklyRoutine.toMutableList()
    val idx = current.indexOfFirst { it.dayOfWeek == daySchedule.dayOfWeek }
    if (idx != -1) {
      current[idx] = daySchedule
    } else {
      current.add(daySchedule)
    }
    updateWeeklyRoutine(current)
  }

  fun copyRoutineToOtherDays(sourceDay: DaySchedule, targetDays: List<Int>) {
    val current = _uiState.value.weeklyRoutine.toMutableList()
    for (targetDay in targetDays) {
      val idx = current.indexOfFirst { it.dayOfWeek == targetDay }
      val copiedClasses = sourceDay.classes.map { it.copy(id = java.util.UUID.randomUUID().toString()) }
      val updated = DaySchedule(dayOfWeek = targetDay, isClassDay = sourceDay.isClassDay, classes = copiedClasses)
      if (idx != -1) {
        current[idx] = updated
      } else {
        current.add(updated)
      }
    }
    updateWeeklyRoutine(current)
  }

  fun loadDemoSchedule() {
    repository.resetToSampleData()
    loadAllData()
    _uiState.update { it.copy(notificationMessage = "Loaded demo university schedule! 🎓") }
  }

  fun sendTestNotification() {
    val success = NotificationHelper.sendTestNotification(getApplication())
    _uiState.update {
      it.copy(
        notificationMessage = if (success) "Test notification sent! 🔔 Check your notifications bar." else "Could not post notification. Please grant permission."
      )
    }
  }

  fun triggerTomorrowReminderNow() {
    val tomorrow = LocalDate.now().plusDays(1)
    val res = resolveDay(tomorrow)
    val success = NotificationHelper.sendTomorrowScheduleNotification(
      getApplication(),
      res.status,
      res.classes,
      res.note
    )
    _uiState.update {
      it.copy(
        notificationMessage = if (success) "Tomorrow's summary sent! 📅" else "Failed to send notification."
      )
    }
  }

  fun exportICalendar(context: Context) {
    val (success, message) = ICalendarExporter.exportAndShareCalendar(
      context = context,
      settings = _uiState.value.settings,
      resolveDay = { resolveDay(it) }
    )
    _uiState.update { it.copy(notificationMessage = message) }
  }

  fun clearNotificationMessage() {
    _uiState.update { it.copy(notificationMessage = null) }
  }
}
