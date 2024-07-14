package com.dullbluelab.textrunnertrial.action.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class TextSetting {
    var color: Color = Color.Black
    private var fontSize: Int = 14
    private var fontWeight: Int = 400
    private var fontStyle: FontStyle = FontStyle.Normal
    private var fontFamily: FontFamily = FontFamily.Default
    private var textDecoration: TextDecoration = TextDecoration.None
    private var textAlign: TextAlign = TextAlign.Start

    //var overflow: TextOverflow = TextOverflow.Clip
    //var maxLines: Int = 1
    fun buildStyle(): TextStyle {
        return TextStyle(
            color = color,
            fontSize = fontSize.sp,
            fontWeight = FontWeight(fontWeight),
            fontStyle = fontStyle,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            textAlign = textAlign,
        )
    }

    fun setStyle(size: Int, weight: Int, style: String, family: String, decoration: String) {
        fontSize = if(size > 0) size else fontSize
        fontWeight = if (weight > 0) weight else fontWeight

        fontStyle = when (style) {
            "normal" -> FontStyle.Normal
            "italic" -> FontStyle.Italic
            else -> fontStyle
        }

        fontFamily = when (family) {
            "default" -> FontFamily.Default
            "sans" -> FontFamily.SansSerif
            "serif" -> FontFamily.Serif
            else -> fontFamily
        }

        textDecoration = when (decoration) {
            "none" -> TextDecoration.None
            "underline" -> TextDecoration.Underline
            "lineThrough" -> TextDecoration.LineThrough
            else -> textDecoration
        }
    }
}