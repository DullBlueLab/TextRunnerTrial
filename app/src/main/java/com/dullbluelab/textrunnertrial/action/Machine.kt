package com.dullbluelab.textrunnertrial.action

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.logic.Syntax
import com.dullbluelab.textrunnertrial.objects.*
import java.lang.Math.random

class Machine(
    private val vm: RunnerViewModel
) {
    private var drawingQueue: Drawing.Lists = Drawing.Lists()

    private val currents: Drawing.Option = Drawing.Option(
        color = Color(0.5f, 0.5f, 0.5f),
        strokeWidth = Stroke.HairlineWidth,
        cap = StrokeCap.Square,
        pathEffect = null,
        alpha = 1f
    )

    private val status = vm.status()
    private val voids = RunVoid()

    companion object {
        const val NAME = "\$m"
        private val MW = Syntax.Method.Word
    }

    fun execute(method: String, args:MutableList<RunObject>): RunObject {
        val result: RunObject = when (method) {
            MW.PRINT -> printf(args)

            MW.CANVAS_WIDTH -> canvasWidth()
            MW.CANVAS_HEIGHT -> canvasHeight()

            MW.DRAW_CIRCLE -> drawCircle(args)
            MW.DRAW_LINE -> drawLine(args)
            MW.DRAW_RECT -> drawRect(args)
            MW.DRAW_IMAGE -> drawImage(args)
            MW.FILL_CANVAS -> fillCanvas(args)

            MW.DRAW_UP -> drawUp()
            MW.NEW_DRAWING -> newDrawing()
            MW.CHANGE_COLOR -> changeColor(args)
            MW.CHANGE_STROKE -> changeStroke(args)
            MW.CHANGE_ALPHA -> changeAlpha(args)

            MW.RANDOM -> getRandom(args)
            MW.PI -> RunDouble(Math.PI)
            MW.SET_TIMER -> setTimer(args)
            MW.CANCEL_TIMER -> cancelTimer()
            MW.TAP_ACTION -> tapAction(args)

            else -> throw Errors.Syntax(Errors.Key.ILLEGAL_METHOD)
        }
        return result
    }

    private fun printf(args: MutableList<RunObject>): RunObject {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.PRINT)

        val strings = args[0].toRunString()
        vm.console().append(strings.valueString())
        return strings
    }

    private fun canvasWidth(): RunDouble {
        return RunDouble(status.canvasSize.width.toDouble())
    }

    private fun canvasHeight(): RunDouble {
        return RunDouble(status.canvasSize.height.toDouble())
    }

    private fun drawCircle(args: MutableList<RunObject>) : RunObject {
        if (args.size != 3) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_CIRCLE)

        val radius = if (args[0].isRunValue())
                (args[0] as RunValue).valueDouble().toFloat() else null
        val centerLeft = if (args[1].isRunValue())
                (args[1] as RunValue).valueDouble().toFloat() else null
        val centerTop = if (args[2].isRunValue())
            (args[2] as RunValue).valueDouble().toFloat() else null

        if (radius == null || centerLeft == null || centerTop == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_CIRCLE)

        val offset = Offset(centerLeft, centerTop)
        drawingQueue.drawCircle(currents.color, radius, offset, currents.alpha)

        return voids
    }

    private fun drawLine(args: MutableList<RunObject>) : RunObject {
        if (args.size != 4) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_LINE)

        val startLeft = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble().toFloat() else null
        val startTop = if (args[1].isRunValue())
            (args[1] as RunValue).valueDouble().toFloat() else null
        val endLeft = if (args[2].isRunValue())
            (args[2] as RunValue).valueDouble().toFloat() else null
        val endTop = if (args[3].isRunValue())
            (args[3] as RunValue).valueDouble().toFloat() else null

        if (startLeft == null || startTop == null || endLeft == null || endTop == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_LINE)

        val start = Offset(startLeft, startTop)
        val end = Offset(endLeft, endTop)
        drawingQueue.drawLine(start, end, currents)
        return voids
    }

    private fun drawRect(args: MutableList<RunObject>) : RunObject {
        if (args.size != 4) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_RECT)

        val left = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble().toFloat() else null
        val top = if (args[1].isRunValue())
            (args[1] as RunValue).valueDouble().toFloat() else null
        val width = if (args[2].isRunValue())
            (args[2] as RunValue).valueDouble().toFloat() else null
        val height = if (args[3].isRunValue())
            (args[3] as RunValue).valueDouble().toFloat() else null

        if (left == null || top == null || width == null || height == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_RECT)

        val leftTop = Offset(left, top)
        val size = Size(width, height)
        drawingQueue.drawRect(currents.color, leftTop, size, currents.alpha)
        return voids
    }

    private fun drawImage(args: MutableList<RunObject>) : RunObject {
        if (args.size != 5) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_IMAGE)
        var bitmap: ImageBitmap? = null
        var cropOffset: IntOffset? = null
        var cropSize: IntSize? = null

        if (args[0].type == RunObject.Type.IMAGES) {
            val images = args[0] as RunImage
            if (! images.loadedFlag) return voids

            bitmap = images.bitmap?.asImageBitmap()
            cropOffset = images.cropOffset
            cropSize = images.cropSize
        }
        val top = if (args[1].isRunValue())
            (args[1] as RunValue).valueInt() else null
        val left = if (args[2].isRunValue())
            (args[2] as RunValue).valueInt() else null
        val width = if (args[3].isRunValue())
            (args[3] as RunValue).valueInt() else null
        val height = if (args[4].isRunValue())
            (args[4] as RunValue).valueInt() else null

        if (bitmap == null || top == null || left == null || width == null || height == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_IMAGE)

        val leftTop = IntOffset(left, top)
        val size = IntSize(width, height)
        drawingQueue.drawImage(bitmap, cropOffset!!, cropSize!!, leftTop, size, currents)
        return voids
    }

    private fun fillCanvas(args: MutableList<RunObject>) : RunObject {
        if (args.size != 3) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.FILL_CANVAS)

        val red = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble().toFloat() else null
        val green = if (args[1].isRunValue())
            (args[1] as RunValue).valueDouble().toFloat() else null
        val blue = if (args[2].isRunValue())
            (args[2] as RunValue).valueDouble().toFloat() else null

        if (red == null || green == null || blue == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.FILL_CANVAS)

        val color = Color(red, green, blue)
        val leftTop = Offset(0f, 0f)
        val size = status.canvasSize

        drawingQueue.drawRect(color, leftTop, size, currents.alpha)
        return voids
    }

    private fun drawUp() : RunVoid {
        vm.updateDrawing(drawingQueue)
        return newDrawing()
    }

    private fun newDrawing() : RunVoid {
        drawingQueue = Drawing.Lists()
        return voids
    }

    private fun changeColor(args: MutableList<RunObject>) : RunObject {
        if (args.size != 3) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_COLOR)

        val red = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble().toFloat() else null
        val green = if (args[1].isRunValue())
            (args[1] as RunValue).valueDouble().toFloat() else null
        val blue = if (args[2].isRunValue())
            (args[2] as RunValue).valueDouble().toFloat() else null

        if (red == null || green == null || blue == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_COLOR)

        currents.color = Color(red, green, blue)
        return voids
    }

    private fun changeStroke(args: MutableList<RunObject>) : RunObject {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_STROKE)

        val width = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble().toFloat() else null

        if (width == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_STROKE)

        currents.strokeWidth = width
        return voids
    }

    private fun changeAlpha(args: MutableList<RunObject>) : RunObject {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_ALPHA)

        val alpha = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble().toFloat() else null

        if (alpha == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_ALPHA)

        currents.alpha = alpha
        return voids
    }

    private fun getRandom(args: MutableList<RunObject>) : RunObject {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.RANDOM)

        val max = if (args[0].isRunValue())
            (args[0] as RunValue).valueDouble() else null

        if (max == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.RANDOM)

        return RunDouble(random() * max)
    }

    private fun setTimer(args: MutableList<RunObject>) : RunObject {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.SET_TIMER)

        var delay = if (args[0].isRunValue())
            (args[0] as RunValue).valueInt().toLong() else null

        if (delay == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.SET_TIMER)

        if (delay < vm.setting.value.timerLimit) delay = vm.setting.value.timerLimit
        status.timerCount = delay
        return voids
    }

    private fun cancelTimer() : RunObject {
        vm.cancelTimer()
        return voids
    }

    private fun tapAction(args: MutableList<RunObject>) : RunObject {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.TAP_ACTION)

        val flag = if (args[0].isRunValue())
            (args[0] as RunValue).valueBoolean() else null

        if (flag == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.TAP_ACTION)
        status.flagTapAction = flag
        return voids
    }
}
