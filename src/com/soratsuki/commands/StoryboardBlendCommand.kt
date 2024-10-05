package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import org.andengine.util.modifier.ease.IEaseFunction

class StoryboardBlendCommand(
    easeFunction: IEaseFunction,
    startTime: Float,
    endTime: Float,
    startValue: Boolean,
    endValue: Boolean
) : StoryboardCommand<Boolean>(easeFunction, startTime, endTime, startValue, endValue) {
    override fun applyInitialValue(textureQuad: TextureQuad) {
        textureQuad.blendAdditive = startValue
    }

    override fun applyValue(textureQuad: TextureQuad, percentage: Float) {
        textureQuad.blendAdditive = if (percentage >= endTime) endValue else startValue
    }
}