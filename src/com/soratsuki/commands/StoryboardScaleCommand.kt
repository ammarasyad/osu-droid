package com.soratsuki.commands

import com.edlplan.framework.math.Vec2
import com.soratsuki.drawables.TextureQuad
import org.andengine.util.modifier.ease.IEaseFunction

class StoryboardScaleCommand(
    easeFunction: IEaseFunction, startTime: Float, endTime: Float, startValue: Vec2, endValue: Vec2
) : StoryboardCommand<Vec2>(easeFunction, startTime, endTime, startValue, endValue) {
    override fun applyInitialValue(textureQuad: TextureQuad) {
        textureQuad.apply {
            enableScale()
        }.scale!!.set(startValue)
    }

    override fun applyValue(textureQuad: TextureQuad, percentage: Float) {
        textureQuad.apply {
            enableScale()
        }.scale!!.set(
            startValue.x * (1 - percentage) + endValue.x * percentage,
            startValue.y * (1 - percentage) + endValue.y * percentage
        )
    }
}