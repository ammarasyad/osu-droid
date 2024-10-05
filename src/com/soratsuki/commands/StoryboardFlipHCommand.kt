package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import org.andengine.util.modifier.ease.IEaseFunction

class StoryboardFlipHCommand(
    easeFunction: IEaseFunction,
    startTime: Float,
    endTime: Float,
    startValue: Boolean,
    endValue: Boolean
) : StoryboardCommand<Boolean>(easeFunction, startTime, endTime, startValue, endValue) {
    override fun applyInitialValue(textureQuad: TextureQuad) {
        textureQuad.flipHorizontal = startValue
    }

    override fun applyValue(textureQuad: TextureQuad, percentage: Float) {
        textureQuad.flipHorizontal = if (percentage >= endTime) endValue else startValue
    }
}