package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClassItem
import com.example.model.DayResolution
import com.example.model.DayStatus
import com.example.model.QuickPeriodType
import com.example.ui.components.PillButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.VividGradient
import com.example.ui.theme.ClassGreen
import com.example.ui.theme.ClassGreenBg
import com.example.ui.theme.ClassGreenText
import com.example.ui.theme.ExamAmber
import com.example.ui.theme.ExamAmberBg
import com.example.ui.theme.ExamAmberText
import com.example.ui.theme.HolidayRed
import com.example.ui.theme.HolidayRedBg
import com.example.ui.theme.HolidayRedText
import com.example.ui.theme.OffGray
import com.example.ui.theme.OffGrayBg
import com.example.ui.theme.OffGrayText
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.CalendarUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
  calendarState: CalendarUiState,
  onSelectMonth: (YearMonth) -> Unit,
  onDateClick: (LocalDate) -> Unit,
  onCloseEditor: () -> Unit,
  onSaveOverride: (LocalDate, DayStatus, List<ClassItem>, String) -> Unit,
  onResetOverride: (LocalDate) -> Unit,
  onOpenQuickPeriod: () -> Unit,
  onCloseQuickPeriod: () -> Unit,
  onApplyQuickPeriod: (QuickPeriodType, LocalDate, LocalDate, String, List<ClassItem>) -> Unit,
  onExportICalendar: () -> Unit,
  resolveDay: (LocalDate) -> DayResolution,
  modifier: Modifier = Modifier
) {
  val currentYm = calendarState.currentYearMonth
  val today = remember { LocalDate.now() }

  // Days in month calculation
  val daysInMonth = remember(currentYm) {
    val total = currentYm.lengthOfMonth()
    (1..total).map { currentYm.atDay(it) }
  }

  val firstDayOfWeek = remember(currentYm) {
    // 1 = Monday ... 7 = Sunday
    currentYm.atDay(1).dayOfWeek.value
  }

  // Count month stats
  val monthResolutions = remember(currentYm, daysInMonth) {
    daysInMonth.map { resolveDay(it) }
  }
  val classDaysCount = monthResolutions.count { it.status == DayStatus.CLASS_DAY }
  val examDaysCount = monthResolutions.count { it.status == DayStatus.EXAM }
  val holidayDaysCount = monthResolutions.count { it.status == DayStatus.HOLIDAY }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Quick Add Period Banner Button
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .clickable { onOpenQuickPeriod() }
          .testTag("quick_add_period_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(VividGradient),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Quick Add Period Tool",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Apply Midterm, Final, or Holiday across date ranges",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onExportICalendar() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("export_ics_button")
            ) {
              Text(
                text = "🗓️ .ics",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(VioletPrimary)
                .clickable { onOpenQuickPeriod() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "+ Period",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        }
      }
    }

    // Month Selection Tabs
    item {
      val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("month_tabs_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(calendarState.semesterMonths) { ym ->
          val isSelected = ym == currentYm
          val text = ym.format(monthFormatter)

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(50))
              .then(
                if (isSelected) Modifier.background(VividGradient)
                else Modifier
                  .background(MaterialTheme.colorScheme.surface)
                  .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(50))
              )
              .clickable { onSelectMonth(ym) }
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("month_tab_${ym.year}_${ym.monthValue}")
          ) {
            Text(
              text = text,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            )
          }
        }
      }
    }

    // Stats Bar for Selected Month
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatPill(
          label = "$classDaysCount Classes",
          color = ClassGreen,
          bgColor = ClassGreenBg,
          modifier = Modifier.weight(1f)
        )
        StatPill(
          label = "$examDaysCount Exams",
          color = ExamAmber,
          bgColor = ExamAmberBg,
          modifier = Modifier.weight(1f)
        )
        StatPill(
          label = "$holidayDaysCount Holidays",
          color = HolidayRed,
          bgColor = HolidayRedBg,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Color Legend
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LegendDot(color = ClassGreen, label = "Class")
        LegendDot(color = OffGray, label = "Off")
        LegendDot(color = HolidayRed, label = "Holiday")
        LegendDot(color = ExamAmber, label = "Exam")
      }
    }

    // Calendar Grid Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("calendar_grid_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          // Weekday header
          val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            weekdays.forEach { dayName ->
              Text(
                text = dayName,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }

          // Grid rows
          val totalLeadingEmpty = firstDayOfWeek - 1 // 1=Mon has 0 empty
          val totalCells = totalLeadingEmpty + daysInMonth.size
          val rowCount = (totalCells + 6) / 7

          for (rowIndex in 0 until rowCount) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              for (colIndex in 0 until 7) {
                val cellIndex = rowIndex * 7 + colIndex
                val dayOffset = cellIndex - totalLeadingEmpty

                if (dayOffset in daysInMonth.indices) {
                  val date = daysInMonth[dayOffset]
                  val res = resolveDay(date)
                  val isToday = date == today

                  DayGridCell(
                    date = date,
                    resolution = res,
                    isToday = isToday,
                    onClick = { onDateClick(date) },
                    modifier = Modifier.weight(1f)
                  )
                } else {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "💡 Tap any date to customize status, exams, notes, or rescheduled classes.",
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 16.dp)
      )
    }
  }

  // Date Editor Bottom Sheet
  if (calendarState.isEditorOpen && calendarState.selectedDate != null && calendarState.editingResolution != null) {
    DateEditorBottomSheet(
      resolution = calendarState.editingResolution,
      onClose = onCloseEditor,
      onSave = { status, classes, note ->
        onSaveOverride(calendarState.selectedDate, status, classes, note)
      },
      onReset = {
        onResetOverride(calendarState.selectedDate)
      }
    )
  }

  // Quick Add Period Dialog
  if (calendarState.isQuickPeriodDialogOpen) {
    QuickAddPeriodDialog(
      initialDate = calendarState.selectedDate ?: today,
      onDismiss = onCloseQuickPeriod,
      onApply = onApplyQuickPeriod
    )
  }
}

