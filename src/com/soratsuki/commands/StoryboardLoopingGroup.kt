package com.soratsuki.commands

import com.soratsuki.drawables.TextureQuad
import kotlin.math.max

class StoryboardLoopingGroup(private val loopStartTime: Double, repeatCount: Int) : StoryboardCommandGroup() {
//    override var endTime = (loopStartTime + super.length * repeatCount).toFloat()
    // Calling the getter in the initializer will cause a stack overflow
    @Suppress("UNUSED_PARAMETER")
    override var startTime = loopStartTime.toFloat()
        set(unused) = Unit

    private val repeatCount: Int

    init {
        if (repeatCount < 0)
            throw IllegalArgumentException("Repeat count must be greater than or equal to 0")

        this.repeatCount = repeatCount + 1
    }

    override fun <T> addCommand(command: StoryboardCommand<T>) {
//        super.addCommand(StoryboardLoopingCommand(command))
        commands.add(StoryboardLoopingCommand(command))
        endTime = max(endTime, loopStartTime.toFloat() + command.length * repeatCount)
    }

    private var testTime = 0.0

    override fun update(textureQuad: TextureQuad, time: Double) {
        for (i in commands.indices) {
            val loopingCommand = commands[i]

            val loopTime = (time - loopStartTime) - loopingCommand.startTime
            val loopCount = (loopTime / loopingCommand.length).toInt()
            val loopProgress = loopTime - loopCount * loopingCommand.length

            if (loopProgress < startTime || loopProgress > endTime) continue

            if (time - testTime > 1000.0) {
                println("Loop time: $loopTime, loop count: $loopCount, loop progress: $loopProgress")
                testTime = time
            }
            loopingCommand.update(textureQuad, loopProgress)
        }
    }

    inner class StoryboardLoopingCommand<T>(private val command: StoryboardCommand<T>) :
        StoryboardCommand<T>(
            command.easeFunction,
            loopStartTime.toFloat() + command.startTime,
            loopStartTime.toFloat() + command.endTime,
            command.startValue,
            command.endValue
        ) {
        override fun applyInitialValue(textureQuad: TextureQuad) {
            command.applyInitialValue(textureQuad)
        }

        override fun applyValue(textureQuad: TextureQuad, @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") time: Float) = command.update(textureQuad, time.toDouble())
    }
}