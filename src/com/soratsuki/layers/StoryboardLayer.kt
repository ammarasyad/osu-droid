package com.soratsuki.layers

import com.soratsuki.datatypes.Depth
import com.soratsuki.fastForEach
import com.soratsuki.sprites.StoryboardSprite

data class StoryboardLayer(private val name: String, val depth: Depth) {
    companion object {
        fun parse(s: String) = StoryboardLayer(s, Depth.parse(s))
    }

    val sprites = mutableListOf<StoryboardSprite>()

    val visibleWhenPassing
        get() = depth.visibleWhenPassing
    val visibleWhenFailing
        get() = depth.visibleWhenFailing

    fun update(time: Double) {
        sprites.fastForEach { it.update(time) }
    }

    fun clear() = sprites.apply { fastForEach(StoryboardSprite::clear) }.clear()
}