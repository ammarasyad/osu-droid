package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import org.andengine.util.modifier.ease.IEaseFunction

sealed class StoryboardCommand<T>(val easeFunction: IEaseFunction, val startTime: Float, val endTime: Float, val startValue: T, val endValue: T) : Comparable<StoryboardCommand<*>> {
    val length = endTime - startTime

    abstract fun applyInitialValue(textureQuad: TextureQuad)

    protected abstract fun applyValue(textureQuad: TextureQuad, percentage: Float)

    override operator fun compareTo(other: StoryboardCommand<*>) =
        if (startTime == other.startTime) {
            endTime.compareTo(other.endTime)
        } else {
            startTime.compareTo(other.startTime)
        }

    fun update(textureQuad: TextureQuad, time: Double) {
        // Special case for looping groups
        if (this is StoryboardLoopingGroup.StoryboardLoopingCommand) {
            applyValue(textureQuad, time.toFloat())
        } else {
            val progress = easeFunction.getPercentage(time.toFloat() - startTime, (endTime - startTime)).noNaN()
            applyValue(textureQuad, progress.coerceIn(0f, 1f))
        }
    }

    private fun Float.noNaN(): Float {
        return if (isNaN()) 0f else this
    }
}