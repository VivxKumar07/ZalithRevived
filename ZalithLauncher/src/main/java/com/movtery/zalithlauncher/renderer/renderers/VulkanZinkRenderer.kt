package com.movtery.zalithlauncher.renderer.renderers

import com.movtery.zalithlauncher.renderer.RendererInterface

class VulkanZinkRenderer : RendererInterface {
    override fun getRendererId(): String = "vulkan_zink"

    override fun getUniqueIdentifier(): String = "7c42df98-e6b3-4f51-b0e2-768a4c519213"

    override fun getRendererName(): String = "Vulkan Zink"

    override fun getRendererSummary(): String = "Mesa Desktop OpenGL over Vulkan with OSMesa fallback."

    override fun getMinMCVersion(): String = "1.17"

    override fun getCompatibilityDescription(): String = "MC 1.17+"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "MESA_GL_VERSION_OVERRIDE" to "4.6",
            "MESA_GLSL_VERSION_OVERRIDE" to "460"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}