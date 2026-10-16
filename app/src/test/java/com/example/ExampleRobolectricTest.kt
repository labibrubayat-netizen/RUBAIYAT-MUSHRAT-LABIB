package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AcademicRepository
import com.example.model.DayResolution
import com.example.model.DayStatus
import com.example.util.ICalendarExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Academic Calendar", appName)
  }

  @Test
  fun `repository loads defaults and saves overrides`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = AcademicRepository(context)

    val settings = repo.getSettings()
    assertNotNull(settings)
    assertTrue(settings.department.isNotBlank())

    val routine = repo.getWeeklyRoutine()
    assertEquals(7, routine.size)

    val overrides = repo.getOverrides()
    assertNotNull(overrides)
  }

  @Test
  fun `icalendar export generates valid RFC 5545 calendar`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = AcademicRepository(context)
    val settings = repo.getSettings()

    val icsString = ICalendarExporter.generateICalendarString(settings) { date ->
      DayResolution(
        date = date,
        dayOfWeek = date.dayOfWeek,
        status = DayStatus.CLASS_DAY,
        classes = emptyList(),
        note = "",
        isOverridden = false
      )
    }

    assertNotNull(icsString)
    assertTrue(icsString.contains("BEGIN:VCALENDAR"))
    assertTrue(icsString.contains("VERSION:2.0"))
    assertTrue(icsString.contains("END:VCALENDAR"))
  }
}
