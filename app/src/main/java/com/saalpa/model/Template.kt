package com.saalpa.model

enum class AspectRatioType(val title: String, val widthRatio: Int, val heightRatio: Int, val description: String) {
    PORTRAIT_9_16("9:16 Reel / Story", 9, 16, "Vertical (TikTok, Reels, Shorts)"),
    SQUARE_1_1("1:1 Square", 1, 1, "Instagram Feed & Posts"),
    LANDSCAPE_16_9("16:9 Widescreen", 16, 9, "YouTube & Landscape Video"),
    PORTRAIT_4_5("4:5 Portrait", 4, 5, "Social Feeds");

    val ratio: Float get() = widthRatio.toFloat() / heightRatio.toFloat()
    val label: String get() = "$widthRatio:$heightRatio"
    val category: String get() = description
}

enum class RenderResolution(val label: String, val width: Int, val height: Int, val note: String) {
    SD_540P("540p (Fast)", 540, 960, "Ultra fast test renders"),
    HD_720P("720p HD", 720, 1280, "Balanced speed & quality"),
    FHD_1080P("1080p Full HD", 1080, 1920, "High quality final export")
}

data class TemplateParam(
    val key: String,
    val label: String,
    val defaultValue: String,
    val currentValue: String = defaultValue,
    val type: ParamType = ParamType.TEXT
)

enum class ParamType {
    TEXT,
    COLOR_HEX,
    NUMBER,
    SELECT
}

