package com.soratsuki.sprites

import com.edlplan.framework.math.Anchor
import com.soratsuki.Storyboard
import com.soratsuki.commands.StoryboardCommand
import com.soratsuki.fastForEach
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.texture.StoryboardTexturePool
import org.andengine.opengl.texture.region.TextureRegion

class StoryboardBasicSprite(
    layer: StoryboardLayer,
    origin: Anchor,
    spriteFilename: String,
    startX: Float,
    startY: Float,
    textureInfo: StoryboardTexturePool.TextureInfo
) : StoryboardSprite(
    layer, origin, spriteFilename, startX, startY, textureInfo
) {

    override fun update(time: Double) {
//        loopingGroups.fastForEach { group ->
//            group.commands.fastForEach { command ->
//                command.update(textureQuad, time)
//            }
//        }

//        for (i in commandGroup.commands.indices) {
//            val command = commandGroup.commands[i]
//
//            if (time < command.startTime || time > command.endTime) continue
//            currentCommand = command
//            command.update(textureQuad, time)
//        }

        commandGroup.update(textureQuad, time)
        loopingGroup?.update(textureQuad, time)

//        loopingGroups.fastForEach { group ->
//            group.update(textureQuad, time)
//        }

//        commandGroup.commands.fastForEach { command ->
//            if (time < command.startTime || time > command.endTime) return@fastForEach
//            command.update(textureQuad, time)
//        }
    }
}