package com.cybersoft.fsd.dakaautomator

import android.graphics.Point
import android.graphics.Rect
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

const val outerHourCoefficient = 0.69987
const val innerHourCoefficient = 0.50336
const val minuteCoefficient = 0.68371

fun findHourPoint(rect: Rect, hour: Int): Point {
    val radius = calcHourRadius(rect, hour)
    val degree = calcDegreeForHour(hour)
    val point = polarToCartesian(radius, degree)
    applyOffset(point, rect)
    return point
}

private fun calcDegreeForHour(hour: Int) = (hour * (360 / 12) + 270) % 360


private fun calcHourRadius(rect: Rect, hour: Int): Double {
    val coefficient = if (hour == 0 || hour > 12) outerHourCoefficient else innerHourCoefficient
    return calcBaseRadius(rect) * coefficient
}

fun findMinutePoint(rect: Rect, minute: Int): Point {
    val radius = calcMinuteRadius(rect)
    val degree = calcDegreeForMinute(minute)
    val point = polarToCartesian(radius, degree)
    applyOffset(point, rect)
    return point
}

private fun applyOffset(point: Point, rect: Rect) {
    val xOffset = (rect.left + rect.right) / 2
    val yOffset = (rect.top + rect.bottom) / 2
    point.offset(xOffset, yOffset)
}

private fun calcDegreeForMinute(minute: Int) =
    (minute * (360 / 60) + 270) % 360


private fun calcMinuteRadius(rect: Rect) =
    calcBaseRadius(rect) * minuteCoefficient


private fun calcBaseRadius(rect: Rect) =
    rect.width() / 2.0


private fun polarToCartesian(radius: Double, degree: Int): Point {
    val radian = Math.toRadians(degree.toDouble())
    return Point((radius * cos(radian)).roundToInt(), (radius * sin(radian)).roundToInt())
}




