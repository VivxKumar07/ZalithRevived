package com.movtery.zalithlauncher.renderer

import android.content.Context
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.renderer.renderers.FreedrenoRenderer
import com.movtery.zalithlauncher.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.renderer.renderers.KopperZinkRenderer
import com.movtery.zalithlauncher.renderer.renderers.LTWRenderer
import com.movtery.zalithlauncher.renderer.renderers.MobileGluesRenderer
import com.movtery.zalithlauncher.renderer.renderers.NGGL4ESRenderer
import com.movtery.zalithlauncher.renderer.renderers.PanfrostRenderer
import com.movtery.zalithlauncher.renderer.renderers.VirGLRenderer
import com.movtery.zalithlauncher.renderer.renderers.VulkanZinkRenderer
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.Tools

/**
 * 启动器所有渲染器总管理者，启动器内置的渲染器与渲染器插件加载的渲染器，都会加载到这里
 */
object Renderers {
    private val renderers: MutableList<RendererInterface> = mutableListOf()
    private var compatibleRenderers: Pair<RenderersList, MutableList<RendererInterface>>? = null
    private var currentRenderer: RendererInterface? = null
    private var isInitialized: Boolean = false

    @JvmStatic
    @JvmOverloads
    fun init(reset: Boolean = false) {
        if (isInitialized && !reset) return
        isInitialized = true

        if (reset) {
            renderers.clear()
            compatibleRenderers = null
            currentRenderer = null
        }

        addRenderers(
            LTWRenderer(),
            NGGL4ESRenderer(),
            GL4ESRenderer(),
            MobileGluesRenderer(),
            KopperZinkRenderer(),
            VulkanZinkRenderer(),
            VirGLRenderer(),
            FreedrenoRenderer(),
            PanfrostRenderer()
        )
    }

    /**
     * 获取兼容当前设备的所有渲染器
     */
    @JvmStatic
    fun getCompatibleRenderers(context: Context): Pair<RenderersList, List<RendererInterface>> = compatibleRenderers ?: run {
        val deviceHasVulkan = Tools.checkVulkanSupport(context.packageManager)
        // Currently, only 32-bit x86 does not have the Zink binary
        val deviceHasZinkBinary = !(Architecture.is32BitsDevice() && Architecture.isx86Device())

        val compatibleRenderers1: MutableList<RendererInterface> = mutableListOf()
        renderers.forEach { renderer ->
            if (renderer.getRendererId().contains("vulkan") && !deviceHasVulkan) return@forEach
            if (renderer.getRendererId().contains("zink") && !deviceHasZinkBinary) return@forEach
            compatibleRenderers1.add(renderer)
        }

        val rendererIdentifiers: MutableList<String> = mutableListOf()
        val rendererNames: MutableList<String> = mutableListOf()
        compatibleRenderers1.forEach { renderer ->
            rendererIdentifiers.add(renderer.getUniqueIdentifier())
            rendererNames.add(renderer.getRendererName())
        }

        val rendererPair = Pair(RenderersList(rendererIdentifiers, rendererNames), compatibleRenderers1)
        compatibleRenderers = rendererPair
        rendererPair
    }

    /**
     * 加入一些渲染器
     */
    @JvmStatic
    fun addRenderers(vararg renderers: RendererInterface) {
        renderers.forEach { renderer ->
            addRenderer(renderer)
        }
    }

    /**
     * 加入单个渲染器
     */
    @JvmStatic
    fun addRenderer(renderer: RendererInterface): Boolean {
        return if (this.renderers.any { it.getUniqueIdentifier() == renderer.getUniqueIdentifier() }) {
            Logging.w("Renderers", "The unique identifier of this renderer (${renderer.getRendererName()} - ${renderer.getUniqueIdentifier()}) conflicts with an already loaded renderer. " +
                    "Normally, this shouldn't happen. You deliberately caused this conflict, didn't you, user?")
            false
        } else {
            this.renderers.add(renderer)
            Logging.i("Renderers", "Renderer loaded: ${renderer.getRendererName()} (${renderer.getRendererId()} - ${renderer.getUniqueIdentifier()})")
            true
        }
    }

