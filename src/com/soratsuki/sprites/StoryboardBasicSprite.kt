package com.soratsuki.sprites

import com.edlplan.framework.math.Anchor
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.texture.StoryboardTexturePool

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
        commandGroup.update(textureQuad, time)
        loopingGroup?.update(textureQuad, time)
    }
}