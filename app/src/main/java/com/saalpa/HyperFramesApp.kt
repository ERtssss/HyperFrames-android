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
                // Configure graphics environment for cloud emulator/container setups without hardware DRM rendernode (/dev/dri/renderD128)
                Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
                Os.setenv("MESA_LOADER_DRIVER_OVERRIDE", "swrast", true)
                Os.setenv("GALLIUM_DRIVER", "llvmpipe", true)
                Os.setenv("MESA_NO_ERROR", "1", true)
                Os.setenv("LIBGL_DRI3_DISABLE", "1", true)
                Os.setenv("EGL_LOG_LEVEL", "fatal", true)
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
