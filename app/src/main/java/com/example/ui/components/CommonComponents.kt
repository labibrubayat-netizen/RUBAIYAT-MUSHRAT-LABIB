package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClassItem
import com.example.model.DayStatus
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
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OffGrayText
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VioletPrimary

// Gen Z Electric Violet & Navy Blue Gradients
val VividGradient = Brush.horizontalGradient(
  listOf(NavyPrimary, Color(0xFF5B21B6), VioletPrimary)
)

val VividVerticalGradient = Brush.verticalGradient(
  listOf(NavyPrimary, VioletPrimary)
)

val NavyToVioletGradient = Brush.horizontalGradient(
  listOf(NavyDeep, VioletNeon)
)

val CardGlowGradient = Brush.linearGradient(
  listOf(NavyPrimary.copy(alpha = 0.12f), VioletPrimary.copy(alpha = 0.15f))
)

@Composable
fun AppTopBar(
  title: String,
  subtitle: String? = null,
  onMenuClick: () -> Unit,
  actions: @Composable () -> Unit = {}
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(VividGradient)
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onMenuClick,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .testTag("hamburger_menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open Menu",
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              fontSize = 20.sp,
              color = Color.White
            )
          )
          if (!subtitle.isNullOrBlank()) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        actions()
      }
    }
  }
}

@Composable
fun StatusBadge(
  status: DayStatus,
  modifier: Modifier = Modifier,
  isSmall: Boolean = false
) {
  val (bgColor, textColor, borderColor) = when (status) {
    DayStatus.CLASS_DAY -> Triple(ClassGreenBg, ClassGreenText, ClassGreen)
    DayStatus.OFF_DAY -> Triple(OffGrayBg, OffGrayText, OffGray)
    DayStatus.HOLIDAY -> Triple(HolidayRedBg, HolidayRedText, HolidayRed)
    DayStatus.EXAM -> Triple(ExamAmberBg, ExamAmberText, ExamAmber)
  }

  val paddingH = if (isSmall) 8.dp else 12.dp
  val paddingV = if (isSmall) 4.dp else 6.dp
  val fontSize = if (isSmall) 12.sp else 13.sp

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(bgColor)
      .border(1.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(50))
      .padding(horizontal = paddingH, vertical = paddingV),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = status.emoji,
        fontSize = fontSize,
        modifier = Modifier.padding(end = 4.dp)
      )
      Text(
        text = status.label,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
    }
  }
}

@Composable
fun PillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: String? = null,
  isPrimary: Boolean = true,
  enabled: Boolean = true
) {
  val bgModifier = if (isPrimary) {
    Modifier.background(VividGradient)
  } else {
    Modifier
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(50))
  }

  Box(
    modifier = modifier
      .height(48.dp)
      .clip(RoundedCornerShape(50))
      .then(bgModifier)
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 20.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface
        )
      )
    }
  }
}

@Composable
fun ClassCard(
  classItem: ClassItem,
  isInProgress: Boolean = false,
  isNext: Boolean = false,
  countdownText: String? = null,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  val cardBorder = if (isInProgress) {
    BorderStrokeMod(2.dp, VividGradient)
  } else if (isNext) {
    BorderStrokeMod(1.5.dp, Brush.horizontalGradient(listOf(VioletPrimary, VioletPrimary)))
  } else {
    null
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .testTag("class_card_${classItem.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isInProgress) {
        Color(0xFFFAF5FF)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isInProgress) 4.dp else 1.5.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (cardBorder != null) Modifier.border(cardBorder.width, cardBorder.brush, RoundedCornerShape(20.dp)) else Modifier)
        .padding(16.dp)
    ) {
      // In progress or countdown badge
      if (isInProgress) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(PinkSecondary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "IN PROGRESS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PinkSecondary,
                letterSpacing = 1.sp
              )
            )
          }

          if (!countdownText.isNullOrBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(VividGradient)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = countdownText,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        }
      } else if (isNext && !countdownText.isNullOrBlank()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "⚡ UP NEXT",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = VioletPrimary
            )
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(50))
              .background(VioletPrimary.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = countdownText,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = VioletPrimary
              )
            )
          }
        }
      }

      // Subject Name
      Text(
        text = classItem.subject,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Time & Room Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = VioletPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${classItem.startTime} - ${classItem.endTime}",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }

        if (classItem.room.isNotBlank()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = PinkSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = classItem.room,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }
    }
  }
}

private data class BorderStrokeMod(val width: androidx.compose.ui.unit.Dp, val brush: Brush)
