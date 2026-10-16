package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.model.ClassItem
import com.example.model.DayStatus

object NotificationHelper {
  const val CHANNEL_ID = "academic_calendar_channel"
  private const val CHANNEL_NAME = "Class & Exam Reminders"
  private const val CHANNEL_DESC = "Notifications for upcoming university classes and schedule updates"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
      }
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun sendTestNotification(context: Context): Boolean {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
      context, 0, intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle("🎓 Academic Calendar Reminder")
      .setContentText("Tomorrow: 3 classes (Data Structures at 09:00 AM). Ready to crush it! ✨")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Tomorrow's Schedule:\n• 09:00 AM - 10:30 AM: Data Structures & Algorithms (Lab 301)\n• 11:00 AM - 12:30 PM: Computer Networks (Hall B)\n• 02:00 PM - 03:30 PM: Linear Algebra (Hall A)\n\nPack your notes and set your alarms! 📚"
        )
      )
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)

    return try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(1001, builder.build())
      true
    } catch (e: SecurityException) {
      false
    }
  }

  fun sendTomorrowScheduleNotification(
    context: Context,
    status: DayStatus,
    classes: List<ClassItem>,
    note: String
  ): Boolean {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context, 0, intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val title = when (status) {
      DayStatus.CLASS_DAY -> "📚 Tomorrow's Classes"
      DayStatus.EXAM -> "📝 Exam Alert for Tomorrow!"
      DayStatus.HOLIDAY -> "🎉 Tomorrow is a Holiday!"
      DayStatus.OFF_DAY -> "🏖️ Tomorrow is an Off Day"
    }

    val contentText = when (status) {
      DayStatus.CLASS_DAY -> {
        if (classes.isNotEmpty()) {
          "You have ${classes.size} class(es). First starts at ${classes.first().startTime}."
        } else {
          "No scheduled classes tomorrow. Enjoy your day!"
        }
      }
      DayStatus.EXAM -> "Upcoming exam tomorrow. Check your schedule and notes!"
      DayStatus.HOLIDAY -> "Holiday tomorrow! Enjoy your break. ✨"
      DayStatus.OFF_DAY -> "No classes scheduled tomorrow. Recharge! ☕"
    }

    val bigTextBuilder = StringBuilder()
    bigTextBuilder.append(contentText).append("\n")
    if (note.isNotBlank()) {
      bigTextBuilder.append("\nNote: ").append(note).append("\n")
    }
    if (classes.isNotEmpty()) {
      bigTextBuilder.append("\nSchedule:\n")
      classes.forEach {
        val roomPart = if (it.room.isNotBlank()) " [${it.room}]" else ""
        bigTextBuilder.append("• ${it.startTime} - ${it.endTime}: ${it.subject}$roomPart\n")
      }
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(contentText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextBuilder.toString().trim()))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)

    return try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(1002, builder.build())
      true
    } catch (e: SecurityException) {
      false
    }
  }
}