data class VideoTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val defaultDurationSec: Float = 3.0f,
    val defaultFps: Int = 30,
    val defaultAspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
    val htmlBody: String,
    val cssStyle: String,
    val jsScript: String,
    val params: List<TemplateParam> = emptyList()
) {
    /**
     * Generates a self-contained HTML document ready for preview or frame rendering.
     * Injects CSS variables, responsive auto-scaling, media overlays (photo/video/voiceover),
     * and JavaScriptInterface bridge for GSAP & Android UI synchronisation.
     */
    fun compileFullHtml(
        paramsMap: Map<String, String>,
        currentTimeSec: Float = 0f,
        durationSec: Float = defaultDurationSec,
        isLivePlaying: Boolean = false,
        mediaOverlays: List<MediaOverlayItem> = emptyList(),
        voiceoverState: VoiceoverState? = null
    ): String {
        var processedHtml = htmlBody
        var processedCss = cssStyle
        var processedJs = jsScript

        val effectiveParams = params.associate { it.key to it.defaultValue }.toMutableMap().apply {
            putAll(paramsMap)
        }

        // Replace custom param placeholders in HTML/CSS/JS (e.g. {{TITLE}}, {{ACCENT_COLOR}})
        effectiveParams.forEach { (key, value) ->
            processedHtml = processedHtml.replace("{{$key}}", value)
            processedCss = processedCss.replace("{{$key}}", value)
            processedJs = processedJs.replace("{{$key}}", value)
        }

        val progress = if (durationSec > 0f) (currentTimeSec / durationSec).coerceIn(0f, 1f) else 0f
        val isFullDocument = processedHtml.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) ||
                processedHtml.trimStart().startsWith("<html", ignoreCase = true)

        // Build HTML markup for Media Overlays (Photos, Videos, Audio tracks)
        val mediaElementsHtml = StringBuilder()
        mediaOverlays.filter { it.isEnabled }.forEach { item ->
            when (item.type) {
                MediaType.PHOTO -> {
                    mediaElementsHtml.append("""
                        <div id="media-${item.id}" class="hyperframe-media-photo" 
                             data-media-id="${item.id}"
                             data-start="${item.startTimeSec}" 
                             data-duration="${item.durationSec}"
                             data-target-scene="${item.targetSceneId}"
                             data-animation="${item.animation.name}"
                             style="position:absolute; left:${item.xPercent}%; top:${item.yPercent}%; transform:translate(-50%, -50%) scale(${item.scale}); opacity:${item.opacity}; z-index:40; pointer-events:none; transition:opacity 0.25s ease, transform 0.25s ease;">
                            <img src="${item.sourceUri}" alt="${item.title}" style="max-width:480px; max-height:480px; width:auto; height:auto; object-fit:contain; border-radius:18px; box-shadow:0 12px 35px rgba(0,0,0,0.6);" />
                        </div>
                    """.trimIndent())
                }
                MediaType.VIDEO -> {
                    val mutedAttr = if (item.isMuted) "muted" else ""
                    mediaElementsHtml.append("""
                        <div id="media-${item.id}" class="hyperframe-media-video"
                             data-media-id="${item.id}"
                             data-start="${item.startTimeSec}" 
                             data-duration="${item.durationSec}"
                             data-target-scene="${item.targetSceneId}"
                             style="position:absolute; left:${item.xPercent}%; top:${item.yPercent}%; transform:translate(-50%, -50%) scale(${item.scale}); opacity:${item.opacity}; z-index:35; pointer-events:none;">
                            <video id="vid-${item.id}" src="${item.sourceUri}" playsinline webkit-playsinline $mutedAttr style="max-width:1080px; max-height:1920px; border-radius:16px;"></video>
                        </div>
                    """.trimIndent())
                }
                MediaType.VOICEOVER, MediaType.BGM -> {
                    val isVoice = item.type == MediaType.VOICEOVER
                    val effectiveVolume = if (isVoice) {
                        if (voiceoverState?.isVoiceoverMuted == true) 0f else (item.volume * (voiceoverState?.voiceoverVolume ?: 1f))
                    } else {
                        if (voiceoverState?.isBgmMuted == true) 0f else (item.volume * (voiceoverState?.bgmVolume ?: 0.35f))
                    }
                    mediaElementsHtml.append("""
                        <audio id="audio-${item.id}" src="${item.sourceUri}" data-start="${item.startTimeSec}" data-duration="${item.durationSec}" data-volume="$effectiveVolume" preload="auto"></audio>
                    """.trimIndent())
                }
                MediaType.TEXT -> {
                    val textShadow = "0 2px 10px rgba(0,0,0,0.8), 0 0 20px ${item.textColor}"
                    mediaElementsHtml.append("""
                        <div id="media-${item.id}" class="hyperframe-media-text"
                             data-media-id="${item.id}"
                             data-start="${item.startTimeSec}" 
                             data-duration="${item.durationSec}"
                             data-target-scene="${item.targetSceneId}"
                             data-animation="${item.animation.name}"
                             style="position:absolute; left:${item.xPercent}%; top:${item.yPercent}%; transform:translate(-50%, -50%) scale(${item.scale}); opacity:${item.opacity}; z-index:50; pointer-events:none; text-align:center; transition:opacity 0.2s ease, transform 0.2s ease;">
                            <div style="font-family:'Inter',system-ui,sans-serif; font-size:${item.fontSizeSp}px; font-weight:900; color:${item.textColor}; text-shadow:$textShadow; background:${item.textBgColor}; padding:6px 16px; border-radius:10px; display:inline-block; border:1px solid rgba(255,255,255,0.15); letter-spacing:0.5px;">
                                ${item.textContent}
                            </div>
                        </div>
                    """.trimIndent())
                }
                MediaType.EFFECT -> {
                    val filterStyle = when (item.effectType) {
                        VideoEffectType.NEON_GLOW -> "box-shadow:inset 0 0 80px rgba(0,240,255,0.45), 0 0 40px rgba(0,240,255,0.3); border:2px solid rgba(0,240,255,0.4);"
                        VideoEffectType.CYBER_GLITCH -> "mix-blend-mode:color-dodge; background:repeating-linear-gradient(0deg, rgba(0,240,255,0.1) 0px, rgba(255,42,109,0.1) 2px, transparent 4px);"
                        VideoEffectType.CINEMATIC_VIGNETTE -> "box-shadow:inset 0 0 120px rgba(0,0,0,0.85);"
                        VideoEffectType.MATRIX_GRID -> "background:radial-gradient(circle, transparent 20%, rgba(0,0,0,0.4) 90%), linear-gradient(90deg, rgba(0,255,128,0.08) 1px, transparent 1px) 0 0 / 24px 24px, linear-gradient(rgba(0,255,128,0.08) 1px, transparent 1px) 0 0 / 24px 24px;"
                        VideoEffectType.FLASH_PULSE -> "background:rgba(255,255,255,0.25); mix-blend-mode:overlay;"
                        VideoEffectType.RGB_SPLIT -> "filter:drop-shadow(3px 0px 0px rgba(255,0,0,0.6)) drop-shadow(-3px 0px 0px rgba(0,240,255,0.6));"
                    }
                    mediaElementsHtml.append("""
                        <div id="media-${item.id}" class="hyperframe-media-effect ${item.effectType.cssClass}"
                             data-media-id="${item.id}"
                             data-start="${item.startTimeSec}" 
                             data-duration="${item.durationSec}"
                             data-target-scene="${item.targetSceneId}"
                             style="position:absolute; left:0; top:0; width:100%; height:100%; opacity:${item.opacity}; z-index:45; pointer-events:none; $filterStyle">
                        </div>
                    """.trimIndent())
                }
            }
        }

        val bridgeScript = """
            <script id="hyperframes-bridge">
                (function() {
                    window.HyperFrames = window.HyperFrames || {};
                    window.HyperFrames.time = $currentTimeSec;
                    window.HyperFrames.duration = $durationSec;
                    window.HyperFrames.progress = $progress;
                    window.HyperFrames.isPlaying = $isLivePlaying;

                    var lastActiveSceneId = "";

                    function autoFitCanvas() {
                        var root = document.getElementById('root') || 
                                   document.querySelector('[data-composition-id]') || 
                                   document.getElementById('hyperframe-root');
                        if (!root) {
                            root = document.body.firstElementChild;
                        }
                        if (!root) return;
                        
                        var targetW = parseFloat(root.getAttribute('data-width')) || 1080;
                        var targetH = parseFloat(root.getAttribute('data-height')) || 1920;

                        var winW = window.innerWidth || document.documentElement.clientWidth || 1080;
                        var winH = window.innerHeight || document.documentElement.clientHeight || 1920;
                        
                        if (winW > 0 && winH > 0 && targetW > 0 && targetH > 0) {
                            var scale = Math.min(winW / targetW, winH / targetH);
                            root.style.position = 'absolute';
                            root.style.left = '50%';
                            root.style.top = '50%';
                            root.style.width = targetW + 'px';
                            root.style.height = targetH + 'px';
                            root.style.transform = 'translate(-50%, -50%) scale(' + scale + ')';
                            root.style.transformOrigin = 'center center';
                            root.style.margin = '0';
                        }
                    }

                    function syncMediaOverlays(timeSec, isPlay) {
                        // 1. Photos
                        var photos = document.querySelectorAll('.hyperframe-media-photo');
                        for (var p = 0; p < photos.length; p++) {
                            var el = photos[p];
                            var start = parseFloat(el.getAttribute('data-start') || '0');
                            var dur = parseFloat(el.getAttribute('data-duration') || '$durationSec');
                            var end = start + dur;
                            var anim = el.getAttribute('data-animation') || 'FADE';
                            if (timeSec >= start && timeSec < end) {
                                el.style.visibility = 'visible';
                                el.style.opacity = '1';
                                if (anim === 'ZOOM_IN') {
                                    var animProg = Math.min(1, (timeSec - start) / 0.4);
                                    var scaleVal = 0.85 + (0.15 * animProg);
                                    el.style.transform = 'translate(-50%, -50%) scale(' + scaleVal + ')';
                                }
                            } else {
                                el.style.visibility = 'hidden';
                                el.style.opacity = '0';
                            }
                        }

                        // 2. Videos
                        var vids = document.querySelectorAll('.hyperframe-media-video video');
                        for (var v = 0; v < vids.length; v++) {
                            var vid = vids[v];
                            var parent = vid.parentElement;
                            var vStart = parseFloat(parent.getAttribute('data-start') || '0');
                            var vDur = parseFloat(parent.getAttribute('data-duration') || '$durationSec');
                            var vEnd = vStart + vDur;
                            if (timeSec >= vStart && timeSec < vEnd) {
                                parent.style.visibility = 'visible';
                                var targetVidTime = Math.max(0, timeSec - vStart);
                                if (Math.abs(vid.currentTime - targetVidTime) > 0.15) {
                                    vid.currentTime = targetVidTime;
                                }
                                if (isPlay && vid.paused) {
                                    vid.play().catch(function(){});
                                } else if (!isPlay && !vid.paused) {
                                    vid.pause();
                                }
                            } else {
                                parent.style.visibility = 'hidden';
                                if (!vid.paused) vid.pause();
                            }
                        }

                        // 3. Audio & Voiceovers
                        var audios = document.querySelectorAll('audio[id^="audio-"]');
                        for (var a = 0; a < audios.length; a++) {
                            var aud = audios[a];
                            var aStart = parseFloat(aud.getAttribute('data-start') || '0');
                            var aDur = parseFloat(aud.getAttribute('data-duration') || '$durationSec');
                            var aEnd = aStart + aDur;
                            var aVol = parseFloat(aud.getAttribute('data-volume') || '1.0');
                            aud.volume = aVol;

                            if (timeSec >= aStart && timeSec < aEnd) {
                                var targetAudTime = Math.max(0, timeSec - aStart);
                                if (Math.abs(aud.currentTime - targetAudTime) > 0.15) {
                                    aud.currentTime = targetAudTime;
                                }
                                if (isPlay && aud.paused && aVol > 0) {
                                    aud.play().catch(function(){});
                                } else if (!isPlay && !aud.paused) {
                                    aud.pause();
                                }
                            } else {
                                if (!aud.paused) aud.pause();
                            }
                        }
                    }

                    function detectAndReportActiveScene(timeSec) {
                        var sceneElements = document.querySelectorAll('.scene, [data-start], [id^="scene-"]');
                        var activeIdx = -1;
                        var activeId = "";
                        var activeTitle = "";

                        for (var i = 0; i < sceneElements.length; i++) {
                            var sc = sceneElements[i];
                            var start = parseFloat(sc.getAttribute('data-start') || '0');
                            var dur = parseFloat(sc.getAttribute('data-duration') || '5');
                            if (timeSec >= start && timeSec < (start + dur)) {
                                activeIdx = i;
                                activeId = sc.id || ('scene_' + (i + 1));
                                var h = sc.querySelector('h1, h2, .hero-name, .badge');
                                activeTitle = h ? h.innerText.trim() : ('Scene ' + (i + 1));
                                break;
                            }
                        }

                        if (activeId && activeId !== lastActiveSceneId) {
                            lastActiveSceneId = activeId;
                            if (window.AndroidHyperFrames && typeof window.AndroidHyperFrames.onSceneChange === 'function') {
                                window.AndroidHyperFrames.onSceneChange(activeIdx, activeId, activeTitle);
                            }
                        }
                    }

                    window.__hfSeek = function(timeSec, prog) {
                        window.HyperFrames.time = timeSec;
                        window.HyperFrames.progress = prog;

                        document.documentElement.style.setProperty('--time', timeSec + 's');
                        document.documentElement.style.setProperty('--progress', prog);
                        document.documentElement.style.setProperty('--duration', '$durationSec' + 's');

                        // 1. HeyGen HyperFrames registered timelines (GSAP)
                        if (window.__timelines) {
                            for (var key in window.__timelines) {
                                if (window.__timelines.hasOwnProperty(key)) {
                                    var tl = window.__timelines[key];
                                    if (tl && typeof tl.seek === 'function') {
                                        tl.seek(timeSec, false);
                                    } else if (tl && typeof tl.time === 'function') {
                                        tl.time(timeSec);
                                    }
                                }
                            }
                        }

                        // 2. Global GSAP timeline fallback
                        if (window.gsap && window.gsap.globalTimeline && typeof window.gsap.globalTimeline.seek === 'function') {
                            window.gsap.globalTimeline.seek(timeSec, false);
                        }

                        // 3. Native seekTo function
                        if (typeof window.seekTo === 'function') {
                            try { window.seekTo(timeSec); } catch(e) {}
                        }
                        if (typeof window.__seekTo === 'function') {
                            try { window.__seekTo(timeSec); } catch(e) {}
                        }

                        // 4. HyperFrames SDK seek handler
                        if (window.HyperFrames && typeof window.HyperFrames.onSeek === 'function') {
                            try { window.HyperFrames.onSeek(timeSec, prog); } catch(e) {}
                        }

                        // 5. Custom onHyperFrameUpdate callback
                        if (typeof window.onHyperFrameUpdate === 'function') {
                            try { window.onHyperFrameUpdate(timeSec, prog); } catch(e) {}
                        }

                        // 6. Declarative scene visibility fallback (data-start & data-duration)
                        if (!window.__timelines || Object.keys(window.__timelines).length === 0) {
                            var scenes = document.querySelectorAll('[data-start]');
                            for (var i = 0; i < scenes.length; i++) {
                                var el = scenes[i];
                                var start = parseFloat(el.getAttribute('data-start') || '0');
                                var dur = parseFloat(el.getAttribute('data-duration') || '9999');
                                var end = start + dur;
                                if (timeSec >= start && timeSec < end) {
                                    el.style.visibility = 'visible';
                                    el.style.opacity = '1';
                                } else {
                                    el.style.visibility = 'hidden';
                                    el.style.opacity = '0';
                                }
                            }
                        }

                        // 7. Synchronize Photos, Videos, and Audio tracks
                        syncMediaOverlays(timeSec, window.HyperFrames.isPlaying);

                        // 8. Detect scene changes and report to Android UI
                        detectAndReportActiveScene(timeSec);
                    };

                    window.HyperFrames.seek = window.__hfSeek;

                    function discoverScenesAndNotifyAndroid() {
                        var sceneList = [];
                        var scenes = document.querySelectorAll('.scene, [data-start], [id^="scene-"]');
                        for (var i = 0; i < scenes.length; i++) {
                            var sc = scenes[i];
                            var sStart = parseFloat(sc.getAttribute('data-start') || (i * 4).toString());
                            var sDur = parseFloat(sc.getAttribute('data-duration') || '4');
                            var sId = sc.id || ('scene-' + (i + 1));
                            var h = sc.querySelector('h1, h2, .hero-name, .badge, .title');
                            var sTitle = h ? h.innerText.trim() : ('Scene ' + (i + 1));
                            sceneList.push({
                                index: i,
                                id: sId,
                                title: sTitle,
                                startSec: sStart,
                                durationSec: sDur
                            });
                        }

                        if (window.AndroidHyperFrames && typeof window.AndroidHyperFrames.onTimelineReady === 'function') {
                            window.AndroidHyperFrames.onTimelineReady($durationSec, sceneList.length, JSON.stringify(sceneList));
                        }
                    }

                    window.addEventListener('resize', autoFitCanvas);
                    window.addEventListener('DOMContentLoaded', function() {
                        autoFitCanvas();
                        discoverScenesAndNotifyAndroid();
                        window.__hfSeek($currentTimeSec, $progress);
                    });
                    setTimeout(function() {
                        autoFitCanvas();
                        discoverScenesAndNotifyAndroid();
                        window.__hfSeek($currentTimeSec, $progress);
                    }, 100);
                })();
            </script>
        """.trimIndent()

        val injectedCss = """
            <style id="hyperframes-injected-styles">
                :root {
                    --time: ${currentTimeSec}s;
                    --duration: ${durationSec}s;
                    --progress: $progress;
                    --is-playing: ${if (isLivePlaying) "running" else "paused"};
                }
                html, body {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    overflow: hidden;
                    position: relative;
                    background: #000000;
                }
                #hyperframes-media-layer {
                    position: absolute;
                    inset: 0;
                    pointer-events: none;
                    z-index: 50;
                    overflow: hidden;
                }
            </style>
        """.trimIndent()

        if (isFullDocument) {
            // Full HTML document (e.g. from HeyGen HyperFrames)
            var fullDoc = processedHtml
            val viewportMeta = """<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">"""
            if (!fullDoc.contains("name=\"viewport\"", ignoreCase = true) && !fullDoc.contains("name='viewport'", ignoreCase = true)) {
                if (fullDoc.contains("<head>", ignoreCase = true)) {
                    fullDoc = fullDoc.replace("<head>", "<head>\n$viewportMeta", ignoreCase = true)
                } else if (fullDoc.contains("<head", ignoreCase = true)) {
                    val headRegex = Regex("""(<head[^>]*>)""", RegexOption.IGNORE_CASE)
                    fullDoc = headRegex.replaceFirst(fullDoc, "$1\n$viewportMeta")
                }
            }

            // Inject styles into <head>
            if (fullDoc.contains("</head>", ignoreCase = true)) {
                fullDoc = fullDoc.replace("</head>", "$injectedCss\n</head>", ignoreCase = true)
            } else if (fullDoc.contains("<body", ignoreCase = true)) {
                fullDoc = fullDoc.replace("<body", "$injectedCss\n<body", ignoreCase = true)
            }

            // Inject media layer into #root or before </body> safely using regex
            val mediaLayerHtml = "<div id=\"hyperframes-media-layer\">$mediaElementsHtml</div>"
            val rootDivRegex = Regex("""(<div\s+id=["']root["'][^>]*>)""", RegexOption.IGNORE_CASE)
            if (rootDivRegex.containsMatchIn(fullDoc)) {
                fullDoc = rootDivRegex.replaceFirst(fullDoc, "$1\n$mediaLayerHtml")
            } else if (fullDoc.contains("</body>", ignoreCase = true)) {
                fullDoc = fullDoc.replace("</body>", "$mediaLayerHtml\n</body>", ignoreCase = true)
            } else {
                fullDoc += "\n$mediaLayerHtml"
            }

            // Inject bridge script before </body>
            if (fullDoc.contains("</body>", ignoreCase = true)) {
                fullDoc = fullDoc.replace("</body>", "$bridgeScript\n</body>", ignoreCase = true)
            } else {
                fullDoc = fullDoc + "\n" + bridgeScript
            }

            return fullDoc
        }

        // Modular HTML snippet template
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0">
                <script src="https://cdn.jsdelivr.net/npm/gsap@3.12.5/dist/gsap.min.js"></script>
                <style>
                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                        user-select: none;
                        -webkit-user-select: none;
                    }
                    :root {
                        --time: ${currentTimeSec}s;
                        --duration: ${durationSec}s;
                        --progress: $progress;
                        --is-playing: ${if (isLivePlaying) "running" else "paused"};
                    }
                    html, body {
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                        background: #000;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
                    }
                    #hyperframe-root {
                        width: 100%;
                        height: 100%;
                        position: relative;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        overflow: hidden;
                    }
                    $processedCss
                </style>
                $injectedCss
            </head>
            <body>
                <div id="hyperframe-root">
                    $processedHtml
                    <div id="hyperframes-media-layer">
                        $mediaElementsHtml
                    </div>
                </div>
                $bridgeScript
                <script>
                    try {
                        $processedJs
                    } catch(e) {
                        console.error('HyperFrames script error:', e);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
