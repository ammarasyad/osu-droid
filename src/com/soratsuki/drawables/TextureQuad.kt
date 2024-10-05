package com.soratsuki.drawables

import android.graphics.PointF
import com.edlplan.framework.math.Anchor
import com.edlplan.framework.math.Vec2
import com.rian.osu.math.times
import com.soratsuki.texture.StoryboardTexturePool
import org.andengine.opengl.texture.region.TextureRegion
import org.andengine.util.color.Color
import org.andengine.util.color.ColorUtils
import ru.nsu.ccfit.zuev.osu.Config
import ru.nsu.ccfit.zuev.osu.Utils
import kotlin.math.cos
import kotlin.math.sin

class TextureQuad(textureInfo: StoryboardTexturePool.TextureInfo) {
    companion object {
        const val SIZE_PER_QUAD = 20
    }

    val size = textureInfo.size.copy()
    val position = Vec2()

    var alpha = 1f

    var anchor: Anchor = Anchor.Center
    var scale: Vec2? = null
    var rotation = 0f
    var accentColor: Color? = null

    var u1 = textureInfo.u1
    var u2 = textureInfo.u2
    var v1 = textureInfo.v1
    var v2 = textureInfo.v2

    var flipHorizontal = false
    var flipVertical = false

    var blendAdditive = false

//    var texture: TextureRegion? = null
//        set(value) {
//            field = value
//            size.set(value?.width ?: 0f, value?.height ?: 0f)
//            u1 = value?.u ?: 0f
//            u2 = value?.u2 ?: 0f
//            v1 = value?.v ?: 0f
//            v2 = value?.v2 ?: 0f
//        }

    fun enableScale() {
        if (scale == null) {
            scale = Vec2(1f, 1f)
        }
    }

    fun enableColor() {
        if (accentColor == null) {
            accentColor = Color(1f, 1f, 1f, 1f)
        }
    }

    // Screen X = [0, Config.getRES_WIDTH()]
    // Screen Y = [0, Config.getRES_HEIGHT()]
    fun writeToBuffer(result: FloatArray, offset: Int) {
        var o = offset

        val u1 = if (flipHorizontal) this.u2 else this.u1
        val u2 = if (flipHorizontal) this.u1 else this.u2
        val v1 = if (flipVertical) this.v2 else this.v1
        val v2 = if (flipVertical) this.v1 else this.v2

        val left   = -size.x * anchor.x() * (scale?.x ?: 1f)
        val right = (size.x + left) * (scale?.x ?: 1f)
        val top    = -size.y * anchor.y() * (scale?.y ?: 1f)
        val bottom = (size.y + top) * (scale?.y ?: 1f)

//        val left = 0f * (scale?.x ?: 1f)
//        val right = size.x * (scale?.x ?: 1f)
//        val top = 0f * (scale?.y ?: 1f)
//        val bottom = size.y * (scale?.y ?: 1f)

        val color = accentColor?.apply {
            val alphaValue = this@TextureQuad.alpha
            Color(red, green, blue, alpha * alphaValue)
        } ?: Color(1f, 1f, 1f, alpha)

        val sin = sin(rotation)
        val cos = cos(rotation)

        val packedColor = ColorUtils.convertRGBAToABGRPackedFloat(color.red, color.green, color.blue, alpha)

        result[o++] = left * cos - top * sin        + position.x
        result[o++] = left * sin + top * cos        + position.y
        result[o++] = packedColor
        result[o++] = u1
        result[o++] = v1

        result[o++] = right * cos - top * sin       + position.x
        result[o++] = right * sin + top * cos       + position.y
        result[o++] = packedColor
        result[o++] = u2
        result[o++] = v1

        result[o++] = left * cos - bottom * sin     + position.x
        result[o++] = left * sin + bottom * cos     + position.y
        result[o++] = packedColor
        result[o++] = u1
        result[o++] = v2

        result[o++] = right * cos - bottom * sin    + position.x
        result[o++] = right * sin + bottom * cos    + position.y
        result[o++] = packedColor
        result[o++] = u2
        result[o] = v2
    }
}