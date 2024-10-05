package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import org.andengine.util.modifier.ease.IEaseFunction

class StoryboardFlipVCommand(
    easeFunction: IEaseFunction,
    startTime: Float,
    endTime: Float,
    startValue: Boolean,
    endValue: Boolean
) : StoryboardCommand<Boolean>(easeFunction, startTime, endTime, startValue, endValue) {
    override fun applyInitialValue(textureQuad: TextureQuad) {
        textureQuad.flipVertical = startValue
    }

    override fun applyValue(textureQuad: TextureQuad, percentage: Float) {
        textureQuad.flipVertical = if (percentage >= endTime) endValue else startValue
    }
}