/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.renderer.renderers

import android.content.Context
import com.movtery.zalithlauncher.renderer.RendererInterface
import java.io.File

class MobileGluesRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_mobileglues"

    override fun getUniqueIdentifier(): String = PACKAGE_NAME

    override fun getRendererName(): String = "MobileGlues"

    override fun getRendererSummary(): String = "Modern 1.17–1.21+ renderer. Best for Mali/MediaTek devices."

    override fun getMinMCVersion(): String = "1.17"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "POJAV_RENDERER" to "opengles3_mobileglues",
            "LIBGL_ES" to "3"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libmobileglues.so"

    companion object {
        const val PACKAGE_NAME = "com.fcl.plugin.mobileglues"

        @JvmStatic
        fun isInstalled(context: Context): Boolean {
            return runCatching {
                context.packageManager.getApplicationInfo(PACKAGE_NAME, 0)
            }.isSuccess
        }

        @JvmStatic
        fun getInstalledPluginNativePath(context: Context): String? {
            val appInfo = runCatching {
                context.packageManager.getApplicationInfo(PACKAGE_NAME, 0)
            }.getOrNull() ?: return null
            val nativeDir = appInfo.nativeLibraryDir
            val soFile = File(nativeDir, "libmobileglues.so")
            return if (soFile.exists()) nativeDir else nativeDir
        }
    }
}
