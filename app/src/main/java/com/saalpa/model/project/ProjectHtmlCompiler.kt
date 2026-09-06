package com.saalpa.model.project

import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.VideoEffectType

/**
 * Compiles a HyperFramesProject into a self-contained, GSAP-driven HTML/CSS/JS document.
 * This runs natively in Android WebView for both live Canvas preview and hardware frame capturing.
 */
object ProjectHtmlCompiler {

    fun compile(
        project: HyperFramesProject,
        currentTimeSec: Float = 0f,
        isPlaying: Boolean = false,
        activeSceneId: String? = null
    ): String {
        val (targetW, targetH) = when (project.aspectRatio) {
            AspectRatioType.PORTRAIT_9_16 -> 1080 to 1920
            AspectRatioType.SQUARE_1_1 -> 1080 to 1080
            AspectRatioType.LANDSCAPE_16_9 -> 1920 to 1080
            AspectRatioType.PORTRAIT_4_5 -> 1080 to 1350
        }

        val totalDuration = project.totalDurationSec
        val scenesHtml = StringBuilder()
        val gsapTimelines = StringBuilder()

        var currentSceneStart = 0f

        project.scenes.forEachIndexed { index, scene ->
            val sceneDuration = scene.durationSec
            val sceneEnd = currentSceneStart + sceneDuration
            val isSceneActive = if (activeSceneId != null) scene.id == activeSceneId else (currentTimeSec >= currentSceneStart && currentTimeSec < sceneEnd)

            // Build Scene Elements HTML
            val elementsHtml = StringBuilder()
            scene.elements.filter { it.isEnabled }.forEach { element ->
                val animClass = when (element.animation.type) {
                    MediaAnimationType.FADE -> "anim-fade"
                    MediaAnimationType.ZOOM_IN -> "anim-zoom"
                    MediaAnimationType.SLIDE_UP -> "anim-slide"
                    MediaAnimationType.BOUNCE -> "anim-bounce"
                    MediaAnimationType.TYPEWRITER -> "anim-typewriter"
                    MediaAnimationType.GLITCH -> "anim-glitch"
                    MediaAnimationType.STATIC -> ""
                }

                val t = element.transform
                val styleBase = "position:absolute; left:${t.xPercent}%; top:${t.yPercent}%; transform:translate(-50%, -50%) scale(${t.scale}) rotate(${t.rotationDeg}deg); opacity:${t.opacity}; z-index:${t.zIndex};"

                when (element.type) {
                    ElementType.TEXT -> {
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-text $animClass" style="$styleBase">
                                <div style="font-family:'Montserrat','Inter',sans-serif; font-size:${element.fontSizeSp}px; font-weight:800; color:${element.textColorHex}; background:${element.textBgHex}; padding:8px 20px; border-radius:12px; letter-spacing:0.5px; text-shadow:0 4px 18px rgba(0,0,0,0.7); text-align:center;">
                                    ${element.textContent}
                                </div>
                            </div>
                        """.trimIndent())
                    }
                    ElementType.IMAGE -> {
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-image $animClass" style="$styleBase">
                                <img src="${element.sourceUri}" alt="${element.name}" style="max-width:500px; max-height:500px; object-fit:contain; border-radius:16px; box-shadow:0 12px 36px rgba(0,0,0,0.6);" />
                            </div>
                        """.trimIndent())
                    }
                    ElementType.VIDEO -> {
                        val muted = if (element.isMuted) "muted" else ""
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-video $animClass" style="$styleBase">
                                <video src="${element.sourceUri}" playsinline webkit-playsinline $muted style="max-width:900px; max-height:1200px; border-radius:16px;"></video>
                            </div>
                        """.trimIndent())
                    }
                    ElementType.BADGE -> {
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-badge $animClass" style="$styleBase">
                                <div style="font-family:'Montserrat',sans-serif; font-size:20px; font-weight:900; color:#818cf8; background:rgba(99,102,241,0.2); border:2px solid #6366f1; padding:10px 24px; border-radius:50px; text-transform:uppercase; letter-spacing:2px;">
                                    ${element.textContent}
                                </div>
                            </div>
                        """.trimIndent())
                    }
                    ElementType.EFFECT -> {
                        val effectCss = when (element.effectType) {
                            VideoEffectType.NEON_GLOW -> "box-shadow:inset 0 0 90px rgba(99,102,241,0.5), 0 0 50px rgba(99,102,241,0.3); border:2px solid rgba(99,102,241,0.4);"
                            VideoEffectType.CYBER_GLITCH -> "mix-blend-mode:color-dodge; background:repeating-linear-gradient(0deg, rgba(99,102,241,0.12) 0px, rgba(239,68,68,0.12) 2px, transparent 4px);"
                            VideoEffectType.CINEMATIC_VIGNETTE -> "box-shadow:inset 0 0 140px rgba(0,0,0,0.9);"
                            VideoEffectType.MATRIX_GRID -> "background:radial-gradient(circle, transparent 20%, rgba(0,0,0,0.5) 90%), linear-gradient(90deg, rgba(99,102,241,0.08) 1px, transparent 1px) 0 0 / 30px 30px, linear-gradient(rgba(99,102,241,0.08) 1px, transparent 1px) 0 0 / 30px 30px;"
                            VideoEffectType.FLASH_PULSE -> "background:rgba(255,255,255,0.2); mix-blend-mode:overlay;"
                            VideoEffectType.RGB_SPLIT -> "filter:drop-shadow(4px 0px 0px rgba(239,68,68,0.7)) drop-shadow(-4px 0px 0px rgba(99,102,241,0.7));"
                        }
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-effect" style="position:absolute; left:0; top:0; width:100%; height:100%; opacity:${t.opacity}; z-index:30; pointer-events:none; $effectCss"></div>
                        """.trimIndent())
                    }
                    else -> {}
                }
            }

            // Build Scene Avatar HTML
            val avatarHtml = if (scene.avatar.isEnabled) {
                """
                <div class="scene-avatar" style="position:absolute; left:${scene.avatar.xPercent}%; top:${scene.avatar.yPercent}%; transform:translate(-50%, -50%) scale(${scene.avatar.scale}); z-index:45; pointer-events:none; text-align:center;">
                    <div style="position:relative; display:inline-block;">
                        <img src="${scene.avatar.avatarImageUrl}" alt="${scene.avatar.characterName}" style="width:240px; height:240px; border-radius:50%; object-fit:cover; border:4px solid #6366f1; box-shadow:0 8px 30px rgba(99,102,241,0.5);" />
                        <div style="position:absolute; bottom:6px; right:6px; background:#10b981; width:22px; height:22px; border-radius:50%; border:3px solid #0f1117;"></div>
                    </div>
                    <div style="margin-top:8px; font-family:'Montserrat',sans-serif; font-size:16px; font-weight:700; color:#e2e8f0; background:rgba(15,17,23,0.8); padding:4px 14px; border-radius:20px; border:1px solid #334155; display:inline-block;">
                        ${scene.avatar.characterName}
                    </div>
                </div>
                """.trimIndent()
            } else ""

            // Build Scene Container
            val sceneBg = if (scene.composition.backgroundGradient.isNotBlank()) scene.composition.backgroundGradient else scene.composition.backgroundColor
            val customSceneContent = scene.composition.customHtml

            scenesHtml.append("""
                <div id="scene-${scene.id}" class="hyperframe-scene ${scene.transition.cssClass}" 
                     data-scene-id="${scene.id}" 
                     data-scene-index="$index" 
                     data-start="$currentSceneStart" 
                     data-duration="$sceneDuration"
                     style="position:absolute; inset:0; width:100%; height:100%; background:$sceneBg; overflow:hidden; display:flex; flex-direction:column; justify-content:center; align-items:center; opacity:0; visibility:hidden;">
                    $customSceneContent
                    $elementsHtml
                    $avatarHtml
                </div>
            """.trimIndent())

            // GSAP Timeline Registration
            val transDuration = when (scene.transition) {
                SceneTransitionType.NONE -> 0.05f
                SceneTransitionType.FADE -> 0.4f
                SceneTransitionType.SLIDE -> 0.45f
                SceneTransitionType.WIPE -> 0.4f
                SceneTransitionType.ZOOM -> 0.45f
                SceneTransitionType.DISSOLVE -> 0.5f
            }

            when (scene.transition) {
                SceneTransitionType.NONE -> {
                    gsapTimelines.append("""
                        tl.set("#scene-${scene.id}", { autoAlpha: 1 }, $currentSceneStart);
                        tl.set("#scene-${scene.id}", { autoAlpha: 0 }, ${sceneEnd});
                    """.trimIndent())
                }
                SceneTransitionType.SLIDE -> {
                    gsapTimelines.append("""
                        tl.fromTo("#scene-${scene.id}", 
                            { autoAlpha: 0, x: 150 }, 
                            { autoAlpha: 1, x: 0, duration: $transDuration, ease: "power2.out" }, 
                            $currentSceneStart
                        );
                        tl.to("#scene-${scene.id}", 
                            { autoAlpha: 0, x: -150, duration: $transDuration, ease: "power2.in" }, 
                            ${(sceneEnd - transDuration).coerceAtLeast(currentSceneStart)}
                        );
                    """.trimIndent())
                }
                SceneTransitionType.ZOOM -> {
                    gsapTimelines.append("""
                        tl.fromTo("#scene-${scene.id}", 
                            { autoAlpha: 0, scale: 0.85 }, 
                            { autoAlpha: 1, scale: 1, duration: $transDuration, ease: "power2.out" }, 
                            $currentSceneStart
                        );
                        tl.to("#scene-${scene.id}", 
                            { autoAlpha: 0, scale: 1.15, duration: $transDuration, ease: "power2.in" }, 
                            ${(sceneEnd - transDuration).coerceAtLeast(currentSceneStart)}
                        );
                    """.trimIndent())
                }
                else -> { // FADE or DISSOLVE
                    gsapTimelines.append("""
                        tl.fromTo("#scene-${scene.id}", 
                            { autoAlpha: 0 }, 
                            { autoAlpha: 1, duration: $transDuration, ease: "power1.inOut" }, 
                            $currentSceneStart
                        );
                        tl.to("#scene-${scene.id}", 
                            { autoAlpha: 0, duration: $transDuration, ease: "power1.inOut" }, 
                            ${(sceneEnd - transDuration).coerceAtLeast(currentSceneStart)}
                        );
                    """.trimIndent())
                }
            }

            currentSceneStart += sceneDuration
        }

        val sceneMetaJson = project.scenes.mapIndexed { idx, sc ->
            val st = project.getSceneStartTime(sc.id)
            """{"index":$idx,"id":"${sc.id}","title":"${sc.title.replace("\"", "\\\"")}","startSec":$st,"durationSec":${sc.durationSec}}"""
        }.joinToString(",", "[", "]")

        return """
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${project.name}</title>
    <!-- GSAP for frame-exact HyperFrames composition -->
    <script src="https://cdn.jsdelivr.net/npm/gsap@3.12.5/dist/gsap.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&family=Montserrat:wght@700;900&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        html, body {
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: #08090d;
            color: #ffffff;
            font-family: 'Inter', -apple-system, sans-serif;
            -webkit-user-select: none;
            user-select: none;
        }
        #hyperframe-root {
            position: absolute;
            width: ${targetW}px;
            height: ${targetH}px;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            transform-origin: center center;
            overflow: hidden;
            background: #0e1015;
            box-shadow: 0 0 60px rgba(0,0,0,0.8);
        }
        
        /* Keyframe animations for elements */
        @keyframes animPulse {
            0%, 100% { transform: translate(-50%, -50%) scale(1); }
            50% { transform: translate(-50%, -50%) scale(1.04); }
        }
        @keyframes animGlitch {
            0% { transform: translate(-50%, -50%) translate(0, 0); }
            20% { transform: translate(-50%, -50%) translate(-3px, 2px); }
            40% { transform: translate(-50%, -50%) translate(3px, -2px); }
            60% { transform: translate(-50%, -50%) translate(-2px, -1px); }
            80% { transform: translate(-50%, -50%) translate(2px, 1px); }
            100% { transform: translate(-50%, -50%) translate(0, 0); }
        }
        .anim-bounce { animation: animPulse 2s infinite ease-in-out; }
        .anim-glitch { animation: animGlitch 0.4s infinite linear; }
    </style>
</head>
<body>
    <div id="hyperframe-root" data-width="$targetW" data-height="$targetH">
        $scenesHtml
    </div>

    <script id="hyperframes-runtime">
        (function() {
            window.HyperFrames = window.HyperFrames || {};
            window.HyperFrames.duration = $totalDuration;
            window.HyperFrames.time = $currentTimeSec;
            window.HyperFrames.isPlaying = $isPlaying;

            // Auto-fit canvas to viewport
            function autoFit() {
                var root = document.getElementById('hyperframe-root');
                if (!root) return;
                var targetW = $targetW;
                var targetH = $targetH;
                var winW = window.innerWidth || document.documentElement.clientWidth;
                var winH = window.innerHeight || document.documentElement.clientHeight;
                if (winW > 0 && winH > 0) {
                    var scale = Math.min(winW / targetW, winH / targetH);
                    root.style.transform = 'translate(-50%, -50%) scale(' + scale + ')';
                }
            }
            window.addEventListener('resize', autoFit);
            autoFit();

            // GSAP Timeline setup
            var tl = gsap.timeline({ paused: true });
            $gsapTimelines

            window.__hfTl = tl;
            window.__timelines = { main: tl };

            // Seek function called from Android
            window.__hfSeek = function(timeSec, progress) {
                window.HyperFrames.time = timeSec;
                if (tl) {
                    tl.seek(timeSec, false);
                }
                // Notify Android bridge about active scene if needed
                var scenes = document.querySelectorAll('.hyperframe-scene');
                for (var i = 0; i < scenes.length; i++) {
                    var sc = scenes[i];
                    var st = parseFloat(sc.getAttribute('data-start') || '0');
                    var dur = parseFloat(sc.getAttribute('data-duration') || '5');
                    if (timeSec >= st && timeSec < (st + dur)) {
                        var idx = parseInt(sc.getAttribute('data-scene-index') || '0');
                        var scId = sc.getAttribute('data-scene-id') || '';
                        if (window.AndroidHyperFrames && typeof window.AndroidHyperFrames.onSceneChange === 'function') {
                            window.AndroidHyperFrames.onSceneChange(idx, scId, 'Scene ' + (idx + 1));
                        }
                        break;
                    }
                }
            };

            // Notify bridge timeline is ready
            setTimeout(function() {
                autoFit();
                if (window.AndroidHyperFrames && typeof window.AndroidHyperFrames.onTimelineReady === 'function') {
                    window.AndroidHyperFrames.onTimelineReady($totalDuration, ${project.scenes.size}, '$sceneMetaJson');
                }
                window.__hfSeek($currentTimeSec, 0);
            }, 50);
        })();
    </script>
</body>
</html>
        """.trimIndent()
    }
}
