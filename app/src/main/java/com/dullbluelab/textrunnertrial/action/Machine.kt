package com.dullbluelab.textrunnertrial.action

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.logic.Syntax
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
    private val voids = Objects.Voids()

    companion object {
        const val NAME = "\$m"
        private val MW = Syntax.Method.Word
    }

    fun execute(method: String, args:MutableList<Objects.Common>): Objects.Common {
        val result: Objects.Common = when (method) {
            MW.PRINT -> printf(args)

            MW.CANVAS_WIDTH -> canvasWidth()
            MW.CANVAS_HEIGHT -> canvasHeight()

            MW.DRAW_CIRCLE -> drawCircle(args)
            MW.DRAW_LINE -> drawLine(args)
            MW.DRAW_RECT -> drawRect(args)
            MW.FILL_CANVAS -> fillCanvas(args)

            MW.DRAW_UP -> drawUp()
            MW.NEW_DRAWING -> newDrawing()
            MW.CHANGE_COLOR -> changeColor(args)
            MW.CHANGE_STROKE -> changeStroke(args)
            MW.CHANGE_ALPHA -> changeAlpha(args)

            MW.RANDOM -> getRandom(args)
            MW.PI -> Objects.Doubles(Math.PI)
            MW.SET_TIMER -> setTimer(args)
            MW.CANCEL_TIMER -> cancelTimer()
            MW.TAP_ACTION -> tapAction(args)

            else -> throw Errors.Syntax(Errors.Key.ILLEGAL_METHOD)
        }
        return result
    }

    private fun printf(args: MutableList<Objects.Common>): Objects.Common {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.PRINT)

        val strings = args[0].toStrings()
        vm.console().append(strings.valueString())
        return strings
    }

    private fun canvasWidth(): Objects.Doubles {
        return Objects.Doubles(status.canvasSize.width.toDouble())
    }

    private fun canvasHeight(): Objects.Doubles {
        return Objects.Doubles(status.canvasSize.height.toDouble())
    }

    private fun drawCircle(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 3) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_CIRCLE)

        val radius = if (args[0].isValues())
                (args[0] as Objects.Values).valueDouble().toFloat() else null
        val centerLeft = if (args[1].isValues())
                (args[1] as Objects.Values).valueDouble().toFloat() else null
        val centerTop = if (args[2].isValues())
            (args[2] as Objects.Values).valueDouble().toFloat() else null

        if (radius == null || centerLeft == null || centerTop == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_CIRCLE)

        val offset = Offset(centerLeft, centerTop)
        drawingQueue.drawCircle(currents.color, radius, offset, currents.alpha)

        return voids
    }

    private fun drawLine(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 4) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_LINE)

        val startLeft = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble().toFloat() else null
        val startTop = if (args[1].isValues())
            (args[1] as Objects.Values).valueDouble().toFloat() else null
        val endLeft = if (args[2].isValues())
            (args[2] as Objects.Values).valueDouble().toFloat() else null
        val endTop = if (args[3].isValues())
            (args[3] as Objects.Values).valueDouble().toFloat() else null

        if (startLeft == null || startTop == null || endLeft == null || endTop == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_LINE)

        val start = Offset(startLeft, startTop)
        val end = Offset(endLeft, endTop)
        drawingQueue.drawLine(start, end, currents)
        return voids
    }

    private fun drawRect(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 4) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_RECT)

        val left = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble().toFloat() else null
        val top = if (args[1].isValues())
            (args[1] as Objects.Values).valueDouble().toFloat() else null
        val width = if (args[2].isValues())
            (args[2] as Objects.Values).valueDouble().toFloat() else null
        val height = if (args[3].isValues())
            (args[3] as Objects.Values).valueDouble().toFloat() else null

        if (left == null || top == null || width == null || height == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.DRAW_RECT)

        val leftTop = Offset(left, top)
        val size = Size(width, height)
        drawingQueue.drawRect(currents.color, leftTop, size, currents.alpha)
        return voids
    }

    private fun fillCanvas(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 3) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.FILL_CANVAS)

        val red = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble().toFloat() else null
        val green = if (args[1].isValues())
            (args[1] as Objects.Values).valueDouble().toFloat() else null
        val blue = if (args[2].isValues())
            (args[2] as Objects.Values).valueDouble().toFloat() else null

        if (red == null || green == null || blue == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.FILL_CANVAS)

        val color = Color(red, green, blue)
        val leftTop = Offset(0f, 0f)
        val size = status.canvasSize

        drawingQueue.drawRect(color, leftTop, size, currents.alpha)
        return voids
    }

    private fun drawUp() : Objects.Voids {
        vm.updateDrawing(drawingQueue)
        return newDrawing()
    }

    private fun newDrawing() : Objects.Voids {
        drawingQueue = Drawing.Lists()
        return voids
    }

    private fun changeColor(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 3) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_COLOR)

        val red = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble().toFloat() else null
        val green = if (args[1].isValues())
            (args[1] as Objects.Values).valueDouble().toFloat() else null
        val blue = if (args[2].isValues())
            (args[2] as Objects.Values).valueDouble().toFloat() else null

        if (red == null || green == null || blue == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_COLOR)

        currents.color = Color(red, green, blue)
        return voids
    }

    private fun changeStroke(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_STROKE)

        val width = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble().toFloat() else null

        if (width == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_STROKE)

        currents.strokeWidth = width
        return voids
    }

    private fun changeAlpha(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_ALPHA)

        val alpha = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble().toFloat() else null

        if (alpha == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.CHANGE_ALPHA)

        currents.alpha = alpha
        return voids
    }

    private fun getRandom(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.RANDOM)

        val max = if (args[0].isValues())
            (args[0] as Objects.Values).valueDouble() else null

        if (max == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.RANDOM)

        return Objects.Doubles(random() * max)
    }

    private fun setTimer(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.SET_TIMER)

        var delay = if (args[0].isValues())
            (args[0] as Objects.Values).valueInt().toLong() else null

        if (delay == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.SET_TIMER)

        if (delay < vm.setting().timerLimit) delay = vm.setting().timerLimit
        status.timerCount = delay
        return voids
    }

    private fun cancelTimer() : Objects.Common {
        vm.cancelTimer()
        return voids
    }

    private fun tapAction(args: MutableList<Objects.Common>) : Objects.Common {
        if (args.size != 1) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.TAP_ACTION)

        val flag = if (args[0].isValues())
            (args[0] as Objects.Values).valueBoolean() else null

        if (flag == null) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, MW.TAP_ACTION)
        status.flagTapAction = flag
        return voids
    }
}
