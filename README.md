<div align="center">

# ⚡ Zalith Revived

### The ultimate fusion of competitive Zalith Launcher 1 PvP mechanics with modern Cubiq Launcher aesthetics.

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-40C463?style=for-the-badge&logo=android&logoColor=white)](https://github.com/VivxKumar07/ZalithRevived)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-Join%20Community-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/fWwfRBbGeZ)
[![GitHub](https://img.shields.io/badge/GitHub-VivxKumar07-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VivxKumar07/ZalithRevived)

</div>

---

## 🌟 About Zalith Revived

**Zalith Revived** was born out of a passion for competitive Minecraft on Android. When official updates for Zalith Launcher 1 ceased, players lost access to newer versions (1.20+, 1.21+, 26.3+) while retaining the fast PvP combat, zero-latency touch controls, and muscle memory built over years.

**Zalith Revived bridges this gap:**
We restored the snappy, responsive touch controls and custom button layout engine from **Zalith Launcher 1**, updated the runtime engine to launch the latest Minecraft releases flawlessly on modern Android architectures (including ARM64 Mali and Adreno devices), and elevated the entire interface with the sleek, dark, PC-style design of **Cubiq Launcher**.

---

## ✨ Key Features

### ⚔️ Authentic ZL1 Touch & PvP Combat
- **Uncompromised PvP Mechanics**: Pixel-perfect virtual control buttons, responsive combat hit registration, low-latency touch handling, and fast combo capabilities.
- **Custom Controls Layout Editor**: Full visual layout manager and drag-and-drop button configurator directly from ZL1.
- **Haptic & Gyroscope Support**: Fine-tuned vibration feedback, gyroscope aiming sensitivity, and full controller/gamepad mapping.

### 🎨 Sleek Modern PC Launcher Interface
- **Cubiq Split UI Layout**: Clean side navigation with quick access to Play, Installations, Skins, Mods, Patch Notes, News, and Settings.
- **Dynamic Hero Artwork**: Seamless background wallpapers that reflect your installed Minecraft version art, complete with subtle backdrop lighting.
- **Minecraft Ore UI Switches**: Authentic Bedrock Edition Ore UI toggles (`[ | ] [ ]` green when ON, `[ ] [ O ]` dark when OFF).
- **Minecraft 3D Buttons & Sliders**: Tactile beveled buttons and Minecraft stone block seekbars.
- **Authentic Click Sound**: Classic Minecraft button click sound effect with instant ON/OFF mute toggle.
- **Real-Time Dynamic Font Customization**: Choose your favorite launcher typeface (Minecraft, Outfit, Manrope, OnePlus Sans, Plus Jakarta Sans, JetBrains Mono, Space Grotesk, Classical Serif, or System) and see the entire UI adapt live in real-time.

### 👤 Interactive 3D Skin & Cape Wardrobe
- **Live 3D WebGL Preview**: Fully interactive 3D model powered by `skinview3d` with realistic waving cape physics, idle breathing animation, and 360° touch rotation.
- **Official Character Library**: Instant switching between Steve (Classic 4px) and Alex (Slim 3px).
- **Pre-Included Official Capes**: Choose between Migrator, 15th Anniversary, Vanilla, Cherry Blossom, and Minecon classics.
- **Custom Skin & Cape Imports**: Import any custom skin or cape `.png` directly from your device storage with instant live preview.

### 🧩 CurseForge & Modrinth Content Hub
- **Universal Mod Browser**: Search, discover, and install modpacks, individual mods (Forge, Fabric, NeoForge, Quilt), shaders, and resource packs directly from official Modrinth and CurseForge repositories.
- **Active Profile Management**: Easily view and organize mods associated with your selected game installation.

### 🚀 Modern Version Compatibility & Multi-Renderer Engine
- **Minecraft 1.7.10 through 26.3+**: Native support for modern versions requiring Java 17 and Java 21, resolving modern LWJGL3 natives permissions and extract paths.
- **Krypton Wrapper (NG-GL4ES)**: High-performance OpenGL ES 3.x emulation with full GLSL shader translation, optimal for MediaTek Dimensity and Mali GPUs.
- **Low-overhead Translation Wrapper (LTW)**: Fast Vulkan/GL4ES translation pipeline.
- **MobileGlues Standalone App Integration**: Built-in detection and direct download link for the MobileGlues companion APK.
- **Intelligent Compatibility Guard**: Real-time incompatible renderer detection that recommends optimal fallbacks with a 1-tap **"Switch & Play"** feature to prevent crashes.

---

## ⚙️ Settings Overview

Zalith Revived preserves the rich customization categories from original Zalith Launcher 1 inside our streamlined split layout:

| Category | Options & Capabilities |
| :--- | :--- |
| **Video** | Renderer selection (LTW, Krypton Wrapper, MobileGlues, GL4ES, Vulkan Zink, VirGL), GPU driver plugins, notch cutout, resolution scaler (20%–100%), sustained performance mode, SurfaceView toggle, and VSync controls. |
| **Controls** | Virtual button layout editor, button scale & snapping, mouse cursor speed & scale, touch screen control modes, gyroscope sensitivity, and gamepad mapping. |
| **Game** | Java runtime selection (Java 8, 17, 21 & custom runtimes), memory allocation (RAM slider), custom JVM arguments, game directory selection, crash handlers, and log viewers. |
| **Launcher** | Launcher font style switcher, Minecraft UI click sound toggle, custom background wallpaper picker, dark/light theme, and cache management. |
| **Experimental** | Experimental multithreaded chunk loading, Vulkan Zink experimental flags, and GLSL translation debug tools. |
| **About** | Launcher release details, credits, Discord community invite, and GitHub repository links. |

---

## 📥 Installation

1. Download the latest release APK from the [Releases](https://github.com/VivxKumar07/ZalithRevived/releases) tab.
2. Install the APK on your Android device (Android 8.0+ supported, 64-bit ARM recommended).
3. Open **Zalith Revived**, select or create an account (Offline, Microsoft, or Ely.by), install your desired Minecraft version, and dive right in!

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34+ recommended)
- Gradle 8.0+

```bash
# Clone the repository
git clone https://github.com/VivxKumar07/ZalithRevived.git
cd ZalithRevived

# Build debug APK
./gradlew :ZalithLauncher:assembleDebug

# The built APK will be located at:
# ZalithLauncher/build/outputs/apk/debug/ZalithLauncher-debug.apk
```

---

## 🤝 Community & Support

- 💬 **Discord**: [Join our Community Discord](https://discord.gg/fWwfRBbGeZ) for support, discussions, and updates.
- 🐛 **Issue Tracker**: Found a bug or have a suggestion? Open an issue on our [GitHub Issues](https://github.com/VivxKumar07/ZalithRevived/issues) page.

---

## 📜 Credits & Acknowledgments

Zalith Revived is built upon the incredible work of the open-source Minecraft and mobile launcher communities:

- **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)**: The pioneering foundation making Java Edition on mobile possible.
- **[Zalith Launcher](https://github.com/MovTery/ZalithLauncher)** (by MovTery & contributors): The beloved launcher whose touch controls and PvP mechanics inspired this project.
- **[Cubiq Launcher](https://github.com/CubiqMC/CubiqLauncher)**: The inspiration for our modern PC-style UI layout.
- **[skinview3d](https://github.com/bs-community/skinview3d)** (by Kent Rasmussen, Haowei Wen, Sean Boult & contributors): Three.js-based 3D skin & cape WebGL renderer.
- **[MobileGlues / FCLRendererPlugin](https://github.com/ShirosakiMio/FCLRendererPlugin)** (by ShirosakiMio): Standalone renderer plugin for modern OpenGL ES emulation.
- **[NG-GL4ES / Krypton Wrapper](https://github.com/krypton-mc)**: OpenGL translation library for modern Minecraft shaders on ARM GPUs.

---

## ⚖️ License

Zalith Revived is distributed under the terms of the **GNU General Public License v3.0 (GPL-3.0)**.  
See the [LICENSE](LICENSE) file for the full text.

*Minecraft is a trademark of Mojang Synergies AB. Zalith Revived is an independent open-source project and is not affiliated with Mojang Studios or Microsoft.*
