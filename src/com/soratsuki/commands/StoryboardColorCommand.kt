package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import org.andengine.util.color.Color
import org.andengine.util.modifier.ease.IEaseFunction

class StoryboardColorCommand(
    easeFunction: IEaseFunction,
    startTime: Float,
    endTime: Float,
    startValue: Color,
    endValue: Color
) : StoryboardCommand<Color>(easeFunction, startTime, endTime, startValue, endValue) {
    override fun applyInitialValue(textureQuad: TextureQuad) {
        textureQuad.apply {
            enableColor()
        }.accentColor!!.set(startValue)
    }

    override fun applyValue(textureQuad: TextureQuad, percentage: Float) {
        textureQuad.apply {
            enableColor()
        }.accentColor!!.set(
            startValue.red * (1 - percentage) + endValue.red * percentage,
            startValue.green * (1 - percentage) + endValue.green * percentage,
            startValue.blue * (1 - percentage) + endValue.blue * percentage,
            startValue.alpha * (1 - percentage) + endValue.alpha * percentage
        )
    }
}