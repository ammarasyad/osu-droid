package com.soratsuki.texture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.edlplan.andengine.TextureHelper
import com.edlplan.framework.math.Vec2Int
import com.reco1l.legacy.graphics.texture.BlankTextureRegion
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.source.FileBitmapTextureAtlasSource
import org.andengine.opengl.util.GLState
import org.andengine.util.debug.Debug
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.max
import kotlin.math.min
import ru.nsu.ccfit.zuev.osu.GlobalManager.getInstance as getGlobalManager

class StoryboardTexturePool(private val directory: File) {
    companion object {
        private const val DEFAULT_MAX_TEXTURE_WIDTH = 4096
        private val emptyTextureRegion = BlankTextureRegion()
    }

    private val maxWidth = min(GLState.INSTANCE.maximumTextureSize, DEFAULT_MAX_TEXTURE_WIDTH)
    private val textures = mutableMapOf<String, TextureInfo>()

    // TODO: Make multiple texture atlases if necessary for some storyboards
    private var finalTextureAtlas: BitmapTextureAtlas? = null

    init {
        if (maxWidth <= 0) {
            throw IllegalStateException("GLState.INSTANCE.maximumTextureSize is $maxWidth")
        }
    }

    fun clear() {
        if (finalTextureAtlas != null)
            getGlobalManager().engine.textureManager.unloadTexture(finalTextureAtlas)

        textures.clear()
    }

    fun getOrAdd(name: String): TextureInfo {
        if (textures.containsKey(name)) {
            return textures[name]!!
        }
        Debug.i("Adding texture: $name")

        val info = loadTextureInfo(name)
        textures[name] = info
        return info
    }

    // TODO: This can be more efficient by intelligently packing textures to empty spaces, but that would considerably increase complexity
    // TODO: Also the tex coords thing is probably very hacky
    fun buildAtlas(): Pair<BitmapTextureAtlas, Map<String, AtlasTexCoordsInfo>?> {
//        if (textures.size == 1)
//            return Pair(textures.values.first().texture.texture as BitmapTextureAtlas, null)
        val atlas = Bitmap.createBitmap(maxWidth, maxWidth, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(atlas)
        val paint = Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }

        val map = mutableMapOf<String, AtlasTexCoordsInfo>()

        var currentX = 0
        var currentY = 0
        var maxAtlasWidth = 0
        var maxAtlasHeight = 0
        var tempBitmap: Bitmap?
        for (texture in textures.values) {
            if (currentX + texture.size.x > maxWidth && currentY + texture.size.y > maxWidth) {
                Debug.w("Storyboard texture atlas reached maximum and cannot grow further!")
                break
            }

            if (currentX + texture.size.x > maxWidth) {
                currentX = 0
                currentY = maxAtlasHeight
                maxAtlasHeight += texture.size.y
            } else {
                maxAtlasHeight = max(maxAtlasHeight, texture.size.y)
            }

            tempBitmap = loadBitmap(texture.name)
            if (tempBitmap == null) {
                Debug.e("Failed to load texture: ${texture.name}, skipping on atlas creation")
                continue
            }
            canvas.drawBitmap(tempBitmap, currentX.toFloat(), currentY.toFloat(), paint)
            tempBitmap.recycle()

            val u1 = currentX.toFloat()
            val u2 = currentX + texture.size.x.toFloat()
            val v1 = currentY.toFloat()
            val v2 = currentY + texture.size.y.toFloat()

            map[texture.name] = AtlasTexCoordsInfo(u1, u2, v1, v2)

            currentX += texture.size.x
            maxAtlasWidth = max(maxAtlasWidth, currentX)
        }

        for (info in map.values) {
            with(info) {
                u1 /= maxAtlasWidth
                u2 = (u2 - 1) / maxAtlasWidth
                v1 /= maxAtlasHeight
                v2 = (v2 - 1) / maxAtlasHeight
            }
        }

        val croppedBitmap = Bitmap.createBitmap(atlas, 0, 0, maxAtlasWidth, maxAtlasHeight)
        canvas.setBitmap(croppedBitmap)

        atlas.recycle()

        val source = FileBitmapTextureAtlasSource.create(TextureHelper.createFactoryFromBitmap(croppedBitmap))
        finalTextureAtlas = BitmapTextureAtlas(getGlobalManager().engine.textureManager, maxAtlasWidth, maxAtlasHeight, TextureOptions.BILINEAR)
        finalTextureAtlas!!.addTextureAtlasSource(source, 0, 0)
        getGlobalManager().engine.textureManager.loadTexture(finalTextureAtlas)

        croppedBitmap.recycle()

        return Pair(finalTextureAtlas!!, map)
    }

    private fun loadBitmap(name: String) =
        try {
            val file = File(directory, name)
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: FileNotFoundException) {
            Debug.e("Texture not found: $name", e)
            null
        }

    private fun loadTextureInfo(name: String) =
        try {
            val file = File(directory, name)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val tex = TextureHelper.createRegion(bitmap)
            TextureInfo(name, tex.u, tex.u2, tex.v, tex.v2, Vec2Int(bitmap.width, bitmap.height))
        } catch (e: Exception) {
            Debug.e("Texture not found: $name, defaulting to empty texture", e)
            TextureInfo(name, emptyTextureRegion.u, emptyTextureRegion.u2, emptyTextureRegion.v, emptyTextureRegion.v2, Vec2Int(1, 1))
        }
    data class AtlasTexCoordsInfo(var u1: Float, var u2: Float, var v1: Float, var v2: Float)
    data class TextureInfo(
        val name: String,
        val u1: Float,
        val u2: Float,
        val v1: Float,
        val v2: Float,
        val size: Vec2Int
    )
}
