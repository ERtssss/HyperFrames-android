package com.saalpa.model.project

import com.saalpa.model.AspectRatioType
import com.saalpa.model.RenderResolution
import java.util.UUID

object DefaultProjectFactory {

    fun createDefaultProject(): HyperFramesProject {
        val defaultHtml = """
            <div id="stage">
                <div class="empty-canvas-guide">
                    <h1>EMPTY CANVAS</h1>
                    <p>Edit HTML, CSS, JS or add elements</p>
                </div>
            </div>
        """.trimIndent()

        val defaultCss = """
            #stage {
                position: absolute;
                inset: 0;
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                background: radial-gradient(circle at 50% 40%, #162038 0%, #06080e 100%);
                overflow: hidden;
            }

            .empty-canvas-guide {
                text-align: center;
                padding: 24px 32px;
                border: 1px dashed rgba(255, 255, 255, 0.25);
                border-radius: 16px;
                background: rgba(10, 12, 18, 0.65);
                backdrop-filter: blur(10px);
            }

            .empty-canvas-guide h1 {
                font-family: 'Montserrat', sans-serif;
                font-size: 28px;
                font-weight: 800;
                letter-spacing: 2px;
                color: rgba(255, 255, 255, 0.9);
                margin-bottom: 8px;
            }

            .empty-canvas-guide p {
                font-family: 'Inter', sans-serif;
                font-size: 14px;
                color: rgba(255, 255, 255, 0.5);
            }
        """.trimIndent()

        val defaultJs = """
            console.log("HyperFrames Scene 1 initialized");
        """.trimIndent()

        val emptyScene = HyperFrameScene(
            id = "scene-01",
            index = 0,
            title = "Scene 1",
            script = "",
            durationSec = 5.0f,
            transition = SceneTransitionType.FADE,
            composition = SceneComposition(
                backgroundColor = "#0c0e14",
                backgroundGradient = "radial-gradient(circle at 50% 30%, #162038 0%, #06080e 100%)",
                customHtml = defaultHtml,
                customCss = defaultCss,
                customJs = defaultJs
            ),
            elements = emptyList()
        )

        return HyperFramesProject(
            id = "MyProject",
            name = "My Project",
            aspectRatio = AspectRatioType.PORTRAIT_9_16,
            resolution = RenderResolution.HD_720P,
            fps = 30,
            scenes = listOf(emptyScene),
            assets = emptyList(),
            settings = ProjectSettings()
        )
    }
}
