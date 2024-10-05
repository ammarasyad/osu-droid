package com.soratsuki.sprites

import com.edlplan.framework.math.Anchor
import com.soratsuki.datatypes.LoopType
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.texture.StoryboardTexturePool
import org.andengine.opengl.texture.region.TextureRegion

class StoryboardAnimationSprite(
    layer: StoryboardLayer,
    origin: Anchor,
    spriteFilename: String,
    startX: Float,
    startY: Float,
    textureInfo: StoryboardTexturePool.TextureInfo,
    val frameCount: Int,
    val frameDelay: Double,
    val loopType: LoopType
) : StoryboardSprite(layer, origin, spriteFilename, startX, startY, textureInfo) {
    override fun update(time: Double) {
        // TODO: Implement update function
    }
}