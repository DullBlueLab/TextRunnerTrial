package com.dullbluelab.textrunnertrial.objects

import android.graphics.Bitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.dullbluelab.textrunnertrial.data.LibraryRepository
import com.dullbluelab.textrunnertrial.logic.Syntax
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

class RunImage : RunObject(Type.IMAGES) {

    private var sourceName: String = ""
    var bitmap: Bitmap? = null
    var loadedFlag: Boolean = false
    var cropOffset: IntOffset = IntOffset(0, 0)
    var cropSize: IntSize = IntSize(0, 0)

/*    constructor(block: CodeBlock.Common) : this() {
        sourceName = block.strings()
    }
*/

    override fun toRunString(): RunString = RunString("image:${sourceName}")
    override fun isRunValue(): Boolean = false
    override fun typeWord(): String = RW.IMAGE

    override fun execute(method: String, arguments: MutableList<RunObject>) : RunObject? {
        var result: RunObject? = super.execute(method, arguments)
        if (result != null) return result

        if (arguments.size == 0) {
            result = when (method) {
                MW.WIDTH -> if (bitmap != null) RunInt(bitmap!!.width) else RunInt(0)
                MW.HEIGHT -> if (bitmap != null) RunInt(bitmap!!.height) else RunInt(0)
                MW.IS_LOADED -> isLoaded()
                else -> null
            }
        }
        else if (arguments.size == 1) {
            val dst = arguments[0]
            result = when (method) {
                RW.INIT -> setSource(dst)
                else -> null
            }
        }
        else if (arguments.size == 4 &&
            arguments[0].isRunValue() && arguments[1].isRunValue() &&
            arguments[2].isRunValue() && arguments[3].isRunValue()) {
            val left = arguments[0] as RunValue
            val top = arguments[1] as RunValue
            val right = arguments[2] as RunValue
            val bottom = arguments[3] as RunValue
            result = when (method) {
                MW.CROP -> crop(left, top, right, bottom)
                else -> null
            }
        }
        return result
    }

    private fun setSource(dst: RunObject): RunObject {
        if (dst.type == Type.STRINGS) {
            sourceName = (dst as RunString).valueString()
        }
        return this
    }

    private fun isLoaded(): RunObject {
        return RunBoolean(loadedFlag)
    }

    fun load(repository: LibraryRepository) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            if (sourceName.isNotEmpty()) {
                bitmap = repository.loadImage(sourceName)
                loadedFlag = true
                initCropState()
            }
        }
    }

    private fun initCropState() {
        if (bitmap != null) {
            cropOffset = IntOffset(0, 0)
            cropSize = IntSize(bitmap!!.width, bitmap!!.height)
        }
    }

    private fun crop(left: RunValue, top: RunValue, right: RunValue, bottom: RunValue): RunObject? {
        if (bitmap == null) return null
        cropOffset = IntOffset(left.valueInt(), top.valueInt())
        cropSize = IntSize(right.valueInt() - left.valueInt(),
            bottom.valueInt() - top.valueInt())
        return this
    }
}
