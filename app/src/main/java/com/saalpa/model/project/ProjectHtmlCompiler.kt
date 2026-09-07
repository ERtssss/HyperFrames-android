package com.saalpa.model.project

import com.saalpa.model.MediaAnimationType
import com.saalpa.model.VideoEffectType

/**
 * Compiles a HyperFramesProject into a self-contained, GSAP-driven HTML/CSS/JS document.
 * Follows a unified composition coordinate system (e.g. 1920x1080) for both live Canvas
 * preview and hardware frame-exact MP4 rendering.
 */
object ProjectHtmlCompiler {

    fun compile(
        project: HyperFramesProject,
        currentTimeSec: Float = 0f,
        isPlaying: Boolean = false,
        activeSceneId: String? = null
    ): String {
        val (targetW, targetH) = project.getEffectiveDimensions()
        val totalDuration = project.totalDurationSec
        val scenesHtml = StringBuilder()
        val gsapTimelines = StringBuilder()

        var currentSceneStart = 0f

        project.scenes.forEachIndexed { index, scene ->
            val sceneDuration = scene.durationSec
            val sceneEnd = currentSceneStart + sceneDuration

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
                        val imgW = (targetW * (element.widthPercent / 100f).coerceAtMost(0.95f)).toInt()
                        val imgH = (targetH * (element.heightPercent / 100f).coerceAtMost(0.95f)).toInt()
                        val fit = element.objectFit.ifBlank { "contain" }
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-image $animClass" 
                                 style="$styleBase max-width:${imgW}px; max-height:${imgH}px; overflow:hidden; border-radius:16px; display:flex; align-items:center; justify-content:center;">
                                <img src="${element.sourceUri}" alt="${element.name}" 
                                     style="width:auto; height:auto; max-width:100%; max-height:100%; object-fit:$fit; border-radius:16px; box-shadow:0 12px 36px rgba(0,0,0,0.6);" />
                            </div>
                        """.trimIndent())
                    }
                    ElementType.VIDEO -> {
                        val muted = if (element.isMuted) "muted" else ""
                        val fit = element.objectFit.ifBlank { "cover" }
                        val videoW = (targetW * (element.widthPercent / 100f).coerceAtMost(1f)).toInt()
                        val videoH = (targetH * (element.heightPercent / 100f).coerceAtMost(1f)).toInt()
                        elementsHtml.append("""
                            <div id="el-${element.id}" class="scene-element el-video $animClass" 
                                 style="$styleBase width:${videoW}px; height:${videoH}px; max-width:${targetW}px; max-height:${targetH}px; overflow:hidden; border-radius:16px; display:flex; align-items:center; justify-content:center;">
                                <video src="${element.sourceUri}" playsinline webkit-playsinline autoplay loop $muted 
                                       style="width:100%; height:100%; max-width:100%; max-height:100%; object-fit:$fit; object-position:${element.objectPosition}; display:block; border-radius:16px;"></video>
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

            // Build Scene Container (Avatar completely removed)
            val sceneBg = if (scene.composition.backgroundGradient.isNotBlank()) scene.composition.backgroundGradient else scene.composition.backgroundColor
            val customSceneContent = scene.composition.customHtml

            scenesHtml.append("""
                <div id="scene-${scene.id}" class="hyperframe-scene ${scene.transition.cssClass}" 
                     data-scene-id="${scene.id}" 
                     data-scene-index="$index" 
                     data-start="$currentSceneStart" 
                     data-duration="$sceneDuration"
                     style="position:absolute; inset:0; width:${targetW}px; height:${targetH}px; background:$sceneBg; overflow:hidden; opacity:0; visibility:hidden;">
                    $customSceneContent
                    $elementsHtml
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
            margin: 0;
            padding: 0;
            overflow: hidden;
            background: #000000;
            color: #ffffff;
            font-family: 'Inter', -apple-system, sans-serif;
            -webkit-user-select: none;
            user-select: none;
        }
        #composition-stage {
            width: 100%;
            height: 100%;
            position: relative;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #000000;
        }
        #composition, #hyperframe-root {
            position: absolute;
            width: ${targetW}px;
            height: ${targetH}px;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            transform-origin: center center;
            overflow: hidden;
            contain: strict;
            background: #0e1015;
            box-shadow: 0 0 60px rgba(0,0,0,0.8);
        }
        .hyperframe-scene {
            position: absolute;
            inset: 0;
            width: ${targetW}px;
            height: ${targetH}px;
            overflow: hidden;
        }
        
        /* Video containment and scaling */
        video {
            max-width: 100%;
            max-height: 100%;
            display: block;
        }
        .fit-contain { object-fit: contain !important; }
        .fit-cover { object-fit: cover !important; }
        .fit-fill { object-fit: fill !important; }
        
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
    <div id="composition-stage">
        <div id="composition" data-width="$targetW" data-height="$targetH">
            $scenesHtml
        </div>
    </div>

    <script id="hyperframes-runtime">
        (function() {
            window.HyperFrames = window.HyperFrames || {};
            window.HyperFrames.duration = $totalDuration;
            window.HyperFrames.time = 0;
            window.HyperFrames.isPlaying = false;

            // Unified auto-fit scaling logic (min of availableWidth / targetW, availableHeight / targetH)
            function autoFit() {
                var comp = document.getElementById('composition') || document.getElementById('hyperframe-root');
                if (!comp) return;
                var targetW = $targetW;
                var targetH = $targetH;
                var stage = document.getElementById('composition-stage') || document.body;
                var availableW = stage.clientWidth || window.innerWidth || document.documentElement.clientWidth;
                var availableH = stage.clientHeight || window.innerHeight || document.documentElement.clientHeight;
                if (availableW > 0 && availableH > 0 && targetW > 0 && targetH > 0) {
                    var scale = Math.min(availableW / targetW, availableH / targetH);
                    comp.style.transform = 'translate(-50%, -50%) scale(' + scale + ')';
                }
            }
            window.addEventListener('resize', autoFit);
            window.addEventListener('orientationchange', autoFit);
            document.addEventListener('DOMContentLoaded', autoFit);
            autoFit();

            // GSAP Timeline setup & lag smoothing optimization for deterministic offline rendering
            if (window.gsap && window.gsap.ticker) {
                window.gsap.ticker.lagSmoothing(0);
            }
            var tl = gsap.timeline({ paused: true });
            $gsapTimelines

            window.__hfTl = tl;
            window.__timelines = { main: tl };

            // Cache DOM elements to avoid expensive querySelectorAll during frame rendering loop
            var cachedVideos = [];
            var cachedScenes = [];
            function refreshDomCache() {
                cachedVideos = Array.prototype.slice.call(document.querySelectorAll('video'));
                cachedScenes = Array.prototype.slice.call(document.querySelectorAll('.hyperframe-scene'));
            }

            // Frame-exact seek function called from Android without reloading DOM
            window.__hfSeek = function(timeSec, progress, isExport) {
                window.HyperFrames.time = timeSec;
                if (tl) {
                    tl.seek(timeSec, false);
                }

                // Synchronize HTML5 videos with current time
                if (cachedVideos.length > 0) {
                    for (var v = 0; v < cachedVideos.length; v++) {
                        var vid = cachedVideos[v];
                        var sceneEl = vid.closest('.hyperframe-scene');
                        var startSec = sceneEl ? parseFloat(sceneEl.getAttribute('data-start') || '0') : 0;
                        var sceneTime = Math.max(0, timeSec - startSec);
                        if (vid.duration && !isNaN(vid.duration) && vid.duration > 0) {
                            var targetVidTime = sceneTime % vid.duration;
                            if (Math.abs(vid.currentTime - targetVidTime) > 0.3) {
                                try { vid.currentTime = targetVidTime; } catch(e) {}
                            }
                        }
                    }
                }

                // Notify Android bridge about active scene ONLY during live preview (skipped during export loop)
                if (!isExport && window.AndroidHyperFrames && typeof window.AndroidHyperFrames.onSceneChange === 'function') {
                    for (var i = 0; i < cachedScenes.length; i++) {
                        var sc = cachedScenes[i];
                        var st = parseFloat(sc.getAttribute('data-start') || '0');
                        var dur = parseFloat(sc.getAttribute('data-duration') || '5');
                        if (timeSec >= st && timeSec < (st + dur)) {
                            var idx = parseInt(sc.getAttribute('data-scene-index') || '0');
                            var scId = sc.getAttribute('data-scene-id') || '';
                            window.AndroidHyperFrames.onSceneChange(idx, scId, 'Scene ' + (idx + 1));
                            break;
                        }
                    }
                }
            };

            // Notify bridge timeline is ready
            setTimeout(function() {
                refreshDomCache();
                autoFit();
                if (window.AndroidHyperFrames && typeof window.AndroidHyperFrames.onTimelineReady === 'function') {
                    window.AndroidHyperFrames.onTimelineReady($totalDuration, ${project.scenes.size}, '$sceneMetaJson');
                }
                window.__hfSeek(0, 0, false);
            }, 50);
        })();
    </script>
</body>
</html>
        """.trimIndent()
    }
}
