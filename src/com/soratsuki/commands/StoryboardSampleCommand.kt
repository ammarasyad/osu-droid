package com.soratsuki.commands

import com.soratsuki.layers.StoryboardLayer

class StoryboardSampleCommand(
    private val startTime: Double,
    private val layer: StoryboardLayer,
    private val filePath: String,
    private val volume: Double
) {
    private var alreadyPlayed = false

    fun update(time: Double) {
        if (time < startTime || alreadyPlayed) return
        // TODO: Finish this
    }
}