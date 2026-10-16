package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicSettings
import com.example.model.ClassItem
import com.example.model.DaySchedule
import com.example.ui.components.PillButton
import com.example.ui.components.VividGradient
import com.example.ui.theme.ClassGreen
import com.example.ui.theme.HolidayRed
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.VioletPrimary
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SettingsScreen(
  settings: AcademicSettings,
  weeklyRoutine: List<DaySchedule>,
  onSaveSettings: (AcademicSettings) -> Unit,
  onUpdateDaySchedule: (DaySchedule) -> Unit,
  onCopyRoutine: (DaySchedule, List<Int>) -> Unit,
  onLoadDemoData: () -> Unit,
  onSendTestNotification: () -> Unit,
  onSendTomorrowReminder: () -> Unit,
  onExportICalendar: () -> Unit,
  modifier: Modifier = Modifier
) {
  var department by remember(settings) { mutableStateOf(settings.department) }
  var semesterName by remember(settings) { mutableStateOf(settings.semesterName) }
  var term by remember(settings) { mutableStateOf(settings.term) }
  var startMonth by remember(settings) { mutableIntStateOf(settings.startMonth) }
  var startYear by remember(settings) { mutableIntStateOf(settings.startYear) }
  var endMonth by remember(settings) { mutableIntStateOf(settings.endMonth) }
  var endYear by remember(settings) { mutableIntStateOf(settings.endYear) }
  var reminderEnabled by remember(settings) { mutableStateOf(settings.reminderEnabled) }
  var reminderHour by remember(settings) { mutableIntStateOf(settings.reminderHour) }
  var reminderMinute by remember(settings) { mutableIntStateOf(settings.reminderMinute) }

  // Active selected weekday for routine editing (1..7)
  var selectedWeekday by remember { mutableIntStateOf(1) }
  val activeDaySchedule = weeklyRoutine.firstOrNull { it.dayOfWeek == selectedWeekday }
    ?: DaySchedule(dayOfWeek = selectedWeekday, isClassDay = true, classes = emptyList())

  var isCopyDialogOpen by remember { mutableStateOf(false) }

  // Notification permission launcher for Android 13+
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      onSendTestNotification()
    }
  }

  fun requestNotificationAndTrigger(action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    action()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Academic Info Card
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("academic_info_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VioletPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.School, contentDescription = null, tint = VioletPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Academic Information",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Generic setup for any department or term",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department / Faculty") },
            placeholder = { Text("e.g. Computer Science & Engineering") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = semesterName,
              onValueChange = { semesterName = it },
              label = { Text("Semester Name") },
              placeholder = { Text("e.g. Fall 2026") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            OutlinedTextField(
              value = term,
              onValueChange = { term = it },
              label = { Text("Term / Year") },
              placeholder = { Text("e.g. Term 1") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Semester date span (Month & Year)
          Text(
            text = "SEMESTER SPAN",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = 1.sp
            )
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Start: Month $startMonth / $startYear",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
              )
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                  onClick = {
                    if (startMonth > 1) startMonth-- else { startMonth = 12; startYear-- }
                  }
                ) { Text("◀") }
                TextButton(
                  onClick = {
                    if (startMonth < 12) startMonth++ else { startMonth = 1; startYear++ }
                  }
                ) { Text("▶") }
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "End: Month $endMonth / $endYear",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
              )
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                  onClick = {
                    if (endMonth > 1) endMonth-- else { endMonth = 12; endYear-- }
                  }
                ) { Text("◀") }
                TextButton(
                  onClick = {
                    if (endMonth < 12) endMonth++ else { endMonth = 1; endYear++ }
                  }
                ) { Text("▶") }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          PillButton(
            text = "Save Academic Info",
            onClick = {
              onSaveSettings(
                settings.copy(
                  department = department,
                  semesterName = semesterName,
                  term = term,
                  startMonth = startMonth,
                  startYear = startYear,
                  endMonth = endMonth,
                  endYear = endYear
                )
              )
            },
            icon = "💾",
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Editable Weekly Routine Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("weekly_routine_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(PinkSecondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = PinkSecondary)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Weekly Routine",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Configure default classes for each day",
                  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
              }
            }

            IconButton(
              onClick = { isCopyDialogOpen = true },
              modifier = Modifier.testTag("copy_routine_button")
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy routine to other days", tint = VioletPrimary)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Weekday chips selector (Mon-Sun)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            (1..7).forEach { dayNum ->
              val dow = DayOfWeek.of(dayNum)
              val dayShort = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault())
              val isSelected = selectedWeekday == dayNum
              val schedule = weeklyRoutine.firstOrNull { it.dayOfWeek == dayNum }
              val isClassDay = schedule?.isClassDay == true

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .then(
                    if (isSelected) Modifier.background(VividGradient)
                    else if (isClassDay) Modifier
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .border(1.dp, ClassGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    else Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                  )
                  .clickable { selectedWeekday = dayNum }
                  .padding(vertical = 8.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = dayShort,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      fontSize = 11.sp
                    )
                  )
                  Box(
                    modifier = Modifier
                      .padding(top = 3.dp)
                      .size(4.dp)
                      .clip(CircleShape)
                      .background(
                        if (isSelected) Color.White
                        else if (isClassDay) ClassGreen
                        else Color.Transparent
                      )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Active day controls
          val dayFullName = DayOfWeek.of(selectedWeekday).getDisplayName(TextStyle.FULL, Locale.getDefault())
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "$dayFullName Routine",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = if (activeDaySchedule.isClassDay) "Classes active" else "Off day (No classes)",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = if (activeDaySchedule.isClassDay) ClassGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }

            Switch(
              checked = activeDaySchedule.isClassDay,
              onCheckedChange = { isChecked ->
                onUpdateDaySchedule(activeDaySchedule.copy(isClassDay = isChecked))
              },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VioletPrimary)
            )
          }

          if (activeDaySchedule.isClassDay) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "CLASSES (${activeDaySchedule.classes.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  letterSpacing = 1.sp
                )
              )

              TextButton(
                onClick = {
                  val newClasses = activeDaySchedule.classes + ClassItem(
                    subject = "New Subject",
                    startTime = "09:00",
                    endTime = "10:30",
                    room = "Room 101"
                  )
                  onUpdateDaySchedule(activeDaySchedule.copy(classes = newClasses))
                }
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Class")
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              activeDaySchedule.classes.forEachIndexed { idx, classItem ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      OutlinedTextField(
                        value = classItem.subject,
                        onValueChange = { newVal ->
                          val updated = activeDaySchedule.classes.toMutableList()
                          updated[idx] = classItem.copy(subject = newVal)
                          onUpdateDaySchedule(activeDaySchedule.copy(classes = updated))
                        },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                      )

                      IconButton(
                        onClick = {
                          val updated = activeDaySchedule.classes.filterIndexed { i, _ -> i != idx }
                          onUpdateDaySchedule(activeDaySchedule.copy(classes = updated))
                        }
                      ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = HolidayRed)
                      }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      OutlinedTextField(
                        value = classItem.startTime,
                        onValueChange = { newVal ->
                          val updated = activeDaySchedule.classes.toMutableList()
                          updated[idx] = classItem.copy(startTime = newVal)
                          onUpdateDaySchedule(activeDaySchedule.copy(classes = updated))
                        },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                      )

                      OutlinedTextField(
                        value = classItem.endTime,
                        onValueChange = { newVal ->
                          val updated = activeDaySchedule.classes.toMutableList()
                          updated[idx] = classItem.copy(endTime = newVal)
                          onUpdateDaySchedule(activeDaySchedule.copy(classes = updated))
                        },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                      )

                      OutlinedTextField(
                        value = classItem.room,
                        onValueChange = { newVal ->
                          val updated = activeDaySchedule.classes.toMutableList()
                          updated[idx] = classItem.copy(room = newVal)
                          onUpdateDaySchedule(activeDaySchedule.copy(classes = updated))
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
          }
        }
      }
    }

    // Notifications & Daily Reminders Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("notification_settings_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(VioletPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = VioletPrimary)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Class Reminders",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Remind me about tomorrow's classes",
                  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
              }
            }

            Switch(
              checked = reminderEnabled,
              onCheckedChange = { isChecked ->
                reminderEnabled = isChecked
                val updated = settings.copy(
                  reminderEnabled = isChecked,
                  reminderHour = reminderHour,
                  reminderMinute = reminderMinute
                )
                onSaveSettings(updated)
                if (isChecked) {
                  requestNotificationAndTrigger { }
                }
              },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VioletPrimary)
            )
          }

          AnimatedVisibility(visible = reminderEnabled) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
              Text(
                text = "Reminder Time: ${String.format("%02d:%02d", reminderHour, reminderMinute)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("18:00 (6 PM)", "20:00 (8 PM)", "21:30 (9:30 PM)", "22:00 (10 PM)").forEach { slot ->
                  val parts = slot.split(" ").first().split(":")
                  val h = parts[0].toInt()
                  val m = parts[1].toInt()
                  val isSel = reminderHour == h && reminderMinute == m

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .then(
                        if (isSel) Modifier.background(VioletPrimary)
                        else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                      )
                      .clickable {
                        reminderHour = h
                        reminderMinute = m
                        onSaveSettings(
                          settings.copy(
                            reminderEnabled = true,
                            reminderHour = h,
                            reminderMinute = m
                          )
                        )
                      }
                      .padding(horizontal = 8.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = "${h}:${String.format("%02d", m)}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                      )
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PillButton(
              text = "Send Test Notification",
              onClick = {
                requestNotificationAndTrigger { onSendTestNotification() }
              },
              icon = "🔔",
              isPrimary = false,
              modifier = Modifier.weight(1f)
            )

            PillButton(
              text = "Preview Tomorrow",
              onClick = {
                requestNotificationAndTrigger { onSendTomorrowReminder() }
              },
              icon = "📅",
              isPrimary = false,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "ℹ️ Note: On-device notifications summarize tomorrow's classes or inform you if it's an off day. Works locally with zero server dependency.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    // iCalendar (.ics) Export & Sync Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("export_icalendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VioletPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = VioletPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Sync with System Calendar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Export as iCalendar (.ics) file",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Export your entire ${settings.semesterName} schedule, class slots, rooms, exam dates, and holidays into an .ics file. Seamlessly syncs with Google Calendar, Apple Calendar, Samsung Calendar, and Outlook.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 20.sp
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          PillButton(
            text = "Export to iCalendar (.ics)",
            onClick = onExportICalendar,
            icon = "🗓️",
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Demo Data & Reset Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Reset & Demo Data",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Instantly populate sample university schedule to explore all features.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
          )

          PillButton(
            text = "Load Demo University Schedule",
            onClick = onLoadDemoData,
            icon = "🎓",
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  // Copy Routine Dialog
  if (isCopyDialogOpen) {
    CopyRoutineDialog(
      sourceDaySchedule = activeDaySchedule,
      onDismiss = { isCopyDialogOpen = false },
      onConfirm = { targets ->
        onCopyRoutine(activeDaySchedule, targets)
        isCopyDialogOpen = false
      }
    )
  }
}

@Composable
private fun CopyRoutineDialog(
  sourceDaySchedule: DaySchedule,
  onDismiss: () -> Unit,
  onConfirm: (List<Int>) -> Unit
) {
  val sourceName = DayOfWeek.of(sourceDaySchedule.dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())
  var selectedDays by remember { mutableStateOf(setOf<Int>()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Copy $sourceName Schedule", fontWeight = FontWeight.Bold)
    },
    text = {
      Column {
        Text(
          text = "Select which weekdays should copy classes from $sourceName (${sourceDaySchedule.classes.size} classes):",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(10.dp))
        (1..7).filter { it != sourceDaySchedule.dayOfWeek }.forEach { dayNum ->
          val dowName = DayOfWeek.of(dayNum).getDisplayName(TextStyle.FULL, Locale.getDefault())
          val isChecked = selectedDays.contains(dayNum)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                selectedDays = if (isChecked) selectedDays - dayNum else selectedDays + dayNum
              }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = isChecked,
              onCheckedChange = { chk ->
                selectedDays = if (chk) selectedDays + dayNum else selectedDays - dayNum
              }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = dowName, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    },
    confirmButton = {
      PillButton(
        text = "Copy to Selected (${selectedDays.size})",
        onClick = { onConfirm(selectedDays.toList()) },
        enabled = selectedDays.isNotEmpty(),
        icon = "📋"
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
