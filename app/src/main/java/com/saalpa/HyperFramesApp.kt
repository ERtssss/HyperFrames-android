package com.saalpa

import android.app.Application
import android.system.Os
import android.util.Log

class HyperFramesApp : Application() {
    companion object {
        init {
            configureMesaEnvironment()
        }

        private fun configureMesaEnvironment() {
            try {
                // Configure Mesa driver to use software rasterizer in environments without hardware DRM render nodes (/dev/dri/renderD128)
                Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
                Os.setenv("MESA_LOADER_DRIVER_OVERRIDE", "swrast", true)
                Os.setenv("GALLIUM_DRIVER", "softpipe", true)
                Os.setenv("MESA_DEBUG", "silent", true)
                Os.setenv("MESA_LOG_FILE", "/dev/null", true)
                Os.setenv("EGL_LOG_LEVEL", "fatal", true)
                Os.setenv("LIBGL_DRI3_DISABLE", "1", true)
            } catch (e: Throwable) {
                Log.d("HyperFramesApp", "Environment configuration: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        configureMesaEnvironment()
    }
}