@Composable
private fun DayGridCell(
  date: LocalDate,
  resolution: DayResolution,
  isToday: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, accentBorder) = when (resolution.status) {
    DayStatus.CLASS_DAY -> Triple(ClassGreenBg.copy(alpha = 0.8f), ClassGreenText, ClassGreen)
    DayStatus.OFF_DAY -> Triple(OffGrayBg.copy(alpha = 0.5f), OffGrayText, OffGray)
    DayStatus.HOLIDAY -> Triple(HolidayRedBg.copy(alpha = 0.85f), HolidayRedText, HolidayRed)
    DayStatus.EXAM -> Triple(ExamAmberBg.copy(alpha = 0.9f), ExamAmberText, ExamAmber)
  }

  Box(
    modifier = modifier
      .padding(2.dp)
      .aspectRatio(1f)
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .then(
        if (isToday) Modifier.border(2.dp, VividGradient, RoundedCornerShape(12.dp))
        else if (resolution.isOverridden) Modifier.border(1.5.dp, accentBorder, RoundedCornerShape(12.dp))
        else Modifier
      )
      .clickable(onClick = onClick)
      .padding(2.dp)
      .testTag("calendar_day_${date}"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = date.dayOfMonth.toString(),
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = if (isToday || resolution.isOverridden) FontWeight.Black else FontWeight.Bold,
          fontSize = 13.sp,
          color = textColor
        )
      )

      // Indicator: tiny dot or emoji
      if (resolution.status == DayStatus.EXAM) {
        Text(text = "📝", fontSize = 9.sp)
      } else if (resolution.status == DayStatus.HOLIDAY) {
        Text(text = "🎉", fontSize = 9.sp)
      } else if (resolution.classes.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.Center) {
          repeat(minOf(resolution.classes.size, 3)) {
            Box(
              modifier = Modifier
                .padding(horizontal = 1.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(textColor)
            )
          }
        }
      }
    }

    if (resolution.isOverridden) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(2.dp)
          .size(5.dp)
          .clip(CircleShape)
          .background(PinkSecondary)
      )
    }
  }
}

