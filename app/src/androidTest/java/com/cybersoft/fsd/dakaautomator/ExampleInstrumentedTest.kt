package com.cybersoft.fsd.dakaautomator

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.stream.IntStream
import kotlin.reflect.KProperty1

private const val APP_PACKAGE = "com.cybersoft.had"

private const val LAUNCH_TIMEOUT = 5000L
private const val LONG_CLICK_TIMEOUT = 700L
private const val SHORT_CLICK_TIMEOUT = 400L
private const val SWIPE_STEPS = 20


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class ExampleInstrumentedTest {

    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    private val startBtn: UiObject by lazy {
        device.findObject(
            UiSelector().resourceId("${APP_PACKAGE}:id/btnOn").text("上班打卡")
        )
    }
    private val endBtn: UiObject by lazy {
        device.findObject(
            UiSelector().resourceId("${APP_PACKAGE}:id/btnOff").text("下班打卡")
        )
    }
    private val clockBtn: UiObject by lazy { device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/btnClock")) }
    private val positiveBtn: UiObject by lazy { device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/md_buttonDefaultPositive")) }
    private val okBtn: UiObject by lazy { device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/ok")) }
    private val blockInConfirmBtn: UiObject by lazy { device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/btnClockInConfirm")) }
    private val blockOutConfirmBtn: UiObject by lazy { device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/btnClockOutConfirm")) }
    private val clockRect: Rect by lazy {
        device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/time_picker")).bounds
    }
    private val h8Pt: Point by lazy {
        findHourPoint(clockRect, 8)
    }
    private val h18Pt: Point by lazy {
        findHourPoint(clockRect, 18)
    }
    private val m45Pt: Point by lazy {
        findMinutePoint(clockRect, 45)
    }
    private val m00Pt: Point by lazy {
        findMinutePoint(clockRect, 0)
    }
    private val m15Pt: Point by lazy {
        findMinutePoint(clockRect, 15)
    }
    private val has08Cond: SearchCondition<Boolean> by lazy {
        Until.hasObject(By.text("08").depth(0))
    }
    private val has18Cond: SearchCondition<Boolean> by lazy {
        Until.hasObject(By.text("18").depth(0))
    }
    private val hasOkCond: SearchCondition<Boolean> by lazy {
        Until.hasObject(By.text("OK").depth(0))
    }

    @Before
    fun startMainActivityFromHomeScreen() {
        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage = device.launcherPackageName
        assertThat(launcherPackage, notNullValue())
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT)

        // Launch the blueprint app
        val context: Context = getApplicationContext()
        val intent = context.packageManager
            .getLaunchIntentForPackage(APP_PACKAGE)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun daka() {
        device.findObject(UiSelector().resourceId("${APP_PACKAGE}:id/ivCalendar"))
            .click()
        device.wait(
            Until.hasObject(
                By.res(APP_PACKAGE, "${APP_PACKAGE}:id/calendarViewContent").depth(0)
            ), LONG_CLICK_TIMEOUT
        )
        IntStream.range(1, 32).forEach(this::dakaForADay)
    }

    private fun dakaForADay(day: Int) {
        // it is possible that date:2x~31 appears twice (last month)
        var daySelection =
            device.findObject(
                UiSelector().resourceId("${APP_PACKAGE}:id/tvDate").text("" + day).instance(1)
            )
        if (!daySelection.exists()) {
            daySelection = device.findObject(
                UiSelector().resourceId("${APP_PACKAGE}:id/tvDate").text("" + day).instance(0)
            )
        }
        if (daySelection.exists()) {
            daySelection.click()
            if (startBtn.exists() && startBtn.isClickable) {
                startBtn.clickAndWaitForNewWindow(LONG_CLICK_TIMEOUT)
                // find clock
                if (!clockBtn.exists()) {
                    // not working day
                    positiveBtn.clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)
                    return
                }
                // click clock
                clockBtn.clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)

                // click 8
                clickAtPoint(h8Pt)
                device.wait(has08Cond, SHORT_CLICK_TIMEOUT)
                // swipe from 45 to 00 randomly
                randomSwipe(m45Pt, m00Pt)

                // daka
                okBtn
                    .clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)
                blockInConfirmBtn
                    .click()
                device.wait(hasOkCond, SHORT_CLICK_TIMEOUT)
                // confirm OK
                positiveBtn
                    .clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)
                // back to calendar
                device.pressBack()
            }
            if (endBtn.exists() && endBtn.isClickable) {
                endBtn.clickAndWaitForNewWindow(LONG_CLICK_TIMEOUT)
                // find clock
                if (!clockBtn.exists()) {
                    // not working day
                    positiveBtn
                        .clickAndWaitForNewWindow(
                            SHORT_CLICK_TIMEOUT
                        )
                    return
                }
                // click clock
                clockBtn.clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)
                // click 18
                clickAtPoint(h18Pt)
                device.wait(has18Cond, SHORT_CLICK_TIMEOUT)
                // swipe from 15 to 00 randomly
                randomSwipe(m15Pt, m00Pt)
                // daka
                okBtn
                    .clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)
                blockOutConfirmBtn
                    .click()
                device.wait(hasOkCond, SHORT_CLICK_TIMEOUT)
                // confirm OK
                positiveBtn
                    .clickAndWaitForNewWindow(SHORT_CLICK_TIMEOUT)
                // back to calendar
                device.pressBack()
            }
        }
    }

    private fun clickAtPoint(p: Point) {
        device.click(p.x, p.y)
    }

    private fun randomSwipe(p0: Point, p1: Point) {
        val rndDelta = Random().nextInt(SWIPE_STEPS + 1)
        device.swipe(
            p0.x,
            p0.y,
            pointBetween(p0, p1, Point::x, rndDelta),
            pointBetween(p0, p1, Point::y, rndDelta),
            SWIPE_STEPS
        )
    }

    private fun pointBetween(
        start: Point,
        end: Point,
        prop: KProperty1<Point, Int>,
        delta: Int
    ) = prop.get(start) + ((prop.get(end) - prop.get(start)) * delta / SWIPE_STEPS)


}