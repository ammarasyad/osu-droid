package com.soratsuki.drawables

import android.opengl.GLES20
import com.soratsuki.texture.StoryboardTexturePool
import org.andengine.entity.sprite.batch.vbo.HighPerformanceSpriteBatchVertexBufferObject
import org.andengine.opengl.texture.ITexture
import org.andengine.opengl.util.GLState
import org.andengine.opengl.vbo.DrawType
import org.andengine.opengl.vbo.IVertexBufferObject
import org.andengine.opengl.vbo.VertexBufferObjectManager
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class StoryboardBatchVertexBufferObject(
    pVertexBufferObjectManager: VertexBufferObjectManager?,
    pCapacity: Int,
    pVertexBufferObjectAttributes: VertexBufferObjectAttributes?
) : HighPerformanceSpriteBatchVertexBufferObject(pVertexBufferObjectManager, pCapacity, DrawType.DYNAMIC, true, pVertexBufferObjectAttributes), IVertexBufferObject {
    private var floatBuffer = mByteBuffer.asFloatBuffer()
    // 2 floats for position, 1 packed float for color, 2 floats for texture coordinates
    private var vertexBuffer = FloatArray(pCapacity)
    private var indexBuffer = ByteBuffer.allocateDirect(pCapacity * 6 * Short.SIZE_BYTES).order(ByteOrder.nativeOrder())

    private var mIndexBufferID = HARDWARE_BUFFER_ID_INVALID

    init {
        for (i in 0 until pCapacity) {
            indexBuffer
                .putShort((i * 4 + 0).toShort())
                .putShort((i * 4 + 1).toShort())
                .putShort((i * 4 + 2).toShort())
                .putShort((i * 4 + 1).toShort())
                .putShort((i * 4 + 3).toShort())
                .putShort((i * 4 + 2).toShort())
        }
        indexBuffer.position(0)
    }

    fun add(textureQuad: TextureQuad) {
        textureQuad.writeToBuffer(vertexBuffer, mBufferDataOffset)
        mBufferDataOffset += TextureQuad.SIZE_PER_QUAD
    }

    override fun bind(pGLState: GLState) {
        if (mHardwareBufferID == HARDWARE_BUFFER_ID_INVALID || mIndexBufferID == HARDWARE_BUFFER_ID_INVALID) {
            mHardwareBufferID = pGLState.generateBuffer()
            mIndexBufferID = pGLState.generateBuffer()
            mDirtyOnHardware = true

            if (mVertexBufferObjectManager != null) {
                mVertexBufferObjectManager!!.onVertexBufferObjectLoaded(this)
            }
        }

        pGLState.bindArrayBuffer(mHardwareBufferID)
        pGLState.bindIndexBuffer(mIndexBufferID)

        if (mDirtyOnHardware) {
            onBufferData()
            mDirtyOnHardware = false
        }
    }

    override fun onBufferData() {
        floatBuffer.position(0)
        floatBuffer.put(vertexBuffer)
        floatBuffer.position(0)

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, floatBuffer.capacity(), floatBuffer, mUsage)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity(), indexBuffer, mUsage)
    }

    override fun draw(pPrimitiveType: Int, pCount: Int) {
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mCapacity / TextureQuad.SIZE_PER_QUAD * 6, GLES20.GL_UNSIGNED_SHORT, 0)
    }
}