package com.reco1l.legacy.engine

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.os.Build
import android.view.Surface
import com.reco1l.framework.extensions.orCatch
import org.andengine.opengl.texture.PixelFormat
import org.andengine.opengl.texture.Texture
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.region.TextureRegion
import org.andengine.opengl.util.GLState
import ru.nsu.ccfit.zuev.osu.GlobalManager
import java.io.File
import javax.microedition.khronos.opengles.GL10

class VideoTexture(val source: String)

    : Texture(
        GlobalManager.getInstance().engine.textureManager,
        PixelFormat.RGBA_8888,
        TextureOptions(
            GL10.GL_NEAREST,
            GL10.GL_LINEAR,
            GL10.GL_CLAMP_TO_EDGE,
            GL10.GL_CLAMP_TO_EDGE,
            false
        ), null)
{

    private val player = MediaPlayer().apply {

        setDataSource(source)
        setVolume(0f, 0f)
        isLooping = false
        prepare()
    }


    private var surfaceTexture: SurfaceTexture? = null


    override fun getWidth() = player.videoWidth

    override fun getHeight() = player.videoHeight


    override fun writeTextureToHardware(pGLState: GLState) = Unit

    override fun loadToHardware(pGLState: GLState)
    {
        mHardwareTextureID = pGLState.generateTexture()

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mHardwareTextureID)

        mTextureOptions.apply()
        mUpdateOnHardwareNeeded = false

        mTextureStateListener?.onLoadedToHardware(this)
    }

    override fun unloadFromHardware(pGLState: GLState?)
    {
        surfaceTexture?.release()
        surfaceTexture = null
        super.unloadFromHardware(pGLState)
    }


    override fun bind(pGLState: GLState?)
    {
        if (!isLoadedToHardware)
            return

        if (surfaceTexture == null)
        {
            surfaceTexture = SurfaceTexture(mHardwareTextureID)

            val surface = Surface(surfaceTexture)
            player.setSurface(surface)
            surface.release()
        }

        { surfaceTexture?.updateTexImage() }.orCatch { isUpdateOnHardwareNeeded = true }
    }

    fun play() = player.start()

    fun pause() = player.pause()

    fun release() = player.release()

    fun seekTo(ms: Int)
    {
        // Unfortunately in old versions we can't seek at closest frame from the desired position.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            player.seekTo(ms.toLong(), MediaPlayer.SEEK_CLOSEST)
        else
            player.seekTo(ms)
    }

    fun setPlaybackSpeed(speed: Float)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            val newParams = player.playbackParams.setSpeed(speed)
            player.playbackParams = newParams
        }
    }


    fun toRegion() = TextureRegion(
        this,
        0f,
        0f,
        width.toFloat(),
        height.toFloat()
    )


    companion object
    {
        /**
         * See [MediaPlayer documentation](https://developer.android.com/guide/topics/media/platform/supported-formats)
         */
        val SUPPORTED_VIDEO_FORMATS = arrayOf("3gp", "mp4", "mkv", "webm")

        fun isSupportedVideo(file: File): Boolean = file.extension.lowercase() in SUPPORTED_VIDEO_FORMATS
    }
}