@Composable
private fun StatPill(
  label: String,
  color: Color,
  bgColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(bgColor)
      .padding(vertical = 8.dp, horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        color = color
      ),
      maxLines = 1
    )
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
      )
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateEditorBottomSheet(
  resolution: DayResolution,
  onClose: () -> Unit,
  onSave: (DayStatus, List<ClassItem>, String) -> Unit,
  onReset: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var currentStatus by remember(resolution) { mutableStateOf(resolution.status) }
  var noteText by remember(resolution) { mutableStateOf(resolution.note) }
  var classItems by remember(resolution) { mutableStateOf(resolution.classes.map { it.copy() }) }

  val dateTitle = remember(resolution.date) {
    resolution.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
  }

  ModalBottomSheet(
    onDismissRequest = onClose,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("date_editor_bottom_sheet"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Edit Date Schedule",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = dateTitle,
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }

          IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }
      }

      // Status selector chips
      item {
        Text(
          text = "DAY STATUS",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          DayStatus.values().forEach { st ->
            val isSelected = currentStatus == st
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .then(
                  if (isSelected) Modifier.background(VividGradient)
                  else Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )
                .clickable { currentStatus = st }
                .padding(vertical = 10.dp, horizontal = 2.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = st.emoji, fontSize = 14.sp)
                Text(
                  text = st.label.replace(" Day", ""),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }
      }

      // Class items if Class or Exam
      if (currentStatus == DayStatus.CLASS_DAY || currentStatus == DayStatus.EXAM) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (currentStatus == DayStatus.EXAM) "EXAM SUBJECTS & SLOTS" else "CLASS SUBJECTS & TIMES",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
              )
            )

            TextButton(
              onClick = {
                val newSubject = if (currentStatus == DayStatus.EXAM) "Exam Subject" else "New Subject"
                classItems = classItems + ClassItem(
                  subject = newSubject,
                  startTime = "10:00",
                  endTime = "11:30",
                  room = "Room 101"
                )
              }
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Row")
            }
          }
        }

        items(classItems.size) { idx ->
          val item = classItems[idx]
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                OutlinedTextField(
                  value = item.subject,
                  onValueChange = { newVal ->
                    val updated = classItems.toMutableList()
                    updated[idx] = item.copy(subject = newVal)
                    classItems = updated
                  },
                  label = { Text("Subject / Title") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )

                IconButton(
                  onClick = {
                    classItems = classItems.filterIndexed { i, _ -> i != idx }
                  }
                ) {
                  Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = HolidayRed)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = item.startTime,
                  onValueChange = { newVal ->
                    val updated = classItems.toMutableList()
                    updated[idx] = item.copy(startTime = newVal)
                    classItems = updated
                  },
                  label = { Text("Start (HH:MM)") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )

                OutlinedTextField(
                  value = item.endTime,
                  onValueChange = { newVal ->
                    val updated = classItems.toMutableList()
                    updated[idx] = item.copy(endTime = newVal)
                    classItems = updated
                  },
                  label = { Text("End (HH:MM)") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )

                OutlinedTextField(
                  value = item.room,
                  onValueChange = { newVal ->
                    val updated = classItems.toMutableList()
                    updated[idx] = item.copy(room = newVal)
                    classItems = updated
                  },
                  label = { Text("Room") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
              }
            }
          }
        }
      }

      // Optional note field
      item {
        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          label = { Text("Optional Note / Special Instructions") },
          placeholder = { Text("e.g. Midterm exam in Hall A, Bring calculator") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3
        )
      }

      // Actions: Save & Reset
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (resolution.isOverridden) {
            PillButton(
              text = "Reset Override",
              onClick = onReset,
              isPrimary = false,
              modifier = Modifier.weight(1f),
              icon = "🔄"
            )
          }

          PillButton(
            text = "Save Changes",
            onClick = {
              onSave(currentStatus, classItems, noteText)
            },
            isPrimary = true,
            modifier = Modifier.weight(1f),
            icon = "💾"
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
private fun QuickAddPeriodDialog(
  initialDate: LocalDate,
  onDismiss: () -> Unit,
  onApply: (QuickPeriodType, LocalDate, LocalDate, String, List<ClassItem>) -> Unit
) {
  var periodType by remember { mutableStateOf(QuickPeriodType.MIDTERM) }
  var startDaysOffset by remember { mutableStateOf(0) }
  var durationDays by remember { mutableStateOf(5) }
  var customNote by remember { mutableStateOf("") }
  var subjectName by remember { mutableStateOf("") }

  val startDate = remember(initialDate, startDaysOffset) { initialDate.plusDays(startDaysOffset.toLong()) }
  val endDate = remember(startDate, durationDays) { startDate.plusDays((durationDays - 1).coerceAtLeast(0).toLong()) }

  val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "⚡ Quick Add Period", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Select a period type to batch-apply across multiple dates in one tap.",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        // Type selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = "Period Type:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            QuickPeriodType.values().forEach { pt ->
              val isSelected = periodType == pt
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .then(
                    if (isSelected) Modifier.background(VividGradient)
                    else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                  )
                  .clickable {
                    periodType = pt
                    if (pt.defaultSubject.isNotBlank()) subjectName = pt.defaultSubject
                  }
                  .padding(vertical = 8.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = pt.emoji, fontSize = 14.sp)
                  Text(
                    text = pt.displayName.split(" ").first(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                  )
                }
              }
            }
          }
        }

        // Duration selector
        Column {
          Text(
            text = "Date Range: ${startDate.format(dateFmt)} → ${endDate.format(dateFmt)} ($durationDays days)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 3, 5, 7, 10).forEach { days ->
              val isSelected = durationDays == days
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .then(
                    if (isSelected) Modifier.background(VioletPrimary)
                    else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                  )
                  .clickable { durationDays = days }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "$days d",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                  )
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = customNote,
          onValueChange = { customNote = it },
          label = { Text("Period Name / Note") },
          placeholder = { Text(periodType.displayName) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      PillButton(
        text = "Apply Period",
        onClick = {
          val classes = if (periodType.defaultStatus == DayStatus.EXAM) {
            val title = if (subjectName.isNotBlank()) subjectName else periodType.displayName
            listOf(ClassItem(subject = title, startTime = "10:00", endTime = "12:00", room = "Exam Hall"))
          } else {
            emptyList()
          }
          val note = if (customNote.isNotBlank()) customNote else periodType.displayName
          onApply(periodType, startDate, endDate, note, classes)
        },
        icon = "⚡"
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
