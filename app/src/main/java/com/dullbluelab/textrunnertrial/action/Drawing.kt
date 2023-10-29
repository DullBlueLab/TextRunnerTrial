package com.dullbluelab.textrunnertrial.action

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

class Drawing {

    enum class Type {
        ARC, CIRCLE, IMAGES, LINE, POINTS, RECT,
    }

    open class Item(
        private val type: Type
    ) {
        var brush: Brush? = null
        var style: DrawStyle? = null
        var colorFilter: ColorFilter? = null
        var blendMode: BlendMode? = null

        fun type() = type
    }

    class Arc(
        var color: Color,
        var startAngle: Float,
        var sweepAngle: Float,
        var useCenter: Boolean,
        var topLeft: Offset,
        var size: Size,
        var alpha: Float = 1.0f
    ) : Item(Type.ARC)

    class Circle(
        var color: Color,
        var radius: Float,
        var center: Offset,
        var alpha: Float = 1.0f,
    ) : Item(Type.CIRCLE) {

    }

    class Images(
        var image: ImageBitmap,
        var srcOffset: IntOffset,
        var srcSize: IntSize,
        var dstOffset: IntOffset,
        var dstSize: IntSize,
        var alpha: Float = 1.0f,
    ) : Item(Type.IMAGES) {

    }

    class Line(
        var color: Color,
        var start: Offset,
        var end: Offset,
        var strokeWidth: Float,
        var cap: StrokeCap,
        var pathEffect: PathEffect?,
        var alpha: Float = 1.0f
    ) : Item(Type.LINE) {

    }

    class Points(
        var points: List<Offset>,
        var pointMode: PointMode,
        var color: Color,
        var strokeWidth: Float,
        var cap: StrokeCap,
        var pathEffect: PathEffect?,
        var alpha: Float = 1.0f
    ) : Item(Type.POINTS) {

    }

    class Rect(
        var color: Color,
        var topLeft: Offset,
        var size: Size,
        var alpha: Float = 1.0f
    ) : Item(Type.RECT) {

    }

    data class Option(
        var color: Color,
        var strokeWidth: Float,
        var cap: StrokeCap,
        var pathEffect: PathEffect?,
        var alpha: Float
    )

    class Lists {
        private val list: MutableList<Item> = mutableListOf()

        fun list() = list

        fun drawCircle(color: Color, radius: Float, center: Offset, alpha: Float) {
            val item = Circle(color, radius, center, alpha)
            list.add(item)
        }

        fun drawLine(start: Offset, end: Offset, option: Option) {
            val item = Line(option.color, start, end,
                option.strokeWidth, option.cap, option.pathEffect, option.alpha)
            list.add(item)
        }

        fun drawRect(color: Color, topLeft: Offset, size: Size, alpha: Float) {
            val item = Rect(color, topLeft, size, alpha)
            list.add(item)
        }

        fun clear() {
            list.clear()
        }
    }
}