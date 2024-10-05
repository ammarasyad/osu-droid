package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import org.andengine.util.modifier.ease.IEaseFunction

class StoryboardYCommand(
    easeFunction: IEaseFunction,
    startTime: Float,
    endTime: Float,
    startValue: Float,
    endValue: Float
) : StoryboardCommand<Float>(easeFunction, startTime, endTime, startValue, endValue) {
    override fun applyInitialValue(textureQuad: TextureQuad) {
        textureQuad.position.y = startValue
    }

    override fun applyValue(textureQuad: TextureQuad, percentage: Float) {
        textureQuad.position.y = startValue * (1 - percentage) + endValue * percentage
    }
}