package com.soratsuki.sprites

import com.edlplan.framework.math.Anchor
import com.soratsuki.commands.StoryboardCommandGroup
import com.soratsuki.commands.StoryboardLoopingGroup
import com.soratsuki.drawables.TextureQuad
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.texture.StoryboardTexturePool
import kotlin.math.min

abstract class StoryboardSprite(
    val layer: StoryboardLayer,
    origin: Anchor,
    val spriteFilename: String,
    startX: Float,
    startY: Float,
    textureInfo: StoryboardTexturePool.TextureInfo
) {
    val commandGroup = StoryboardCommandGroup()
    val textureQuad = TextureQuad(textureInfo)
    protected var loopingGroup: StoryboardLoopingGroup? = null
    protected var origin
        get() = textureQuad.anchor
        set(value) {
            textureQuad.anchor = value
        }

    init {
        this.origin = origin
        textureQuad.position.x = startX
        textureQuad.position.y = startY
    }

    fun addLoopingGroup(loopStartTime: Double, repeatCount: Int): StoryboardLoopingGroup {
        loopingGroup = StoryboardLoopingGroup(loopStartTime, repeatCount)
        return loopingGroup!!
    }

    fun clear() {
        commandGroup.clear()
    }

    abstract fun update(time: Double)

    fun shouldDraw(time: Double): Boolean {
        @Suppress("ConvertTwoComparisonsToRangeCheck") // prevent ClosedFloatingPointRange creation
        return if (loopingGroup == null) {
            time >= commandGroup.startTime && time <= commandGroup.endTime
        } else {
            val minStartTime = min(commandGroup.startTime, loopingGroup!!.startTime)
            val maxEndTime = min(commandGroup.endTime, loopingGroup!!.endTime)
            time >= minStartTime && time <= maxEndTime
        }
    }

}
