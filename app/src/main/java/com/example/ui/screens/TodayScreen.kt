package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.Brush
import com.example.model.AcademicSettings
import com.example.model.DayResolution
import com.example.model.DayStatus
import com.example.ui.components.ClassCard
import com.example.ui.components.PillButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.VividGradient
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.TodayUiState
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
  todayState: TodayUiState,
  settings: AcademicSettings,
  onNavigateToCalendar: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onSelectDateInCalendar: (java.time.LocalDate) -> Unit,
  modifier: Modifier = Modifier
) {
  val res = todayState.todayResolution
  val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
  val dateTitle = todayState.today.format(dateFormatter)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(6.dp))
      // Hero Card with Gradient banner
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("today_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Gradient Accent Top Bar
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(VividGradient)
              .padding(horizontal = 20.dp, vertical = 18.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "TODAY'S SCHEDULE",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.2.sp
                  )
                )

                if (res != null) {
                  StatusBadge(status = res.status, isSmall = true)
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = dateTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Black,
                  fontSize = 22.sp,
                  color = Color.White
                )
              )

              if (settings.semesterName.isNotBlank() || settings.department.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${settings.semesterName} • ${settings.department}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                  )
                )
              }
            }
          }

          // Summary Headline inside Hero
          Column(modifier = Modifier.padding(20.dp)) {
            val status = res?.status ?: DayStatus.OFF_DAY
            val classCount = res?.classes?.size ?: 0

            when (status) {
              DayStatus.CLASS_DAY -> {
                if (classCount > 0) {
                  Text(
                    text = "You have $classCount class${if (classCount > 1) "es" else ""} scheduled today! 🎯",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  if (todayState.countdownText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(50))
                          .background(VioletPrimary.copy(alpha = 0.1f))
                          .padding(horizontal = 10.dp, vertical = 4.dp)
                      ) {
                        Text(
                          text = todayState.countdownText,
                          style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = VioletPrimary
                          )
                        )
                      }
                    }
                  }
                } else {
                  Text(
                    text = "No classes on the schedule today! ✨",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = "A free slot in your routine — perfect for catching up on projects.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }
              DayStatus.EXAM -> {
                Text(
                  text = "Exam Day in progress! 📝",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
                Text(
                  text = if (res?.note?.isNotBlank() == true) res.note else "Good luck with your examinations today! You've got this.",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
              DayStatus.HOLIDAY -> {
                Text(
                  text = "University Holiday! 🎉",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = if (res?.note?.isNotBlank() == true) res.note else "Campus is closed today. Enjoy your day off! 🏖️",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
              DayStatus.OFF_DAY -> {
                Text(
                  text = "Off Day — Relax & Recharge 🏖️",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "No university classes scheduled today.",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }

            if (res?.isOverridden == true && res.note.isNotBlank() && status != DayStatus.EXAM && status != DayStatus.HOLIDAY) {
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .padding(10.dp)
              ) {
                Text(
                  text = "📌 Note: ${res.note}",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
          }
        }
      }
    }

    // Real-Time Dynamic Countdown Component
    item {
      RealTimeClassCountdownCard(todayState = todayState)
    }

    // Classes List section (if any)
    if (res != null && res.classes.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Classes Today",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          )
          Text(
            text = "${res.classes.size} classes",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }

      items(res.classes) { classItem ->
        val isInProgress = todayState.inProgressClass?.id == classItem.id
        val isNext = todayState.nextClass?.id == classItem.id
        val countdown = if (isInProgress) todayState.countdownText else if (isNext) todayState.countdownText else null

        ClassCard(
          classItem = classItem,
          isInProgress = isInProgress,
          isNext = isNext,
          countdownText = countdown
        )
      }
    }

    // "Coming Up" Card: previewing the next few non-off days
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("coming_up_card"),
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
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = PinkSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Coming Up",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                  )
                )
                Text(
                  text = "Next non-off days ahead",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onNavigateToCalendar() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Full Calendar",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = VioletPrimary
                  )
                )
                Icon(
                  imageVector = Icons.Default.ArrowForward,
                  contentDescription = null,
                  tint = VioletPrimary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (todayState.upcomingNonOffDays.isEmpty()) {
            Text(
              text = "No upcoming classes or exams found in the next 30 days.",
              style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.padding(vertical = 12.dp)
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              val shortDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
              todayState.upcomingNonOffDays.forEach { upcomingDay ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onSelectDateInCalendar(upcomingDay.date) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = upcomingDay.date.format(shortDateFormatter),
                        style = MaterialTheme.typography.titleSmall.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = 15.sp
                        )
                      )
                      if (upcomingDay.isOverridden) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VioletPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                          Text(
                            text = "MODIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold,
                              color = VioletPrimary
                            )
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val summaryText = if (upcomingDay.status == DayStatus.EXAM) {
                      if (upcomingDay.note.isNotBlank()) upcomingDay.note else "Scheduled Examination"
                    } else if (upcomingDay.status == DayStatus.HOLIDAY) {
                      if (upcomingDay.note.isNotBlank()) upcomingDay.note else "University Holiday"
                    } else if (upcomingDay.classes.isNotEmpty()) {
                      val subs = upcomingDay.classes.take(2).joinToString(", ") { it.subject }
                      if (upcomingDay.classes.size > 2) "$subs +${upcomingDay.classes.size - 2} more" else subs
                    } else {
                      "Class day"
                    }

                    Text(
                      text = summaryText,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      maxLines = 1
                    )
                  }

                  Spacer(modifier = Modifier.width(8.dp))
                  StatusBadge(status = upcomingDay.status, isSmall = true)
                }
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun RealTimeClassCountdownCard(
  todayState: TodayUiState,
  modifier: Modifier = Modifier
) {
  val nextClass = todayState.nextClass
  val inProgressClass = todayState.inProgressClass
  val secondsUntilNext = todayState.secondsUntilNextClass
  val secondsLeftInClass = todayState.secondsLeftInClass
  val totalDuration = todayState.totalClassDurationSeconds ?: 5400L

  if (nextClass != null && secondsUntilNext != null) {
    val hours = secondsUntilNext / 3600
    val minutes = (secondsUntilNext % 3600) / 60
    val seconds = secondsUntilNext % 60

    Card(
      modifier = modifier
        .fillMaxWidth()
        .testTag("real_time_countdown_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, VividGradient, RoundedCornerShape(24.dp))
          .padding(18.dp)
      ) {
        // Tag & Status Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(50))
              .background(VioletPrimary.copy(alpha = 0.12f))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(VioletPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "NEXT CLASS COUNTDOWN",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = VioletPrimary,
                letterSpacing = 0.8.sp,
                fontSize = 11.sp
              )
            )
          }

          Text(
            text = "Starts at ${nextClass.startTime}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Next Subject Title
        Text(
          text = nextClass.subject,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Black,
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.onSurface
          ),
          maxLines = 2
        )

        if (nextClass.room.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = PinkSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = nextClass.room,
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Digital Countdown Blocks [HH] : [MM] : [SS]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CountdownDigitBlock(value = String.format("%02d", hours), unit = "HOURS")
          CountdownSeparator()
          CountdownDigitBlock(value = String.format("%02d", minutes), unit = "MINUTES")
          CountdownSeparator()
          CountdownDigitBlock(value = String.format("%02d", seconds), unit = "SECONDS", isAccent = true)
        }
      }
    }
  } else if (inProgressClass != null && secondsLeftInClass != null) {
    val minutes = secondsLeftInClass / 60
    val seconds = secondsLeftInClass % 60
    val elapsed = (totalDuration - secondsLeftInClass).coerceAtLeast(0)
    val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    Card(
      modifier = modifier
        .fillMaxWidth()
        .testTag("real_time_inprogress_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .border(2.dp, VividGradient, RoundedCornerShape(24.dp))
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(50))
              .background(PinkSecondary.copy(alpha = 0.15f))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PinkSecondary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "IN SESSION RIGHT NOW",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PinkSecondary,
                letterSpacing = 0.8.sp,
                fontSize = 11.sp
              )
            )
          }

          Text(
            text = "Ends at ${inProgressClass.endTime}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = inProgressClass.subject,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Black,
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CountdownDigitBlock(value = String.format("%02d", minutes), unit = "MINS LEFT")
          CountdownSeparator()
          CountdownDigitBlock(value = String.format("%02d", seconds), unit = "SECS LEFT", isAccent = true)
        }

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50)),
          color = VioletPrimary,
          trackColor = VioletPrimary.copy(alpha = 0.15f)
        )
      }
    }
  }
}

@Composable
private fun CountdownDigitBlock(
  value: String,
  unit: String,
  isAccent: Boolean = false
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(78.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(
          if (isAccent) VividGradient else Brush.horizontalGradient(listOf(NavyDeep, NavyPrimary))
        )
        .padding(vertical = 10.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Black,
          fontSize = 26.sp,
          color = Color.White,
          letterSpacing = 1.sp
        )
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = unit,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp
      )
    )
  }
}

@Composable
private fun CountdownSeparator() {
  Text(
    text = ":",
    style = MaterialTheme.typography.headlineMedium.copy(
      fontWeight = FontWeight.Black,
      fontSize = 24.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 16.dp)
  )
}
