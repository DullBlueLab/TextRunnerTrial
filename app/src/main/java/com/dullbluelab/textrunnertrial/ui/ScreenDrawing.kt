package com.dullbluelab.textrunnertrial.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullbluelab.textrunnertrial.RunnerScreen
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.action.Drawing

@Composable
fun ScreenDrawing(
    viewModel: RunnerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val status = viewModel.status()

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    if (viewModel.enableAction() && viewModel.status().flagTapAction)
                        viewModel.onTap(offset)
                }
            )
        }
    ) {
        if (status.canvasSize.width != size.width || status.canvasSize.height != size.height) {
            status.canvasSize = size
            if (viewModel.enableAction()) viewModel.canvasChanged()
        }

        uiState.drawingQueue.list().forEach() { item ->
            when (item.type()) {
                Drawing.Type.ARC -> {
                    val i = item as Drawing.Arc
                    drawArc( i.color, i.startAngle, i.sweepAngle, i.useCenter, i.topLeft,
                        i.size, i.alpha )
                }
                Drawing.Type.CIRCLE -> {
                    val i = item as Drawing.Circle
                    drawCircle( i.color, i.radius, i.center, i.alpha )
                }
                Drawing.Type.IMAGES -> {
                    val i = item as Drawing.Images
                    drawImage( i.image, i.srcOffset, i.srcSize, i.dstOffset, i.dstSize, i.alpha )
                }
                Drawing.Type.LINE -> {
                    val i = item as Drawing.Line
                    drawLine( i.color, i.start, i.end, i.strokeWidth )
                }
                Drawing.Type.POINTS -> {
                    val i = item as Drawing.Points
                    drawPoints( i.points, i.pointMode, i.color, i.strokeWidth,
                        i.cap, i.pathEffect, i.alpha )
                }
                Drawing.Type.RECT -> {
                    val i = item as Drawing.Rect
                    drawRect( i.color, i.topLeft, i.size, i.alpha )
                }
            }
        }
    }
}