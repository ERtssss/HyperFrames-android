package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Build
import android.util.Log
import android.view.Surface
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Native Android H.264 Video Encoder utilizing MediaCodec and MediaMuxer.
 * Uses an EGL Input Surface to encode Bitmaps into pristine MP4 video.
 */
class HtmlVideoEncoder(
    private val outputFile: File,
    private val width: Int,
    private val height: Int,
    private val fps: Int = 30,
    private val bitRate: Int = 8_000_000 // 8 Mbps
) {
    private val TAG = "HtmlVideoEncoder"
    private val MIME_TYPE = "video/avc"
    private val I_FRAME_INTERVAL = 1 // 1 sec between keyframes

    private var encoder: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var eglCore: EglCore? = null
    private var windowSurface: WindowSurface? = null
    private var textureRenderer: TextureRenderer? = null
    private var muxer: MediaMuxer? = null

    private var trackIndex = -1
    private var isMuxerStarted = false
    private val bufferInfo = MediaCodec.BufferInfo()

    fun start() {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }

        encoder = MediaCodec.createEncoderByType(MIME_TYPE).apply {
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = createInputSurface()
            start()
        }

        eglCore = EglCore()
        windowSurface = WindowSurface(eglCore!!, inputSurface!!)
        windowSurface?.makeCurrent()
        textureRenderer = TextureRenderer(width, height)

        muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        isMuxerStarted = false
        trackIndex = -1
    }

    fun encodeFrame(bitmap: Bitmap, frameIndex: Int) {
        drainEncoder(endOfStream = false)

        val egl = windowSurface ?: return
        egl.makeCurrent()

        textureRenderer?.drawBitmap(bitmap)

        // Set presentation time in nanoseconds
        val presentationTimeNs = (frameIndex * 1_000_000_000L) / fps
        egl.setPresentationTime(presentationTimeNs)
        egl.swapBuffers()
    }

    fun finish() {
        drainEncoder(endOfStream = false)
        encoder?.signalEndOfInputStream()
        drainEncoder(endOfStream = true)
        release()
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val enc = encoder ?: return
        val mux = muxer ?: return

        val timeoutUs = 10000L
        while (true) {
            val encoderStatus = enc.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (isMuxerStarted) {
                    throw RuntimeException("Format changed after muxer started")
                }
                val newFormat = enc.outputFormat
                trackIndex = mux.addTrack(newFormat)
                mux.start()
                isMuxerStarted = true
            } else if (encoderStatus >= 0) {
                val encodedData = enc.getOutputBuffer(encoderStatus)
                    ?: throw RuntimeException("EncoderOutputBuffer $encoderStatus was null")

                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0
                }

                if (bufferInfo.size != 0) {
                    if (!isMuxerStarted) {
                        throw RuntimeException("Muxer hasn't started")
                    }
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    mux.writeSampleData(trackIndex, encodedData, bufferInfo)
                }

                enc.releaseOutputBuffer(encoderStatus, false)

                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    fun release() {
        try {
            encoder?.stop()
            encoder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping encoder", e)
        }
        encoder = null

        try {
            if (isMuxerStarted) {
                muxer?.stop()
            }
            muxer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping muxer", e)
        }
        muxer = null

        textureRenderer?.release()
        textureRenderer = null

        windowSurface?.release()
        windowSurface = null

        eglCore?.release()
        eglCore = null

        inputSurface?.release()
        inputSurface = null
    }

    // Helper EGL Classes for Surface Rendering
    private class EglCore {
        private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
        var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
        var eglConfig: EGLConfig? = null

        init {
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (eglDisplay === EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("unable to get EGL14 display")
            }
            val version = IntArray(2)
            if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                throw RuntimeException("unable to initialize EGL14")
            }

            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                0x3142, 1, // EGL_RECORDABLE_ANDROID
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)
            eglConfig = configs[0] ?: throw RuntimeException("Unable to find suitable EGLConfig")

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
            if (eglContext === EGL14.EGL_NO_CONTEXT) {
                throw RuntimeException("Failed to create EGL context")
            }
        }

        fun release() {
            if (eglDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroyContext(eglDisplay, eglContext)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(eglDisplay)
            }
            eglDisplay = EGL14.EGL_NO_DISPLAY
            eglContext = EGL14.EGL_NO_CONTEXT
        }

        fun getDisplay(): EGLDisplay = eglDisplay
    }

    private class WindowSurface(private val eglCore: EglCore, private val surface: Surface) {
        private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

        init {
            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
            eglSurface = EGL14.eglCreateWindowSurface(eglCore.getDisplay(), eglCore.eglConfig, surface, surfaceAttribs, 0)
            if (eglSurface === EGL14.EGL_NO_SURFACE) {
                throw RuntimeException("Failed to create window surface")
            }
        }

        fun makeCurrent() {
            EGL14.eglMakeCurrent(eglCore.getDisplay(), eglSurface, eglSurface, eglCore.eglContext)
        }

        fun swapBuffers(): Boolean {
            return EGL14.eglSwapBuffers(eglCore.getDisplay(), eglSurface)
        }

        fun setPresentationTime(nsecs: Long) {
            EGLExt.eglPresentationTimeANDROID(eglCore.getDisplay(), eglSurface, nsecs)
        }

        fun release() {
            if (eglSurface !== EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(eglCore.getDisplay(), eglSurface)
                eglSurface = EGL14.EGL_NO_SURFACE
            }
        }
    }

    private class TextureRenderer(private val width: Int, private val height: Int) {
        private val vertexShaderCode = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = aPosition;
                vTextureCoord = aTextureCoord;
            }
        """.trimIndent()

        private val fragmentShaderCode = """
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTextureCoord);
            }
        """.trimIndent()

        // Full screen quad with flipped Y texture coords (standard for OpenGL bitmap display)
        private val triangleVerticesData = floatArrayOf(
            -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
             1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f,  1.0f, 0.0f, 0.0f, 0.0f,
             1.0f,  1.0f, 0.0f, 1.0f, 0.0f
        )

        private val triangleVertices: FloatBuffer = ByteBuffer.allocateDirect(triangleVerticesData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(triangleVerticesData)
                position(0)
            }

        private var program = 0
        private var textureId = 0
        private var aPositionHandle = 0
        private var aTextureCoordHandle = 0
        private var uTextureHandle = 0

        init {
            program = createProgram(vertexShaderCode, fragmentShaderCode)
            aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition")
            aTextureCoordHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")
            uTextureHandle = GLES20.glGetUniformLocation(program, "uTexture")

            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        }

        fun drawBitmap(bitmap: Bitmap) {
            GLES20.glViewport(0, 0, width, height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            GLES20.glUseProgram(program)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES20.glUniform1i(uTextureHandle, 0)

            triangleVertices.position(0)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, triangleVertices)
            GLES20.glEnableVertexAttribArray(aPositionHandle)

            triangleVertices.position(3)
            GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 5 * 4, triangleVertices)
            GLES20.glEnableVertexAttribArray(aTextureCoordHandle)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }

        fun release() {
            if (program != 0) {
                GLES20.glDeleteProgram(program)
                program = 0
            }
            if (textureId != 0) {
                val textures = intArrayOf(textureId)
                GLES20.glDeleteTextures(1, textures, 0)
                textureId = 0
            }
        }

        private fun createProgram(vertexSource: String, fragmentSource: String): Int {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            val prog = GLES20.glCreateProgram()
            GLES20.glAttachShader(prog, vertexShader)
            GLES20.glAttachShader(prog, fragmentShader)
            GLES20.glLinkProgram(prog)
            return prog
        }

        private fun loadShader(shaderType: Int, source: String): Int {
            val shader = GLES20.glCreateShader(shaderType)
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            return shader
        }
    }
}
