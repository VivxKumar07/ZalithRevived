package com.movtery.zalithlauncher.renderer

/**
 * 启动器渲染器实现
 */
interface RendererInterface {
    /**
     * 获取渲染器的ID
     */
    fun getRendererId(): String

    /**
     * 获取渲染器的唯一标识ID
     */
    fun getUniqueIdentifier(): String

    /**
     * 获取渲染器的名称
     */
    fun getRendererName(): String

    /**
     * 获取渲染器的环境变量
     */
    fun getRendererEnv(): Lazy<Map<String, String>>

    /**
     * 获取需要dlopen的库
     */
    fun getDlopenLibrary(): Lazy<List<String>>

    /**
     * 获取渲染器的库
     */
    fun getRendererLibrary(): String

    /**
     * 获取EGL名称
     */
    fun getRendererEGL(): String? = null

    fun getRendererSummary(): String? = null
    fun getMinMCVersion(): String? = null
    fun getMaxMCVersion(): String? = null
    fun isRecommended(): Boolean = false

    fun getCompatibilityDescription(): String {
        val min = getMinMCVersion()
        val max = getMaxMCVersion()
        return when {
            min != null && max != null -> "MC $min – $max"
            min != null -> "MC $min+"
            max != null -> "MC <= $max"
            else -> "All MC Versions"
        }
    }

    fun isCompatibleWith(minecraftVersion: String, context: android.content.Context? = null): Boolean {
        val parsedVersion = Regex("""^\s*(\d+(?:\.\d+){1,2})""")
            .find(minecraftVersion)
            ?.groupValues
            ?.getOrNull(1)
            ?: return true

        if (getUniqueIdentifier() == "com.fcl.plugin.mobileglues" && context != null) {
            if (!com.movtery.zalithlauncher.renderer.renderers.MobileGluesRenderer.isInstalled(context)) {
                return false
            }
        }

        val minVersion = getMinMCVersion()?.let {
            Regex("""^\s*(\d+(?:\.\d+){1,2})""").find(it)?.groupValues?.getOrNull(1) ?: it
        }
        val maxVersion = getMaxMCVersion()?.let {
            Regex("""^\s*(\d+(?:\.\d+){1,2})""").find(it)?.groupValues?.getOrNull(1) ?: it
        }

        fun compare(v1: String, v2: String): Int {
            val p1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
            val p2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(p1.size, p2.size)
            for (i in 0 until maxLen) {
                val n1 = p1.getOrElse(i) { 0 }
                val n2 = p2.getOrElse(i) { 0 }
                if (n1 != n2) return n1.compareTo(n2)
            }
            return 0
        }

        val isAtLeastMin = minVersion == null || compare(parsedVersion, minVersion) >= 0
        val isAtMostMax = maxVersion == null || compare(parsedVersion, maxVersion) <= 0
        return isAtLeastMin && isAtMostMax
    }
}