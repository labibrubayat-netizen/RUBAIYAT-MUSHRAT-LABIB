package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
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
import com.example.ui.components.PillButton
import com.example.ui.components.VividGradient
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.VioletPrimary

@Composable
fun AboutScreen(
  onNavigateToToday: () -> Unit,
  onNavigateToCalendar: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // App Hero Card
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("about_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(VividGradient)
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "🎓", fontSize = 32.sp)
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Academic Calendar",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.Black,
                  color = Color.White
                )
              )
              Text(
                text = "Built for university students • Version 1.0",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = Color.White.copy(alpha = 0.9f),
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }

          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Academic Calendar is a modern, student-centric schedule planner designed to take the friction out of university life. From tracking daily lectures with live countdowns to batch-scheduling midterms and exams, everything is at your fingertips.",
              style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }
    }

    // How the Data Model Works
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VioletPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = VioletPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "How the 3-Tier Model Works",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          ModelStepItem(
            step = "1",
            title = "Weekly Routine Template",
            desc = "Define which days (Mon-Sun) you attend university and the default subjects/slots. This repeats every week automatically."
          )

          Spacer(modifier = Modifier.height(10.dp))

          ModelStepItem(
            step = "2",
            title = "Per-Date Overrides",
            desc = "Need to swap a lecture, mark an unexpected holiday, or log an exam? Tap any date in the Calendar to override that day's routine without changing the rest of the semester."
          )

          Spacer(modifier = Modifier.height(10.dp))

          ModelStepItem(
            step = "3",
            title = "Quick Add Period Tool",
            desc = "Apply Midterm Week, Finals, or Holiday Breaks across a continuous date range with just 1 tap. Overrides are generated instantly!"
          )
        }
      }
    }

    // Installation Guide (Home Screen & PWA)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PinkSecondary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.InstallMobile, contentDescription = null, tint = PinkSecondary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Installation & Home Screen",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Fast launch & offline access",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "📱 On Android Devices:\n• The app runs natively as an Android app with full instant offline capabilities.\n• You can place an icon or shortcut directly on your home screen launcher for 1-tap schedule checks before class.",
            style = MaterialTheme.typography.bodyMedium.copy(
              lineHeight = 22.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "🌐 Web & PWA Users:\n• In Chrome or Edge, tap the browser menu (⋮) and select 'Install app' or 'Add to Home Screen'.\n• On Safari (iOS), tap the Share icon and select 'Add to Home Screen' for a fullscreen app experience.",
            style = MaterialTheme.typography.bodyMedium.copy(
              lineHeight = 22.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }

    // Privacy & Storage Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VioletPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = VioletPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Privacy First & 100% Offline",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "All schedule templates, overrides, and settings are saved locally on your device storage. No account sign-up is required, and your course data stays private to you.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
          )
        }
      }
    }

    // Quick Actions
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PillButton(
          text = "View Today",
          onClick = onNavigateToToday,
          icon = "📅",
          isPrimary = true,
          modifier = Modifier.weight(1f)
        )

        PillButton(
          text = "Open Calendar",
          onClick = onNavigateToCalendar,
          icon = "🗓️",
          isPrimary = false,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun ModelStepItem(step: String, title: String, desc: String) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(VividGradient),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = step,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 18.sp
        )
      )
    }
  }
}
