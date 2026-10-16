package com.example.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

enum class DayStatus(val label: String, val emoji: String) {
  CLASS_DAY("Class Day", "📚"),
  OFF_DAY("Off Day", "🏖️"),
  HOLIDAY("Holiday", "🎉"),
  EXAM("Exam Day", "📝")
}

data class ClassItem(
  val id: String = UUID.randomUUID().toString(),
  val subject: String = "",
  val startTime: String = "09:00",
  val endTime: String = "10:30",
  val room: String = "",
  val instructor: String = ""
)

data class DaySchedule(
  val dayOfWeek: Int, // 1 = Monday ... 7 = Sunday (matches java.time.DayOfWeek)
  val isClassDay: Boolean = true,
  val classes: List<ClassItem> = emptyList()
)

data class DayOverride(
  val date: String, // "YYYY-MM-DD"
  val status: DayStatus,
  val classes: List<ClassItem> = emptyList(),
  val note: String = ""
)

data class AcademicSettings(
  val department: String = "Computer Science & Engineering",
  val semesterName: String = "Fall 2026",
  val term: String = "Term 1",
  val startYear: Int = 2026,
  val startMonth: Int = 9,  // September
  val endYear: Int = 2027,
  val endMonth: Int = 1,    // January
  val reminderEnabled: Boolean = false,
  val reminderHour: Int = 20, // 8:00 PM
  val reminderMinute: Int = 0
)

enum class QuickPeriodType(
  val displayName: String,
  val defaultStatus: DayStatus,
  val emoji: String,
  val defaultSubject: String
) {
  MIDTERM("Midterm Exams", DayStatus.EXAM, "📝", "Midterm Examination"),
  FINAL_EXAM("Final Exams", DayStatus.EXAM, "🎯", "Final Examination"),
  HOLIDAY("Holiday Break", DayStatus.HOLIDAY, "🏖️", ""),
  STUDY_RECESS("Study Recess / Prep", DayStatus.OFF_DAY, "☕", "")
}

data class DayResolution(
  val date: LocalDate,
  val dayOfWeek: DayOfWeek,
  val status: DayStatus,
  val classes: List<ClassItem>,
  val note: String,
  val isOverridden: Boolean
)
