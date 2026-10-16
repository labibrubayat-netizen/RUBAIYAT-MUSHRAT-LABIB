package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AcademicSettings
import com.example.model.ClassItem
import com.example.model.DayOverride
import com.example.model.DaySchedule
import com.example.model.DayStatus
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AcademicRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("academic_calendar_prefs", Context.MODE_PRIVATE)

  // In-memory cache for fast, non-blocking UI access
  private var cachedSettings: AcademicSettings? = null
  private var cachedRoutine: List<DaySchedule>? = null
  private var cachedOverrides: Map<String, DayOverride>? = null

  companion object {
    private const val KEY_SETTINGS = "academic_settings"
    private const val KEY_ROUTINE = "weekly_routine"
    private const val KEY_OVERRIDES = "date_overrides"
    private const val KEY_INITIALIZED = "is_initialized_v1"
  }

  init {
    if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
      initializeWithDefaultSampleData()
      prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
    }
  }

  @Synchronized
  fun getSettings(): AcademicSettings {
    cachedSettings?.let { return it }
    val loaded = loadSettingsFromPrefs()
    cachedSettings = loaded
    return loaded
  }

  private fun loadSettingsFromPrefs(): AcademicSettings {
    val raw = prefs.getString(KEY_SETTINGS, null) ?: return defaultSettings()
    return try {
      val json = JSONObject(raw)
      AcademicSettings(
        department = json.optString("department", "Computer Science & Engineering"),
        semesterName = json.optString("semesterName", "Fall 2026"),
        term = json.optString("term", "Term 1"),
        startYear = json.optInt("startYear", 2026),
        startMonth = json.optInt("startMonth", 9),
        endYear = json.optInt("endYear", 2027),
        endMonth = json.optInt("endMonth", 1),
        reminderEnabled = json.optBoolean("reminderEnabled", false),
        reminderHour = json.optInt("reminderHour", 20),
        reminderMinute = json.optInt("reminderMinute", 0)
      )
    } catch (e: Exception) {
      defaultSettings()
    }
  }

  @Synchronized
  fun saveSettings(settings: AcademicSettings) {
    cachedSettings = settings
    val json = JSONObject().apply {
      put("department", settings.department)
      put("semesterName", settings.semesterName)
      put("term", settings.term)
      put("startYear", settings.startYear)
      put("startMonth", settings.startMonth)
      put("endYear", settings.endYear)
      put("endMonth", settings.endMonth)
      put("reminderEnabled", settings.reminderEnabled)
      put("reminderHour", settings.reminderHour)
      put("reminderMinute", settings.reminderMinute)
    }
    prefs.edit().putString(KEY_SETTINGS, json.toString()).apply()
  }

  @Synchronized
  fun getWeeklyRoutine(): List<DaySchedule> {
    cachedRoutine?.let { return it }
    val loaded = loadWeeklyRoutineFromPrefs()
    cachedRoutine = loaded
    return loaded
  }

  private fun loadWeeklyRoutineFromPrefs(): List<DaySchedule> {
    val raw = prefs.getString(KEY_ROUTINE, null) ?: return defaultRoutine()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<DaySchedule>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val dayOfWeek = obj.getInt("dayOfWeek")
        val isClassDay = obj.optBoolean("isClassDay", true)
        val classesArray = obj.optJSONArray("classes") ?: JSONArray()
        val classes = mutableListOf<ClassItem>()
        for (j in 0 until classesArray.length()) {
          val cObj = classesArray.getJSONObject(j)
          classes.add(
            ClassItem(
              id = cObj.optString("id", UUID.randomUUID().toString()),
              subject = cObj.optString("subject", ""),
              startTime = cObj.optString("startTime", "09:00"),
              endTime = cObj.optString("endTime", "10:30"),
              room = cObj.optString("room", ""),
              instructor = cObj.optString("instructor", "")
            )
          )
        }
        list.add(DaySchedule(dayOfWeek, isClassDay, classes))
      }
      if (list.size == 7) list else defaultRoutine()
    } catch (e: Exception) {
      defaultRoutine()
    }
  }

  @Synchronized
  fun saveWeeklyRoutine(routine: List<DaySchedule>) {
    cachedRoutine = routine
    val array = JSONArray()
    for (day in routine) {
      val dayObj = JSONObject().apply {
        put("dayOfWeek", day.dayOfWeek)
        put("isClassDay", day.isClassDay)
        val cArr = JSONArray()
        for (c in day.classes) {
          cArr.put(
            JSONObject().apply {
              put("id", c.id)
              put("subject", c.subject)
              put("startTime", c.startTime)
              put("endTime", c.endTime)
              put("room", c.room)
              put("instructor", c.instructor)
            }
          )
        }
        put("classes", cArr)
      }
      array.put(dayObj)
    }
    prefs.edit().putString(KEY_ROUTINE, array.toString()).apply()
  }

  @Synchronized
  fun getOverrides(): Map<String, DayOverride> {
    cachedOverrides?.let { return it }
    val loaded = loadOverridesFromPrefs()
    cachedOverrides = loaded
    return loaded
  }

  private fun loadOverridesFromPrefs(): Map<String, DayOverride> {
    val raw = prefs.getString(KEY_OVERRIDES, null) ?: return emptyMap()
    return try {
      val map = mutableMapOf<String, DayOverride>()
      val obj = JSONObject(raw)
      val keys = obj.keys()
      while (keys.hasNext()) {
        val dateKey = keys.next()
        val itemObj = obj.getJSONObject(dateKey)
        val statusStr = itemObj.optString("status", DayStatus.OFF_DAY.name)
        val status = try {
          DayStatus.valueOf(statusStr)
        } catch (e: Exception) {
          DayStatus.OFF_DAY
        }
        val note = itemObj.optString("note", "")
        val classesArr = itemObj.optJSONArray("classes") ?: JSONArray()
        val classes = mutableListOf<ClassItem>()
        for (i in 0 until classesArr.length()) {
          val cObj = classesArr.getJSONObject(i)
          classes.add(
            ClassItem(
              id = cObj.optString("id", UUID.randomUUID().toString()),
              subject = cObj.optString("subject", ""),
              startTime = cObj.optString("startTime", "09:00"),
              endTime = cObj.optString("endTime", "10:30"),
              room = cObj.optString("room", ""),
              instructor = cObj.optString("instructor", "")
            )
          )
        }
        map[dateKey] = DayOverride(dateKey, status, classes, note)
      }
      map
    } catch (e: Exception) {
      emptyMap()
    }
  }

  @Synchronized
  fun saveOverride(override: DayOverride) {
    val current = getOverrides().toMutableMap()
    current[override.date] = override
    saveAllOverrides(current)
  }

  @Synchronized
  fun removeOverride(date: String) {
    val current = getOverrides().toMutableMap()
    current.remove(date)
    saveAllOverrides(current)
  }

  @Synchronized
  fun saveBatchOverrides(overrides: List<DayOverride>) {
    val current = getOverrides().toMutableMap()
    for (o in overrides) {
      current[o.date] = o
    }
    saveAllOverrides(current)
  }

  private fun saveAllOverrides(map: Map<String, DayOverride>) {
    cachedOverrides = map
    val obj = JSONObject()
    for ((key, o) in map) {
      val itemObj = JSONObject().apply {
        put("status", o.status.name)
        put("note", o.note)
        val cArr = JSONArray()
        for (c in o.classes) {
          cArr.put(
            JSONObject().apply {
              put("id", c.id)
              put("subject", c.subject)
              put("startTime", c.startTime)
              put("endTime", c.endTime)
              put("room", c.room)
              put("instructor", c.instructor)
            }
          )
        }
        put("classes", cArr)
      }
      obj.put(key, itemObj)
    }
    prefs.edit().putString(KEY_OVERRIDES, obj.toString()).apply()
  }

  @Synchronized
  fun resetToSampleData() {
    cachedSettings = null
    cachedRoutine = null
    cachedOverrides = null
    initializeWithDefaultSampleData()
  }

  private fun initializeWithDefaultSampleData() {
    saveSettings(defaultSettings())
    saveWeeklyRoutine(defaultRoutine())

    val sampleOverrides = mapOf(
      "2026-10-22" to DayOverride(
        date = "2026-10-22",
        status = DayStatus.EXAM,
        classes = listOf(
          ClassItem(subject = "Midterm: Data Structures & Algorithms", startTime = "10:00", endTime = "12:00", room = "Exam Hall 1"),
          ClassItem(subject = "Midterm: Linear Algebra", startTime = "14:00", endTime = "16:00", room = "Exam Hall 2")
        ),
        note = "Midterm Exam Day 1. Bring student ID and blue pens."
      ),
      "2026-10-23" to DayOverride(
        date = "2026-10-23",
        status = DayStatus.EXAM,
        classes = listOf(
          ClassItem(subject = "Midterm: Operating Systems", startTime = "10:00", endTime = "12:00", room = "Exam Hall 1")
        ),
        note = "Covers Process scheduling & virtual memory."
      ),
      "2026-10-30" to DayOverride(
        date = "2026-10-30",
        status = DayStatus.HOLIDAY,
        classes = emptyList(),
        note = "Autumn Campus Cultural Festival - No Classes!"
      )
    )
    saveAllOverrides(sampleOverrides)
  }

  private fun defaultSettings(): AcademicSettings {
    return AcademicSettings(
      department = "Computer Science & Engineering",
      semesterName = "Fall 2026",
      term = "Term 1",
      startYear = 2026,
      startMonth = 9,
      endYear = 2027,
      endMonth = 1,
      reminderEnabled = true,
      reminderHour = 20,
      reminderMinute = 0
    )
  }

  private fun defaultRoutine(): List<DaySchedule> {
    return listOf(
      DaySchedule(
        dayOfWeek = 1,
        isClassDay = true,
        classes = listOf(
          ClassItem(subject = "Data Structures & Algorithms", startTime = "09:00", endTime = "10:30", room = "Lab 301"),
          ClassItem(subject = "Computer Networks", startTime = "11:00", endTime = "12:30", room = "Hall B"),
          ClassItem(subject = "Linear Algebra", startTime = "14:00", endTime = "15:30", room = "Hall A")
        )
      ),
      DaySchedule(
        dayOfWeek = 2,
        isClassDay = true,
        classes = listOf(
          ClassItem(subject = "Operating Systems", startTime = "10:00", endTime = "11:30", room = "Lecture Hall 4"),
          ClassItem(subject = "Database Management Systems", startTime = "13:00", endTime = "14:30", room = "Lab 204")
        )
      ),
      DaySchedule(
        dayOfWeek = 3,
        isClassDay = true,
        classes = listOf(
          ClassItem(subject = "Data Structures & Algorithms", startTime = "09:00", endTime = "10:30", room = "Lab 301"),
          ClassItem(subject = "Web Development Lab", startTime = "11:00", endTime = "13:00", room = "Computer Center"),
          ClassItem(subject = "Artificial Intelligence", startTime = "14:30", endTime = "16:00", room = "Hall C")
        )
      ),
      DaySchedule(
        dayOfWeek = 4,
        isClassDay = true,
        classes = listOf(
          ClassItem(subject = "Computer Networks Lab", startTime = "09:30", endTime = "11:30", room = "Lab 102"),
          ClassItem(subject = "Operating Systems", startTime = "13:00", endTime = "14:30", room = "Lecture Hall 4")
        )
      ),
      DaySchedule(
        dayOfWeek = 5,
        isClassDay = true,
        classes = listOf(
          ClassItem(subject = "Software Engineering Seminar", startTime = "10:00", endTime = "11:30", room = "Auditorium"),
          ClassItem(subject = "Project Mentorship", startTime = "14:00", endTime = "15:30", room = "Faculty Room 12")
        )
      ),
      DaySchedule(
        dayOfWeek = 6,
        isClassDay = false,
        classes = emptyList()
      ),
      DaySchedule(
        dayOfWeek = 7,
        isClassDay = false,
        classes = emptyList()
      )
    )
  }
}
