package com.saalpa.model.project

import com.saalpa.model.AspectRatioType
import com.saalpa.model.RenderResolution
import java.util.UUID

object DefaultProjectFactory {

    fun createDefaultProject(): HyperFramesProject {
        val emptyScene = HyperFrameScene(
            id = "scene_01",
            index = 0,
            title = "Scene 1",
            script = "",
            durationSec = 5.0f,
            transition = SceneTransitionType.FADE,
            composition = SceneComposition(
                backgroundColor = "#0c0e14",
                backgroundGradient = "radial-gradient(circle at 50% 30%, #162038 0%, #06080e 100%)"
            ),
            elements = emptyList()
        )

        return HyperFramesProject(
            id = "proj_${UUID.randomUUID().toString().take(8)}",
            name = "Новый проект",
            aspectRatio = AspectRatioType.PORTRAIT_9_16,
            resolution = RenderResolution.HD_720P,
            fps = 30,
            scenes = listOf(emptyScene),
            assets = emptyList(),
            settings = ProjectSettings()
        )
    }
}