    /**
     * 设置当前的渲染器
     * @param context 用于初始化适配当前设备的渲染器
     * @param uniqueIdentifier 渲染器的唯一标识符，用于找到当前想要设置的渲染器
     * @param retryToFirstOnFailure 如果未找到匹配的渲染器，是否跳回渲染器列表的首个渲染器
     */
    @JvmStatic
    @JvmOverloads
    fun setCurrentRenderer(context: Context, uniqueIdentifier: String, retryToFirstOnFailure: Boolean = true) {
        if (!isInitialized) init()
        val compatibleRenderers = getCompatibleRenderers(context).second
        val target = uniqueIdentifier.trim()
        currentRenderer = compatibleRenderers.find {
            it.getUniqueIdentifier().equals(target, ignoreCase = true) ||
            it.getRendererId().equals(target, ignoreCase = true) ||
            it.getRendererName().equals(target, ignoreCase = true) ||
            (target.equals("krypton", ignoreCase = true) && it is NGGL4ESRenderer) ||
            (target.equals("opengles3", ignoreCase = true) && it is NGGL4ESRenderer) ||
            (target.equals("ltw", ignoreCase = true) && it is LTWRenderer) ||
            (target.equals("opengles3_ltw", ignoreCase = true) && it is LTWRenderer) ||
            (target.equals("gl4es", ignoreCase = true) && it is GL4ESRenderer) ||
            (target.equals("opengles2", ignoreCase = true) && it is GL4ESRenderer) ||
            (target.equals("mobileglues", ignoreCase = true) && it is MobileGluesRenderer) ||
            (target.equals("opengles3_mobileglues", ignoreCase = true) && it is MobileGluesRenderer) ||
            (target.equals("kopperzink", ignoreCase = true) && it is KopperZinkRenderer) ||
            (target.equals("vulkan_zink", ignoreCase = true) && it is VulkanZinkRenderer) ||
            (target.equals("virgl", ignoreCase = true) && it is VirGLRenderer) ||
            (target.equals("freedreno", ignoreCase = true) && it is FreedrenoRenderer) ||
            (target.equals("panfrost", ignoreCase = true) && it is PanfrostRenderer)
        } ?: run {
            if (retryToFirstOnFailure) {
                val fallback = compatibleRenderers.find { it is NGGL4ESRenderer }
                    ?: compatibleRenderers.find { it is LTWRenderer }
                    ?: compatibleRenderers.firstOrNull()
                    ?: throw IllegalStateException("No renderers available!")
                Logging.w("Renderers", "Incompatible renderer $uniqueIdentifier will be replaced with ${fallback.getUniqueIdentifier()} (${fallback.getRendererName()})")
                fallback
            } else null
        }
    }

    /**
     * 获取当前的渲染器
     */
    @JvmStatic
    fun getCurrentRenderer(): RendererInterface {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        return currentRenderer ?: throw IllegalStateException("Current renderer not set")
    }

    /**
     * 当前是否设置了渲染器
     */
    @JvmStatic
    fun isCurrentRendererValid(): Boolean = isInitialized && this.currentRenderer != null

    @JvmStatic
    @JvmOverloads
    fun getRecommendedFallback(minecraftVersion: String, context: Context? = null): RendererInterface {
        if (!isInitialized) init()
        val parsedVersion = Regex("""^\s*(\d+(?:\.\d+){1,2})""")
            .find(minecraftVersion)
            ?.groupValues
            ?.getOrNull(1) ?: "1.16.5"

        val isModern = parsedVersion.startsWith("26.") || parsedVersion.startsWith("1.21") || parsedVersion.startsWith("1.20") || parsedVersion.startsWith("1.19") || parsedVersion.startsWith("1.18") || parsedVersion.startsWith("1.17")

        return if (isModern) {
            renderers.find { it is NGGL4ESRenderer && it.isCompatibleWith(minecraftVersion, context) }
                ?: renderers.find { it is LTWRenderer && it.isCompatibleWith(minecraftVersion, context) }
                ?: renderers.find { it is MobileGluesRenderer && it.isCompatibleWith(minecraftVersion, context) }
                ?: renderers.firstOrNull { it.isCompatibleWith(minecraftVersion, context) }
                ?: NGGL4ESRenderer()
        } else {
            renderers.find { it is GL4ESRenderer && it.isCompatibleWith(minecraftVersion, context) }
                ?: renderers.firstOrNull { it.isCompatibleWith(minecraftVersion, context) }
                ?: GL4ESRenderer()
        }
    }

    @JvmStatic
    fun setCurrentRendererForMinecraftVersion(context: Context, uniqueIdentifier: String, minecraftVersion: String) {
        setCurrentRenderer(context, uniqueIdentifier, false)
        val selected = currentRenderer
        if (selected != null && selected.isCompatibleWith(minecraftVersion, context)) return

        val fallback = getRecommendedFallback(minecraftVersion, context)
        currentRenderer = fallback
        Logging.w("Renderers", "Renderer ${selected?.getRendererName()} does not support Minecraft $minecraftVersion; using ${fallback.getRendererName()} instead.")
    }
}