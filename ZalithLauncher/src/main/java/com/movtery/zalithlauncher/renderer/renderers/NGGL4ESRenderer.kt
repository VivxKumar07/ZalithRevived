package com.movtery.zalithlauncher.renderer.renderers

import com.movtery.zalithlauncher.renderer.RendererInterface

class NGGL4ESRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3"

    override fun getUniqueIdentifier(): String = "e7b90ed6-e518-4d4e-93dc-5c7133cd5b31"

    override fun getRendererName(): String = "Krypton Wrapper"

    override fun getRendererSummary(): String = "Lightweight for 1.17+. Best performance on low-end & Mali chips."

    override fun isRecommended(): Boolean = true

    override fun getMinMCVersion(): String = "1.17"

    override fun getCompatibilityDescription(): String = "MC 1.17+"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "LIBGL_USE_MC_COLOR" to "1",
            "LIBGL_GL" to "31",
            "LIBGL_ES" to "3",
            "LIBGL_NORMALIZE" to "1",
            "LIBGL_NOERROR" to "1",
            "LIBGL_DEFAULT_VAO" to "1",
            "LIBGL_NOBANNER" to "1",
            "LIBGL_BATCH" to "1"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libng_gl4es.so"
}
