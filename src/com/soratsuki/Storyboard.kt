package com.soratsuki

import com.edlplan.framework.math.Anchor
import com.reco1l.legacy.graphics.texture.BlankTexture
import com.soratsuki.commands.StoryboardSampleCommand
import com.soratsuki.datatypes.Depth
import com.soratsuki.drawables.StoryboardBatchVertexBufferObject
import com.soratsuki.drawables.TextureQuad
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.parser.StoryboardParser
import com.soratsuki.sprites.StoryboardBasicSprite
import com.soratsuki.sprites.StoryboardSprite
import com.soratsuki.texture.StoryboardTexturePool
import org.andengine.entity.sprite.batch.SpriteBatch
import org.andengine.util.debug.Debug
import ru.nsu.ccfit.zuev.osu.GlobalManager.getInstance
import java.io.File
import kotlin.math.max
import kotlin.math.min

class Storyboard(private val width: Float, private val height: Float) : SpriteBatch(
    BlankTexture(),
    DEFAULT_CAPACITY,
    StoryboardBatchVertexBufferObject(
        getInstance().engine.vertexBufferObjectManager,
        DEFAULT_CAPACITY * TextureQuad.SIZE_PER_QUAD,
        VERTEXBUFFEROBJECTATTRIBUTES_DEFAULT
    )
) {
    companion object {
        private const val DEFAULT_CAPACITY = 1024
    }

    private val layers = arrayOfNulls<StoryboardLayer>(Depth.entries.size)
    private val sampleEvents = mutableListOf<StoryboardSampleCommand>()

    private var texturePool: StoryboardTexturePool? = null
    private var loadedFile = ""
    private var replaceBackground = false

    var transparentBackground = false
    var backgroundFile = ""

    init {
        val scale = max(640 / width, 480 / height)
        setScale(scale)
    }

    fun draw(textureQuad: TextureQuad) {
        (mSpriteBatchVertexBufferObject as StoryboardBatchVertexBufferObject).add(textureQuad)
    }

    fun loadStoryboard(osuFile: String) {
        release()
        loadedFile = osuFile

        val dir = File(osuFile).parentFile ?: return

        texturePool = StoryboardTexturePool(dir)

        val file = findOsb(osuFile) ?: File(osuFile)
        val parser = StoryboardParser(this, texturePool!!)

        parser.parseStoryboardFile(file)

        replaceBackground = needReplaceBackground()

        if (backgroundFile != "" && !replaceBackground) {
            texturePool!!.getOrAdd(backgroundFile)
            val backgroundLayer = StoryboardLayer("Background", Depth.BACKGROUND)
            addElement(StoryboardBasicSprite(backgroundLayer, Anchor.TopLeft, backgroundFile, 0f, 0f, texturePool!!.getOrAdd(backgroundFile)).apply {
                textureQuad.enableScale()
                textureQuad.scale!!.set(min(width / textureQuad.size.x, height / textureQuad.size.y))
            })
        }

        parser.release()

        val map: Map<String, StoryboardTexturePool.AtlasTexCoordsInfo>?
        texturePool!!.buildAtlas().let { result ->
            mTexture = result.first
            map = result.second
        }

        layers.fastForEach { layer ->
            layer?.sprites?.fastForEach { sprite ->
                sprite.commandGroup.applyInitialValue(sprite.textureQuad)
            }
        }

        if (map != null) {
            for (i in 0..<Depth.entries.size) {
                val layer = layers[i] ?: continue

                for (sprite in layer.sprites) {
                    val texCoordsInfo = map[sprite.spriteFilename] ?: continue
                    with(sprite.textureQuad) {
                        u1 = texCoordsInfo.u1
                        u2 = texCoordsInfo.u2
                        v1 = texCoordsInfo.v1
                        v2 = texCoordsInfo.v2
                    }
                }
            }
        }

        Debug.i("Storyboard", "Texture quad count: ${layers.sumOf { it?.sprites?.size ?: 0} * 20}")
    }

    fun update(time: Double) {
        val updateTime = time.coerceAtLeast(0.0)

        for (i in 0..<Depth.entries.size) {
            val layer = layers[i] ?: continue

            layer.sprites.fastForEach { sprite ->
                sprite.update(updateTime)
                if (sprite.shouldDraw(updateTime) || sprite.spriteFilename == backgroundFile) {
                    draw(sprite.textureQuad)
                }
            }
        }

        submit()
    }

    fun setBrightness(brightness: Float) {
        layers.fastForEach { layer ->
            layer?.sprites?.fastForEach { sprite ->
                sprite.textureQuad.enableColor()
                sprite.textureQuad.accentColor!!.set(brightness, brightness, brightness, 1f)
            }
        }
    }

    fun release() {
        layers.fastForEachIndexed { index, layer ->
            layer?.clear()
            layers[index] = null
        }

        texturePool?.clear().let { texturePool = null }
        loadedFile = ""
    }

    fun addElement(sprite: StoryboardSprite) {
        if (layers[sprite.layer.depth.value] == null) {
            layers[sprite.layer.depth.value] = StoryboardLayer(sprite.layer.toString(), sprite.layer.depth)
        }

        // TODO: Optimize this
        if (layers[sprite.layer.depth.value]!!.sprites.contains(sprite)) {
            val index = layers[sprite.layer.depth.value]!!.sprites.indexOf(sprite)
            layers[sprite.layer.depth.value]!!.sprites[index] = sprite
        } else {
            layers[sprite.layer.depth.value]!!.sprites.add(sprite)
        }
    }

    fun addSampleCommand(startTime: Double, layer: StoryboardLayer, filePath: String, volume: Double) {
        sampleEvents.add(StoryboardSampleCommand(startTime, layer, filePath, volume))
    }

    private fun findOsb(osuFile: String): File? {
        val dir = File(osuFile).parentFile
        val files = dir?.listFiles { _, name -> name.endsWith(".osb") }
        return files?.firstOrNull()
    }

    private fun needReplaceBackground(): Boolean {
        if (backgroundFile == "" || layers[0] == null) return false

        return layers[0]!!.sprites.filterIsInstance<StoryboardBasicSprite>().any { it.spriteFilename == backgroundFile }
    }

    override fun finalize() {
        release()
        super.finalize()
    }
}