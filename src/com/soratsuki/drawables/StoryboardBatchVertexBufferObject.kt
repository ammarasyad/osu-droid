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
) : HighPerformanceSpriteBatchVertexBufferObject(pVertexBufferObjectManager, pCapacity, DrawType.STATIC, true, pVertexBufferObjectAttributes), IVertexBufferObject {
    private var floatBuffer = mByteBuffer.asFloatBuffer()
    // 2 floats for position, 4 floats for color, 2 floats for texture coordinates
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
//        val indexArray = ShortArray(pCapacity * 6)
//        for (i in 0 until pCapacity) {
//            indexArray[i * 6 + 0] = (i * 4 + 0).toShort()
//            indexArray[i * 6 + 1] = (i * 4 + 1).toShort()
//            indexArray[i * 6 + 2] = (i * 4 + 2).toShort()
//            indexArray[i * 6 + 3] = (i * 4 + 1).toShort()
//            indexArray[i * 6 + 4] = (i * 4 + 3).toShort()
//            indexArray[i * 6 + 5] = (i * 4 + 2).toShort()
//        }
//        indexBuffer.put(indexArray)
//        indexBuffer.position(0)
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

    override fun getBufferData() = vertexBuffer
    override fun onBufferData() {
        floatBuffer.position(0)
        floatBuffer.put(vertexBuffer)
        floatBuffer.position(0)

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, floatBuffer.capacity(), floatBuffer, mUsage)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity(), indexBuffer, mUsage)
    }

//    private var previousViewport = intArrayOf(0, 0, 0, 0)

    override fun draw(pPrimitiveType: Int, pCount: Int) {
//        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, previousViewport, 0)
//        GLES20.glViewport(0, 0, 640, 480)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mCapacity / TextureQuad.SIZE_PER_QUAD * 6, GLES20.GL_UNSIGNED_SHORT, 0)
//        GLES20.glViewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3])
    }
}