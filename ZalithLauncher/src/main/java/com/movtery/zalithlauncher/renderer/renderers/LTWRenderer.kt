package com.movtery.zalithlauncher.renderer.renderers

import com.movtery.zalithlauncher.renderer.RendererInterface

class LTWRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_ltw"

    override fun getUniqueIdentifier(): String = "d2c1e8b4-55a1-4e78-9032-11cf4e87d3a1"

    override fun getRendererName(): String = "LTW (Large Thin Wrapper)"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "POJAV_RENDERER" to "opengles3_ltw",
            "LIBGL_ES" to "3",
            "WGPU_POWER_PREF" to "high-performance",
            "WGPU_BACKEND" to "vulkan",
            "MESA_VK_WSI_PRESENT_MODE" to "mailbox"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libltw.so"
}
