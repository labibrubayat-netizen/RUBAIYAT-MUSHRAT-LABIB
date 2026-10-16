package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.AcademicSettings
import com.example.model.DayResolution
import com.example.model.DayStatus
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

object ICalendarExporter {

  private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
  private val DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

  fun generateICalendarString(
    settings: AcademicSettings,
    resolveDay: (LocalDate) -> DayResolution
  ): String {
    val sb = StringBuilder()
    val calName = escapeIcsText("${settings.semesterName} - ${settings.department}")

    sb.appendLine("BEGIN:VCALENDAR")
    sb.appendLine("VERSION:2.0")
    sb.appendLine("PRODID:-//Academic Calendar//University Student Timetable//EN")
    sb.appendLine("CALSCALE:GREGORIAN")
    sb.appendLine("METHOD:PUBLISH")
    sb.appendLine("X-WR-CALNAME:$calName")
    sb.appendLine("X-WR-TIMEZONE:UTC")

    val nowStamp = LocalDate.now().format(DATE_ONLY_FORMATTER) + "T120000Z"

    // Iterate through all days of the semester
    val startYm = try {
      YearMonth.of(settings.startYear, settings.startMonth)
    } catch (e: Exception) {
      YearMonth.now()
    }

    val endYm = try {
      YearMonth.of(settings.endYear, settings.endMonth)
    } catch (e: Exception) {
      startYm.plusMonths(4)
    }

    var currDate = startYm.atDay(1)
    val endDate = endYm.atEndOfMonth()

    var eventCount = 0

    while (!currDate.isAfter(endDate) && eventCount < 500) {
      val res = resolveDay(currDate)
      val dateFormatted = currDate.format(DATE_ONLY_FORMATTER)

      when (res.status) {
        DayStatus.CLASS_DAY, DayStatus.EXAM -> {
          if (res.classes.isNotEmpty()) {
            for (c in res.classes) {
              val startClean = c.startTime.replace(":", "").padEnd(4, '0') + "00"
              val endClean = c.endTime.replace(":", "").padEnd(4, '0') + "00"
              val summary = if (res.status == DayStatus.EXAM) "📝 EXAM: ${c.subject}" else c.subject

              sb.appendLine("BEGIN:VEVENT")
              sb.appendLine("UID:${currDate}-${c.id.take(8)}@academiccalendar")
              sb.appendLine("DTSTAMP:$nowStamp")
              sb.appendLine("DTSTART:${dateFormatted}T${startClean}")
              sb.appendLine("DTEND:${dateFormatted}T${endClean}")
              sb.appendLine("SUMMARY:${escapeIcsText(summary)}")
              if (c.room.isNotBlank()) {
                sb.appendLine("LOCATION:${escapeIcsText(c.room)}")
              }
              val desc = buildString {
                if (c.instructor.isNotBlank()) append("Instructor: ${c.instructor}\\n")
                if (res.note.isNotBlank()) append("Note: ${res.note}")
              }
              if (desc.isNotBlank()) {
                sb.appendLine("DESCRIPTION:${escapeIcsText(desc)}")
              }
              sb.appendLine("STATUS:CONFIRMED")
              sb.appendLine("END:VEVENT")
              eventCount++
            }
          } else if (res.status == DayStatus.EXAM) {
            // All-day exam event if no specific slots
            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("UID:${currDate}-exam@academiccalendar")
            sb.appendLine("DTSTAMP:$nowStamp")
            sb.appendLine("DTSTART;VALUE=DATE:${dateFormatted}")
            sb.appendLine("DTEND;VALUE=DATE:${currDate.plusDays(1).format(DATE_ONLY_FORMATTER)}")
            sb.appendLine("SUMMARY:📝 Exam Day")
            if (res.note.isNotBlank()) {
              sb.appendLine("DESCRIPTION:${escapeIcsText(res.note)}")
            }
            sb.appendLine("STATUS:CONFIRMED")
            sb.appendLine("END:VEVENT")
            eventCount++
          }
        }
        DayStatus.HOLIDAY -> {
          val summary = if (res.note.isNotBlank()) "🎉 Holiday: ${res.note}" else "🎉 University Holiday"
          sb.appendLine("BEGIN:VEVENT")
          sb.appendLine("UID:${currDate}-holiday@academiccalendar")
          sb.appendLine("DTSTAMP:$nowStamp")
          sb.appendLine("DTSTART;VALUE=DATE:${dateFormatted}")
          sb.appendLine("DTEND;VALUE=DATE:${currDate.plusDays(1).format(DATE_ONLY_FORMATTER)}")
          sb.appendLine("SUMMARY:${escapeIcsText(summary)}")
          if (res.note.isNotBlank()) {
            sb.appendLine("DESCRIPTION:${escapeIcsText(res.note)}")
          }
          sb.appendLine("STATUS:CONFIRMED")
          sb.appendLine("END:VEVENT")
          eventCount++
        }
        DayStatus.OFF_DAY -> {
          // Off days don't need calendar events
        }
      }

      currDate = currDate.plusDays(1)
    }

    sb.appendLine("END:VCALENDAR")
    return sb.toString()
  }

  fun exportAndShareCalendar(
    context: Context,
    settings: AcademicSettings,
    resolveDay: (LocalDate) -> DayResolution
  ): Pair<Boolean, String> {
    return try {
      val icsContent = generateICalendarString(settings, resolveDay)
      val safeSemester = settings.semesterName.replace(Regex("[^a-zA-Z0-9]"), "_")
      val fileName = "Academic_Schedule_${safeSemester}.ics"
      val cacheFile = File(context.cacheDir, fileName)

      FileOutputStream(cacheFile).use { fos ->
        fos.write(icsContent.toByteArray(Charsets.UTF_8))
      }

      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        cacheFile
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/calendar"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, "${settings.semesterName} Academic Timetable")
        putExtra(
          Intent.EXTRA_TEXT,
          "Here is the iCalendar (.ics) export for ${settings.semesterName} (${settings.department}). Open this file to sync classes, exams, and holidays with your system calendar."
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      val chooser = Intent.createChooser(shareIntent, "Open or Sync Academic Calendar (.ics)").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      context.startActivity(chooser)
      Pair(true, "Exported $fileName! Choose your calendar app to sync. 🗓️")
    } catch (e: Exception) {
      // Fallback: copy content to clipboard
      try {
        val icsContent = generateICalendarString(settings, resolveDay)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Academic Calendar .ics", icsContent)
        clipboard.setPrimaryClip(clip)
        Pair(true, "iCalendar data copied to clipboard! (Fallback)")
      } catch (e2: Exception) {
        Pair(false, "Failed to export iCalendar: ${e.message}")
      }
    }
  }

  private fun escapeIcsText(text: String): String {
    return text
      .replace("\\", "\\\\")
      .replace(";", "\\;")
      .replace(",", "\\,")
      .replace("\n", "\\n")
      .replace("\r", "")
  }
}
