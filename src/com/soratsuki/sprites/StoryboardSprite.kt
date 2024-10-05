package com.soratsuki.sprites

import com.edlplan.framework.math.Anchor
import com.soratsuki.commands.StoryboardCommand
import com.soratsuki.commands.StoryboardCommandGroup
import com.soratsuki.commands.StoryboardLoopingGroup
import com.soratsuki.drawables.TextureQuad
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.texture.StoryboardTexturePool
import kotlin.math.min

abstract class StoryboardSprite(
    val layer: StoryboardLayer,
    origin: Anchor,
    val spriteFilename: String,
    startX: Float,
    startY: Float,
    textureInfo: StoryboardTexturePool.TextureInfo
) {
    val commandGroup = StoryboardCommandGroup()
    val textureQuad = TextureQuad(textureInfo)
//    protected val loopingGroups = mutableListOf<StoryboardLoopingGroup>()
    protected var loopingGroup: StoryboardLoopingGroup? = null
    protected var origin
        get() = textureQuad.anchor
        set(value) {
            textureQuad.anchor = value
        }

    init {
        this.origin = origin
        textureQuad.position.x = startX
        textureQuad.position.y = startY
    }

    fun addCommand(command: StoryboardCommand<*>) {
        commandGroup.addCommand(command)
    }

    fun addLoopingGroup(loopStartTime: Double, repeatCount: Int): StoryboardLoopingGroup {
        loopingGroup = StoryboardLoopingGroup(loopStartTime, repeatCount)
//        loopingGroups.add(loopingGroup)
//        commandGroup.commands.addAll(loopingGroup.commands)
        return loopingGroup!!
    }

    fun clear() {
        commandGroup.clear()
    }

    abstract fun update(time: Double)

//    fun shouldDraw(time: Double) = time >= commandGroup.startTime && time <= commandGroup.endTime
    fun shouldDraw(time: Double): Boolean {
    @Suppress("ConvertTwoComparisonsToRangeCheck") // prevent ClosedFloatingPointRange creation
    return if (loopingGroup == null) {
        time >= commandGroup.startTime && time <= commandGroup.endTime
    } else {
        val minStartTime = min(commandGroup.startTime, loopingGroup!!.startTime)
        val maxEndTime = min(commandGroup.endTime, loopingGroup!!.endTime)
        time >= minStartTime && time <= maxEndTime
    }
}

    fun updateSprite(time: Double) {
//        if (commandGroup.commands.isEmpty()) return
//        if (currentCommand == null) {
//            currentCommand = commandGroup.commands[0]
//        }

//        val size = commandGroup.commands.size
//
//        if (currentCommandIndex < size && currentCommand!!.endTime < time) {
//            var idx = currentCommandIndex + 1
//            while (idx < size && commandGroup.commands[idx].startTime < time) {
//                idx++
//            }
//
//            if (idx != size) {
//                if (commandGroup.commands[idx].startTime < time) {
//                    currentCommand = commandGroup.commands[idx]
//                } else {
//                    idx--
//                    currentCommand = commandGroup.commands[idx]
//                }
//                currentCommandIndex = idx
//            } else {
//                currentCommandIndex = size - 1
//                currentCommand = commandGroup.commands[currentCommandIndex]
//            }
//        }

//        if (time >= currentCommand!!.startTime) {
//            currentCommand!!.update(textureQuad, time)
//            currentCommandIndex++
//            currentCommand = commandGroup.commands.getOrNull(currentCommandIndex)
//        }

//        loopingGroups.fastForEach { it.update(time) }

//        if (time > currentCommand!!.endTime) {
//            currentCommandIndex++
//            if (currentCommandIndex >= commandGroup.commands.size) {
//                currentCommandIndex = 0
//            }
//            currentCommand = commandGroup.commands[currentCommandIndex]
//        }
        update(time)
    }
}
