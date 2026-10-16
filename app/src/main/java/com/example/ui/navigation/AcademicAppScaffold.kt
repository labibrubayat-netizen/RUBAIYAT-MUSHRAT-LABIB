package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppTopBar
import com.example.ui.components.VividGradient
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.AcademicUiState
import com.example.viewmodel.AcademicViewModel
import com.example.viewmodel.AppScreen
import kotlinx.coroutines.launch

@Composable
fun AcademicAppScaffold(
  viewModel: AcademicViewModel,
  uiState: AcademicUiState,
  modifier: Modifier = Modifier
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  // Handle back press: if drawer open -> close; if screen != TODAY -> go to TODAY
  BackHandler(enabled = drawerState.isOpen || uiState.activeScreen != AppScreen.TODAY) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else {
      viewModel.setScreen(AppScreen.TODAY)
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier
          .width(300.dp)
          .testTag("navigation_drawer"),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
      ) {
        // Drawer Header with Vivid Gradient
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(VividGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
          Column {
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "🎓", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Academic Calendar",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            )

            val subtitle = "${uiState.settings.semesterName} • ${uiState.settings.term}"
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Drawer Items
        AppScreen.values().forEach { screen ->
          val isSelected = uiState.activeScreen == screen
          NavigationDrawerItem(
            label = {
              Text(
                text = screen.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              )
            },
            icon = {
              Text(text = screen.icon, fontSize = 20.sp)
            },
            selected = isSelected,
            onClick = {
              viewModel.setScreen(screen)
              scope.launch { drawerState.close() }
            },
            modifier = Modifier
              .padding(horizontal = 12.dp, vertical = 4.dp)
              .testTag("nav_item_${screen.name.lowercase()}"),
            shape = RoundedCornerShape(16.dp),
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = VioletPrimary.copy(alpha = 0.15f),
              selectedTextColor = VioletPrimary,
              unselectedTextColor = MaterialTheme.colorScheme.onSurface
            )
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer info inside drawer
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(20.dp)
        ) {
          Text(
            text = uiState.settings.department,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          Text(
            text = "Offline local storage enabled ✨",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              fontSize = 11.sp
            )
          )
        }
      }
    }
  ) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .statusBarsPadding(),
      topBar = {
        val screenSubtitle = when (uiState.activeScreen) {
          AppScreen.TODAY -> uiState.settings.semesterName
          AppScreen.CALENDAR -> "Month-by-month & Overrides"
          AppScreen.SETTINGS -> "Routine & Course Settings"
          AppScreen.ABOUT -> "How to Use & Install"
        }
        AppTopBar(
          title = uiState.activeScreen.title,
          subtitle = screenSubtitle,
          onMenuClick = {
            scope.launch { drawerState.open() }
          }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .navigationBarsPadding()
      ) {
        AnimatedContent(
          targetState = uiState.activeScreen,
          transitionSpec = {
            val fromIndex = initialState.ordinal
            val toIndex = targetState.ordinal
            if (toIndex >= fromIndex) {
              (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
              ) + fadeIn(animationSpec = tween(220)))
                .togetherWith(
                  slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
                  ) + fadeOut(animationSpec = tween(180))
                )
            } else {
              (slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
              ) + fadeIn(animationSpec = tween(220)))
                .togetherWith(
                  slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth / 3 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
                  ) + fadeOut(animationSpec = tween(180))
                )
            }
          },
          label = "screen_slide_transition",
          modifier = Modifier.fillMaxSize()
        ) { targetScreen ->
          when (targetScreen) {
            AppScreen.TODAY -> {
              TodayScreen(
                todayState = uiState.todayState,
                settings = uiState.settings,
                onNavigateToCalendar = { viewModel.setScreen(AppScreen.CALENDAR) },
                onNavigateToSettings = { viewModel.setScreen(AppScreen.SETTINGS) },
                onSelectDateInCalendar = { date ->
                  viewModel.selectMonth(java.time.YearMonth.of(date.year, date.month))
                  viewModel.openDateEditor(date)
                  viewModel.setScreen(AppScreen.CALENDAR)
                }
              )
            }
            AppScreen.CALENDAR -> {
              val context = androidx.compose.ui.platform.LocalContext.current
              CalendarScreen(
                calendarState = uiState.calendarState,
                onSelectMonth = { ym -> viewModel.selectMonth(ym) },
                onDateClick = { date -> viewModel.openDateEditor(date) },
                onCloseEditor = { viewModel.closeDateEditor() },
                onSaveOverride = { date, status, classes, note ->
                  viewModel.saveDateOverride(date, status, classes, note)
                },
                onResetOverride = { date ->
                  viewModel.resetDateOverride(date)
                },
                onOpenQuickPeriod = { viewModel.openQuickPeriodDialog() },
                onCloseQuickPeriod = { viewModel.closeQuickPeriodDialog() },
                onApplyQuickPeriod = { pType, sDate, eDate, note, classes ->
                  viewModel.applyQuickPeriod(pType, sDate, eDate, note, classes)
                },
                onExportICalendar = { viewModel.exportICalendar(context) },
                resolveDay = { date -> viewModel.resolveDay(date) }
              )
            }
            AppScreen.SETTINGS -> {
              val context = androidx.compose.ui.platform.LocalContext.current
              SettingsScreen(
                settings = uiState.settings,
                weeklyRoutine = uiState.weeklyRoutine,
                onSaveSettings = { s -> viewModel.updateAcademicSettings(s) },
                onUpdateDaySchedule = { d -> viewModel.updateDayInRoutine(d) },
                onCopyRoutine = { src, targets -> viewModel.copyRoutineToOtherDays(src, targets) },
                onLoadDemoData = { viewModel.loadDemoSchedule() },
                onSendTestNotification = { viewModel.sendTestNotification() },
                onSendTomorrowReminder = { viewModel.triggerTomorrowReminderNow() },
                onExportICalendar = { viewModel.exportICalendar(context) }
              )
            }
            AppScreen.ABOUT -> {
              AboutScreen(
                onNavigateToToday = { viewModel.setScreen(AppScreen.TODAY) },
                onNavigateToCalendar = { viewModel.setScreen(AppScreen.CALENDAR) }
              )
            }
          }
        }

        // Notification Message Toast / Banner
        AnimatedVisibility(
          visible = uiState.notificationMessage != null,
          enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(16.dp)
        ) {
          if (uiState.notificationMessage != null) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
              elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.clearNotificationMessage() }
                .testTag("app_notification_banner")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = uiState.notificationMessage,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontWeight = FontWeight.SemiBold
                  ),
                  modifier = Modifier.weight(1f)
                )
                IconButton(
                  onClick = { viewModel.clearNotificationMessage() },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(16.dp)
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
