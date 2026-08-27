package com.saalpa.model

enum class AspectRatioType(val title: String, val widthRatio: Int, val heightRatio: Int, val description: String) {
    PORTRAIT_9_16("9:16 Reel / Story", 9, 16, "Vertical (TikTok, Reels, Shorts)"),
    SQUARE_1_1("1:1 Square", 1, 1, "Instagram Feed & Posts"),
    LANDSCAPE_16_9("16:9 Widescreen", 16, 9, "YouTube & Landscape Video"),
    PORTRAIT_4_5("4:5 Portrait", 4, 5, "Social Feeds")
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
     * Injects CSS variables and deterministic timeline script.
     */
    fun compileFullHtml(
        paramsMap: Map<String, String>,
        currentTimeSec: Float = 0f,
        durationSec: Float = defaultDurationSec,
        isLivePlaying: Boolean = false
    ): String {
        var processedHtml = htmlBody
        var processedCss = cssStyle
        var processedJs = jsScript

        // Replace custom param placeholders in HTML/CSS/JS (e.g. {{TITLE}}, {{ACCENT_COLOR}})
        paramsMap.forEach { (key, value) ->
            processedHtml = processedHtml.replace("{{$key}}", value)
            processedCss = processedCss.replace("{{$key}}", value)
            processedJs = processedJs.replace("{{$key}}", value)
        }

        val progress = if (durationSec > 0f) (currentTimeSec / durationSec).coerceIn(0f, 1f) else 0f

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0">
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
            </head>
            <body>
                <div id="hyperframe-root">
                    $processedHtml
                </div>
                <script>
                    (function() {
                        window.HyperFrames = {
                            time: $currentTimeSec,
                            duration: $durationSec,
                            progress: $progress,
                            isPlaying: $isLivePlaying,
                            seek: function(t, p) {
                                document.documentElement.style.setProperty('--time', t + 's');
                                document.documentElement.style.setProperty('--progress', p);
                                if (window.onHyperFrameUpdate) {
                                    window.onHyperFrameUpdate(t, p);
                                }
                            }
                        };
                        try {
                            $processedJs
                        } catch(e) {
                            console.error('HyperFrames script error:', e);
                        }
                    })();
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
