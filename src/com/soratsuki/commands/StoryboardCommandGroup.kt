package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import com.soratsuki.fastForEach
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

open class StoryboardCommandGroup(protected val commands: MutableList<StoryboardCommand<*>> = mutableListOf()) {
    companion object {
        private val comparator = Comparator<StoryboardCommand<*>> { a, b ->
            if (a.startTime == b.startTime) {
                a.endTime.compareTo(b.endTime)
            } else {
                a.startTime.compareTo(b.startTime)
            }
        }
    }

    open var startTime = Float.MAX_VALUE
    open var endTime = Float.MIN_VALUE
    open val length
        get() = endTime - startTime

    open fun <T> addCommand(command: StoryboardCommand<T>) {
        commands.add(command)
        startTime = min(startTime, command.startTime)
        endTime = max(endTime, command.endTime)
    }

    fun applyInitialValue(textureQuad: TextureQuad) {
        sort()

        val classSet = hashSetOf<KClass<*>>()
        commands.fastForEach { command ->
            if (classSet.add(command::class)) {
                command.applyInitialValue(textureQuad)
            }
        }
    }

    open fun update(textureQuad: TextureQuad, time: Double) {
        for (i in commands.indices) {
            val command = commands[i]

            if (time < command.startTime || time > command.endTime) continue
            command.update(textureQuad, time)
        }
    }

    fun clear() {
        commands.clear()
        startTime = Float.MAX_VALUE
        endTime = Float.MIN_VALUE
    }

    fun sort() = commands.sortWith(comparator)
}