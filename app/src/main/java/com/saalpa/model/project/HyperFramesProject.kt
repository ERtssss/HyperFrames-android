package com.saalpa.model.project

import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.RenderResolution
import com.saalpa.model.VideoEffectType
import java.util.UUID

/**
 * Scene Transition Types for scene boundaries in HyperFrames
 */
enum class SceneTransitionType(val label: String, val cssClass: String) {
    NONE("Прямая склейка (Cut)", "trans-none"),
    FADE("Плавный переход (Fade)", "trans-fade"),
    SLIDE("Выезд в сторону (Slide)", "trans-slide"),
    WIPE("Шторка (Wipe)", "trans-wipe"),
    ZOOM("Масштабирование (Zoom)", "trans-zoom"),
    DISSOLVE("Кино-растворение (Dissolve)", "trans-dissolve")
}

/**
 * Avatar settings attached to a Scene
 */
data class SceneAvatarSettings(
    val id: String = "alex_pro",
    val characterName: String = "Alex (Pro Host)",
    val avatarImageUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
    val position: String = "bottom-right", // bottom-right, bottom-left, center, custom
    val xPercent: Float = 82f,
    val yPercent: Float = 75f,
    val scale: Float = 1.0f,
    val motion: String = "Breathing & Speaking",
    val isEnabled: Boolean = true
)

/**
 * Voice & Speech settings for a Scene Script
 */
data class SceneVoiceSettings(
    val voiceId: String = "voice_rachel",
    val speakerName: String = "Rachel (Pro Studio)",
    val language: String = "ru-RU / en-US",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val audioPath: String? = null,
    val isRecording: Boolean = false,
    val recordingDurationSec: Float = 0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false
)

/**
 * Visual composition data for a Scene
 */
data class SceneComposition(
    val backgroundColor: String = "#0c0e14",
    val backgroundGradient: String = "radial-gradient(circle at 50% 30%, #1c2333 0%, #08090d 100%)",
    val customHtml: String = "",
    val customCss: String = "",
    val customJs: String = ""
)

/**
 * Element Type in a Scene
 */
enum class ElementType(val label: String) {
    TEXT("Текст"),
    IMAGE("Изображение"),
    VIDEO("Видео футаж"),
    AVATAR("Аватар"),
    BADGE("Бейдж / Тег"),
    EFFECT("Визуальный эффект"),
    AUDIO("Аудиодорожка")
}

/**
 * Element Transform in Canvas
 */
data class ElementTransform(
    val xPercent: Float = 50f,
    val yPercent: Float = 50f,
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f,
    val zIndex: Int = 10
)

/**
 * Element Timing inside a Scene
 */
data class ElementTiming(
    val startOffsetSec: Float = 0f,
    val durationSec: Float = 4f
)

/**
 * Element Animation
 */
data class HyperFrameAnimation(
    val type: MediaAnimationType = MediaAnimationType.ZOOM_IN,
    val durationSec: Float = 0.5f,
    val delaySec: Float = 0f
)

/**
 * A single element inside a Scene
 */
data class HyperFrameElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType = ElementType.TEXT,
    val name: String = "Элемент",
    val textContent: String = "Заголовок сцены",
    val fontSizeSp: Int = 32,
    val textColorHex: String = "#FFFFFF",
    val textBgHex: String = "transparent",
    val fontStyle: String = "Montserrat Bold",
    val sourceUri: String = "",
    val effectType: VideoEffectType = VideoEffectType.NEON_GLOW,
    val transform: ElementTransform = ElementTransform(),
    val timing: ElementTiming = ElementTiming(),
    val animation: HyperFrameAnimation = HyperFrameAnimation(),
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isEnabled: Boolean = true
)

/**
 * A single Scene in HyperFrames - The primary unit of the project
 */
data class HyperFrameScene(
    val id: String = UUID.randomUUID().toString(),
    val index: Int = 0,
    val title: String = "Сцена",
    val script: String = "Текст диктора для озвучивания и генерации речи в этой сцене...",
    val durationSec: Float = 5.0f,
    val voice: SceneVoiceSettings = SceneVoiceSettings(),
    val avatar: SceneAvatarSettings = SceneAvatarSettings(),
    val composition: SceneComposition = SceneComposition(),
    val elements: List<HyperFrameElement> = emptyList(),
    val transition: SceneTransitionType = SceneTransitionType.FADE
)

/**
 * Project settings
 */
data class ProjectSettings(
    val autoPlayScenes: Boolean = true,
    val loopPlayback: Boolean = false,
    val showSafeArea: Boolean = false,
    val showGrid: Boolean = false
)

/**
 * Project Asset (imported image, audio, video)
 */
data class ProjectAsset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: String,
    val type: ElementType,
    val sizeBytes: Long = 0
)

/**
 * HyperFrames Project Model - The Root Model of the Studio
 */
data class HyperFramesProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Новая композиция HyperFrames",
    val aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
    val resolution: RenderResolution = RenderResolution.HD_720P,
    val fps: Int = 30,
    val scenes: List<HyperFrameScene> = emptyList(),
    val assets: List<ProjectAsset> = emptyList(),
    val settings: ProjectSettings = ProjectSettings()
) {
    val totalDurationSec: Float get() = scenes.sumOf { it.durationSec.toDouble() }.toFloat().coerceAtLeast(0.5f)

    fun getSceneAtTime(timeSec: Float): HyperFrameScene? {
        var accumulated = 0f
        for (scene in scenes) {
            val next = accumulated + scene.durationSec
            if (timeSec >= accumulated && timeSec < next) {
                return scene
            }
            accumulated = next
        }
        return scenes.lastOrNull()
    }

    fun getSceneStartTime(sceneId: String): Float {
        var accumulated = 0f
        for (scene in scenes) {
            if (scene.id == sceneId) return accumulated
            accumulated += scene.durationSec
        }
        return 0f
    }
}
