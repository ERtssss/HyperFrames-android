package com.saalpa.model

import java.util.UUID

enum class MediaType(val label: String, val iconName: String) {
    PHOTO("Фото", "Image"),
    VIDEO("Видео", "Movie"),
    VOICEOVER("Озвучка", "Mic"),
    BGM("Музыка", "MusicNote"),
    TEXT("Текст", "TextFields"),
    EFFECT("Эффект", "AutoAwesome")
}

enum class MediaAnimationType(val title: String) {
    FADE("Плавное появление (Fade)"),
    ZOOM_IN("Масштабирование (Zoom In)"),
    SLIDE_UP("Вылет снизу (Slide Up)"),
    BOUNCE("Прыжок (Bounce)"),
    TYPEWRITER("Печатная машинка (Typewriter)"),
    GLITCH("Cyber Glitch"),
    STATIC("Без анимации (Static)")
}

enum class VideoEffectType(val title: String, val cssClass: String) {
    NEON_GLOW("Неоновое свечение (Neon Glow)", "effect-neon-glow"),
    CYBER_GLITCH("Глитч шум (Glitch Matrix)", "effect-cyber-glitch"),
    CINEMATIC_VIGNETTE("Кино-виньетка (Vignette)", "effect-vignette"),
    MATRIX_GRID("Кибер-сетка (Matrix Grid)", "effect-matrix-grid"),
    FLASH_PULSE("Вспышка в бит (Flash Pulse)", "effect-flash-pulse"),
    RGB_SPLIT("RGB Сдвиг (RGB Split)", "effect-rgb-split")
}

data class MediaOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val type: MediaType = MediaType.PHOTO,
    val title: String = "Новый медиа элемент",
    val sourceUri: String = "",
    val targetSceneId: String = "", // empty means global / all scenes
    val startTimeSec: Float = 0f,
    val durationSec: Float = 5f,
    val volume: Float = 1.0f,
    val xPercent: Float = 50f, // 0..100 horizontal center
    val yPercent: Float = 50f, // 0..100 vertical center
    val scale: Float = 1.0f,   // 0.2 .. 3.0
    val opacity: Float = 1.0f, // 0.0 .. 1.0
    val rotationDeg: Float = 0f,
    val animation: MediaAnimationType = MediaAnimationType.ZOOM_IN,
    val isEnabled: Boolean = true,
    val isMuted: Boolean = false,
    
    // Text overlay specific properties
    val textContent: String = "Текст видео",
    val textColor: String = "#00F0FF",
    val textBgColor: String = "rgba(0,0,0,0.6)",
    val fontSizeSp: Int = 28,
    val fontStyle: String = "Cyber Bold",

    // Effect specific properties
    val effectType: VideoEffectType = VideoEffectType.NEON_GLOW
) {
    val endTimeSec: Float get() = startTimeSec + durationSec
    
    fun isActiveAt(timeSec: Float): Boolean {
        if (!isEnabled) return false
        return timeSec >= startTimeSec && timeSec <= endTimeSec
    }
}

data class SceneMarker(
    val index: Int,
    val id: String,
    val title: String,
    val startSec: Float,
    val durationSec: Float
) {
    val endSec: Float get() = startSec + durationSec
}

data class VoiceoverState(
    val isRecording: Boolean = false,
    val recordingDurationSec: Float = 0f,
    val recordedAudioPath: String? = null,
    val voiceoverVolume: Float = 1.0f,
    val bgmVolume: Float = 0.35f,
    val isVoiceoverMuted: Boolean = false,
    val isBgmMuted: Boolean = false
)

object PresetMediaAssets {
    val SAMPLE_PHOTOS = listOf(
        MediaOverlayItem(
            id = "preset_hero_hayabusa",
            type = MediaType.PHOTO,
            title = "Хаябуса (MLBB Аватар)",
            sourceUri = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80",
            targetSceneId = "scene-2",
            startTimeSec = 2f,
            durationSec = 4f,
            xPercent = 50f,
            yPercent = 38f,
            scale = 1.15f,
            animation = MediaAnimationType.ZOOM_IN
        ),
        MediaOverlayItem(
            id = "preset_cyber_badge",
            type = MediaType.PHOTO,
            title = "Cyber Crest Logo",
            sourceUri = "https://images.unsplash.com/photo-1563089145-599997674d42?w=800&auto=format&fit=crop&q=80",
            targetSceneId = "",
            startTimeSec = 0f,
            durationSec = 4f,
            xPercent = 50f,
            yPercent = 35f,
            scale = 1.0f,
            animation = MediaAnimationType.BOUNCE
        )
    )

    val SAMPLE_VIDEOS = listOf(
        MediaOverlayItem(
            id = "preset_particle_loop",
            type = MediaType.VIDEO,
            title = "Cyber Particle Fog",
            sourceUri = "https://assets.mixkit.co/videos/preview/mixkit-digital-animation-of-blue-and-purple-lines-99764-small.mp4",
            targetSceneId = "",
            startTimeSec = 0f,
            durationSec = 15f,
            opacity = 0.4f,
            scale = 1.0f,
            volume = 0f,
            isMuted = true,
            animation = MediaAnimationType.STATIC
        )
    )

    val SAMPLE_VOICEOVERS = listOf(
        MediaOverlayItem(
            id = "preset_vo_patch_ru",
            type = MediaType.VOICEOVER,
            title = "Озвучка: Патч Обновление",
            sourceUri = "https://actions.google.com/sounds/v1/sports/crowd_cheer.ogg",
            startTimeSec = 0f,
            durationSec = 15f,
            volume = 1.0f
        ),
        MediaOverlayItem(
            id = "preset_bgm_synth",
            type = MediaType.BGM,
            title = "BGM: Cyber Beat",
            sourceUri = "https://actions.google.com/sounds/v1/science_fiction/scifi_pulse.ogg",
            startTimeSec = 0f,
            durationSec = 15f,
            volume = 0.35f
        )
    )
}
