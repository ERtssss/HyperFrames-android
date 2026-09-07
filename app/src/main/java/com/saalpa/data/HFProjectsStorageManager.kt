package com.saalpa.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.saalpa.model.AspectRatioType
import com.saalpa.model.RenderResolution
import com.saalpa.model.project.ElementType
import com.saalpa.model.project.ElementTiming
import com.saalpa.model.project.ElementTransform
import com.saalpa.model.project.HyperFrameAnimation
import com.saalpa.model.project.HyperFrameElement
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.HyperFramesProject
import com.saalpa.model.project.ProjectSettings
import com.saalpa.model.project.SceneComposition
import com.saalpa.model.project.SceneTransitionType
import com.saalpa.model.project.SceneVoiceSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Summary descriptor of a project stored inside HF-projects/
 */
data class HFProjectSummary(
    val dirName: String,
    val name: String,
    val dir: File,
    val lastModified: Long,
    val sceneCount: Int,
    val totalDurationSec: Float,
    val aspectRatio: AspectRatioType,
    val resolution: RenderResolution
)

/**
 * Central filesystem manager for HyperFrames projects stored directly in HF-projects/.
 * Follows the file-based architecture where HTML/CSS/JS files on disk are the primary
 * source of truth.
 */
class HFProjectsStorageManager(private val context: Context) {

    companion object {
        private const val TAG = "HFProjectsStorage"
        const val HF_PROJECTS_DIR_NAME = "HF-projects"
        const val PROJECT_JSON = "project.json"
        const val SCENE_JSON = "scene.json"
        const val INDEX_HTML = "index.html"
        const val STYLE_CSS = "style.css"
        const val SCRIPT_JS = "script.js"
        const val SCENES_DIR = "scenes"
        const val ASSETS_DIR = "assets"
    }

    /**
     * Resolves the primary HF-projects/ directory on the device.
     * Prioritizes external app files directory or documents directory so projects can be
     * easily browsed, transferred, or backed up.
     */
    fun getHfProjectsRoot(): File {
        val candidates = listOfNotNull(
            context.getExternalFilesDir(null)?.let { File(it, HF_PROJECTS_DIR_NAME) },
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), HF_PROJECTS_DIR_NAME),
            File(context.filesDir, HF_PROJECTS_DIR_NAME)
        )

        for (dir in candidates) {
            try {
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                if (dir.exists() && dir.canWrite()) {
                    return dir
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not use directory ${dir.absolutePath}: ${e.message}")
            }
        }

        val fallback = File(context.filesDir, HF_PROJECTS_DIR_NAME)
        if (!fallback.exists()) fallback.mkdirs()
        return fallback
    }

    /**
     * Initializes the HF-projects storage upon app startup.
     * Ensures directory exists and creates an initial clean project if empty.
     */
    fun ensureStorageInitialized(): File {
        val root = getHfProjectsRoot()
        val projects = listProjects()
        if (projects.isEmpty()) {
            createNewProject(
                name = "My Project",
                aspectRatio = AspectRatioType.PORTRAIT_9_16,
                resolution = RenderResolution.HD_720P
            )
        }
        return root
    }

    /**
     * Lists all projects found inside HF-projects/
     */
    fun listProjects(): List<HFProjectSummary> {
        val root = getHfProjectsRoot()
        val projectDirs = root.listFiles { file -> file.isDirectory } ?: emptyArray()

        return projectDirs.mapNotNull { dir ->
            try {
                val projJsonFile = File(dir, PROJECT_JSON)
                if (projJsonFile.exists()) {
                    val json = JSONObject(projJsonFile.readText(StandardCharsets.UTF_8))
                    val name = json.optString("name", dir.name)
                    val scenesArray = json.optJSONArray("scenes") ?: JSONArray()
                    val sceneCount = scenesArray.length()
                    val ratioStr = json.optString("aspectRatio", AspectRatioType.PORTRAIT_9_16.name)
                    val resStr = json.optString("resolution", RenderResolution.HD_720P.name)
                    val ratio = try { AspectRatioType.valueOf(ratioStr) } catch (e: Exception) { AspectRatioType.PORTRAIT_9_16 }
                    val res = try { RenderResolution.valueOf(resStr) } catch (e: Exception) { RenderResolution.HD_720P }

                    var totalDur = 0f
                    val scenesDir = File(dir, SCENES_DIR)
                    if (scenesDir.exists()) {
                        scenesDir.listFiles { f -> f.isDirectory }?.forEach { scDir ->
                            val scJson = File(scDir, SCENE_JSON)
                            if (scJson.exists()) {
                                try {
                                    val scObj = JSONObject(scJson.readText(StandardCharsets.UTF_8))
                                    totalDur += scObj.optDouble("duration", 5.0).toFloat()
                                } catch (_: Exception) {}
                            }
                        }
                    }

                    HFProjectSummary(
                        dirName = dir.name,
                        name = name,
                        dir = dir,
                        lastModified = dir.lastModified(),
                        sceneCount = if (sceneCount > 0) sceneCount else 1,
                        totalDurationSec = if (totalDur > 0f) totalDur else 5.0f,
                        aspectRatio = ratio,
                        resolution = res
                    )
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error inspecting project dir ${dir.name}", e)
                null
            }
        }.sortedByDescending { it.lastModified }
    }

    /**
     * Creates a new clean project directory inside HF-projects/
     * with project.json and an initial empty scene-01.
     */
    fun createNewProject(
        name: String = "My Project",
        aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
        resolution: RenderResolution = RenderResolution.HD_720P
    ): HyperFramesProject {
        val root = getHfProjectsRoot()
        val safeName = sanitizeDirectoryName(name)
        var targetDir = File(root, safeName)
        var counter = 1
        while (targetDir.exists()) {
            targetDir = File(root, "${safeName}_$counter")
            counter++
        }
        targetDir.mkdirs()

        // Create assets/
        File(targetDir, ASSETS_DIR).mkdirs()

        // Create scenes/scene-01/
        val scenesDir = File(targetDir, SCENES_DIR).apply { mkdirs() }
        val scene01Dir = File(scenesDir, "scene-01").apply { mkdirs() }
        File(scene01Dir, ASSETS_DIR).mkdirs()

        val initialHtml = """
            <div id="stage">
                <div class="empty-canvas-guide">
                    <h1>EMPTY CANVAS</h1>
                    <p>Edit HTML, CSS, JS or add elements</p>
                </div>
            </div>
        """.trimIndent()

        val initialCss = """
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

        val initialJs = """
            // HyperFrames Scene 1 Logic
            console.log("Scene 1 initialized");
        """.trimIndent()

        File(scene01Dir, INDEX_HTML).writeText(initialHtml, StandardCharsets.UTF_8)
        File(scene01Dir, STYLE_CSS).writeText(initialCss, StandardCharsets.UTF_8)
        File(scene01Dir, SCRIPT_JS).writeText(initialJs, StandardCharsets.UTF_8)

        val scene01Json = JSONObject().apply {
            put("id", "scene-01")
            put("name", "Scene 1")
            put("duration", 5.0)
            put("script", "")
            put("transition", "FADE")
            put("backgroundColor", "#0c0e14")
            put("backgroundGradient", "radial-gradient(circle at 50% 30%, #162038 0%, #06080e 100%)")
            put("elements", JSONArray())
            put("voice", JSONObject().apply {
                put("speakerName", "Rachel (Pro Studio)")
                put("speed", 1.0)
                put("pitch", 1.0)
                put("volume", 1.0)
                put("audioFile", "")
            })
        }
        File(scene01Dir, SCENE_JSON).writeText(scene01Json.toString(2), StandardCharsets.UTF_8)

        val projectDims = getDimensionsFor(aspectRatio, resolution)
        val projectJson = JSONObject().apply {
            put("name", name)
            put("version", 1)
            put("width", projectDims.first)
            put("height", projectDims.second)
            put("fps", 30)
            put("aspectRatio", aspectRatio.name)
            put("resolution", resolution.name)
            put("scenes", JSONArray().put("scene-01"))
            put("settings", JSONObject().apply {
                put("autoPlayScenes", true)
                put("loopPlayback", false)
                put("showSafeArea", false)
                put("showGrid", false)
            })
        }
        File(targetDir, PROJECT_JSON).writeText(projectJson.toString(2), StandardCharsets.UTF_8)

        return loadProject(targetDir)
    }

    /**
     * Loads a project directly from its filesystem directory in HF-projects/
     */
    fun loadProject(projectDir: File): HyperFramesProject {
        val projJsonFile = File(projectDir, PROJECT_JSON)
        if (!projJsonFile.exists()) {
            throw IllegalArgumentException("project.json not found in ${projectDir.absolutePath}")
        }

        val json = JSONObject(projJsonFile.readText(StandardCharsets.UTF_8))
        val name = json.optString("name", projectDir.name)
        val ratioStr = json.optString("aspectRatio", AspectRatioType.PORTRAIT_9_16.name)
        val resStr = json.optString("resolution", RenderResolution.HD_720P.name)
        val fps = json.optInt("fps", 30)
        val ratio = try { AspectRatioType.valueOf(ratioStr) } catch (e: Exception) { AspectRatioType.PORTRAIT_9_16 }
        val res = try { RenderResolution.valueOf(resStr) } catch (e: Exception) { RenderResolution.HD_720P }

        val scenesDir = File(projectDir, SCENES_DIR)
        val scenesArray = json.optJSONArray("scenes") ?: JSONArray()
        val sceneNames = mutableListOf<String>()
        for (i in 0 until scenesArray.length()) {
            sceneNames.add(scenesArray.getString(i))
        }

        val loadedScenes = mutableListOf<HyperFrameScene>()
        val dirsToLoad = if (sceneNames.isNotEmpty()) {
            sceneNames.map { File(scenesDir, it) }.filter { it.exists() && it.isDirectory }
        } else {
            scenesDir.listFiles { f -> f.isDirectory }?.toList() ?: emptyList()
        }

        dirsToLoad.forEachIndexed { idx, scDir ->
            val scJsonFile = File(scDir, SCENE_JSON)
            val scJson = if (scJsonFile.exists()) {
                JSONObject(scJsonFile.readText(StandardCharsets.UTF_8))
            } else {
                JSONObject()
            }

            val htmlFile = File(scDir, INDEX_HTML)
            val cssFile = File(scDir, STYLE_CSS)
            val jsFile = File(scDir, SCRIPT_JS)

            val htmlContent = if (htmlFile.exists()) htmlFile.readText(StandardCharsets.UTF_8) else ""
            val cssContent = if (cssFile.exists()) cssFile.readText(StandardCharsets.UTF_8) else ""
            val jsContent = if (jsFile.exists()) jsFile.readText(StandardCharsets.UTF_8) else ""

            val scId = scJson.optString("id", scDir.name)
            val scTitle = scJson.optString("name", "Scene ${idx + 1}")
            val scDuration = scJson.optDouble("duration", 5.0).toFloat()
            val scScript = scJson.optString("script", "")
            val scTransStr = scJson.optString("transition", "FADE")
            val scTrans = try { SceneTransitionType.valueOf(scTransStr) } catch (e: Exception) { SceneTransitionType.FADE }
            val bgCol = scJson.optString("backgroundColor", "#0c0e14")
            val bgGrad = scJson.optString("backgroundGradient", "")

            // Parse voice settings
            val voiceObj = scJson.optJSONObject("voice")
            val voiceSettings = if (voiceObj != null) {
                val voiceAudioFile = voiceObj.optString("audioFile", "")
                val resolvedAudioPath = if (voiceAudioFile.isNotBlank()) {
                    File(scDir, voiceAudioFile).absolutePath
                } else null
                SceneVoiceSettings(
                    speakerName = voiceObj.optString("speakerName", "Rachel (Pro Studio)"),
                    speed = voiceObj.optDouble("speed", 1.0).toFloat(),
                    pitch = voiceObj.optDouble("pitch", 1.0).toFloat(),
                    volume = voiceObj.optDouble("volume", 1.0).toFloat(),
                    audioPath = resolvedAudioPath
                )
            } else {
                SceneVoiceSettings()
            }

            loadedScenes.add(
                HyperFrameScene(
                    id = scId,
                    index = idx,
                    title = scTitle,
                    script = scScript,
                    durationSec = scDuration,
                    voice = voiceSettings,
                    composition = SceneComposition(
                        backgroundColor = bgCol,
                        backgroundGradient = bgGrad,
                        customHtml = htmlContent,
                        customCss = cssContent,
                        customJs = jsContent
                    ),
                    transition = scTrans,
                    elements = emptyList()
                )
            )
        }

        val finalScenes = if (loadedScenes.isEmpty()) {
            listOf(
                HyperFrameScene(
                    id = "scene-01",
                    index = 0,
                    title = "Scene 1",
                    durationSec = 5.0f
                )
            )
        } else loadedScenes

        val settingsObj = json.optJSONObject("settings")
        val settings = if (settingsObj != null) {
            ProjectSettings(
                autoPlayScenes = settingsObj.optBoolean("autoPlayScenes", true),
                loopPlayback = settingsObj.optBoolean("loopPlayback", false),
                showSafeArea = settingsObj.optBoolean("showSafeArea", false),
                showGrid = settingsObj.optBoolean("showGrid", false)
            )
        } else ProjectSettings()

        return HyperFramesProject(
            id = projectDir.name,
            name = name,
            aspectRatio = ratio,
            resolution = res,
            fps = fps,
            scenes = finalScenes,
            assets = emptyList(),
            settings = settings
        )
    }

    /**
     * Saves the entire project structure to disk inside HF-projects/
     */
    fun saveProject(project: HyperFramesProject, projectDir: File) {
        if (!projectDir.exists()) projectDir.mkdirs()

        val scenesDir = File(projectDir, SCENES_DIR).apply { mkdirs() }
        val scenesArray = JSONArray()

        project.scenes.forEachIndexed { index, scene ->
            val sceneFolderName = if (scene.id.startsWith("scene-") || scene.id.startsWith("scene_")) {
                scene.id.replace("_", "-")
            } else {
                "scene-${String.format("%02d", index + 1)}"
            }
            scenesArray.put(sceneFolderName)

            val scDir = File(scenesDir, sceneFolderName).apply { mkdirs() }
            val assetsDir = File(scDir, ASSETS_DIR).apply { mkdirs() }

            // Write HTML / CSS / JS files directly
            File(scDir, INDEX_HTML).writeText(scene.composition.customHtml, StandardCharsets.UTF_8)
            File(scDir, STYLE_CSS).writeText(scene.composition.customCss, StandardCharsets.UTF_8)
            File(scDir, SCRIPT_JS).writeText(scene.composition.customJs, StandardCharsets.UTF_8)

            // Write scene.json
            val voiceRelativePath = scene.voice.audioPath?.let { fullPath ->
                try {
                    val audioF = File(fullPath)
                    if (audioF.exists() && audioF.parentFile?.absolutePath == assetsDir.absolutePath) {
                        "assets/${audioF.name}"
                    } else if (audioF.exists()) {
                        val dest = File(assetsDir, "voice_${scene.id}.mp3")
                        audioF.copyTo(dest, overwrite = true)
                        "assets/${dest.name}"
                    } else null
                } catch (e: Exception) {
                    null
                }
            } ?: ""

            val scJson = JSONObject().apply {
                put("id", scene.id)
                put("name", scene.title)
                put("duration", scene.durationSec.toDouble())
                put("script", scene.script)
                put("transition", scene.transition.name)
                put("backgroundColor", scene.composition.backgroundColor)
                put("backgroundGradient", scene.composition.backgroundGradient)
                put("voice", JSONObject().apply {
                    put("speakerName", scene.voice.speakerName)
                    put("speed", scene.voice.speed.toDouble())
                    put("pitch", scene.voice.pitch.toDouble())
                    put("volume", scene.voice.volume.toDouble())
                    put("audioFile", voiceRelativePath)
                })
            }
            File(scDir, SCENE_JSON).writeText(scJson.toString(2), StandardCharsets.UTF_8)
        }

        val dims = project.getEffectiveDimensions()
        val projJson = JSONObject().apply {
            put("name", project.name)
            put("version", 1)
            put("width", dims.first)
            put("height", dims.second)
            put("fps", project.fps)
            put("aspectRatio", project.aspectRatio.name)
            put("resolution", project.resolution.name)
            put("scenes", scenesArray)
            put("settings", JSONObject().apply {
                put("autoPlayScenes", project.settings.autoPlayScenes)
                put("loopPlayback", project.settings.loopPlayback)
                put("showSafeArea", project.settings.showSafeArea)
                put("showGrid", project.settings.showGrid)
            })
        }
        File(projectDir, PROJECT_JSON).writeText(projJson.toString(2), StandardCharsets.UTF_8)
    }

    /**
     * Saves code changes directly to the scene's index.html, style.css, or script.js file.
     */
    fun saveSceneCodeFiles(
        projectDir: File,
        sceneId: String,
        html: String,
        css: String,
        js: String
    ) {
        val scDir = findSceneDirectory(projectDir, sceneId) ?: return
        File(scDir, INDEX_HTML).writeText(html, StandardCharsets.UTF_8)
        File(scDir, STYLE_CSS).writeText(css, StandardCharsets.UTF_8)
        File(scDir, SCRIPT_JS).writeText(js, StandardCharsets.UTF_8)
    }

    /**
     * Locates a scene folder matching the scene ID.
     */
    fun findSceneDirectory(projectDir: File, sceneId: String): File? {
        val scenesDir = File(projectDir, SCENES_DIR)
        if (!scenesDir.exists()) return null
        // 1. Direct folder match
        val direct = File(scenesDir, sceneId)
        if (direct.exists() && direct.isDirectory) return direct
        // 2. Search by scene.json id
        scenesDir.listFiles { f -> f.isDirectory }?.forEach { dir ->
            val jsonF = File(dir, SCENE_JSON)
            if (jsonF.exists()) {
                try {
                    val obj = JSONObject(jsonF.readText(StandardCharsets.UTF_8))
                    if (obj.optString("id") == sceneId) return dir
                } catch (_: Exception) {}
            }
        }
        return null
    }

    /**
     * Adds a new empty scene directory with clean HTML/CSS/JS files to a project.
     */
    fun createScene(
        projectDir: File,
        sceneNumber: Int,
        title: String = "Scene $sceneNumber"
    ): HyperFrameScene {
        val sceneFolderName = "scene-${String.format("%02d", sceneNumber)}"
        val scenesDir = File(projectDir, SCENES_DIR).apply { mkdirs() }
        var scDir = File(scenesDir, sceneFolderName)
        var counter = 1
        while (scDir.exists()) {
            scDir = File(scenesDir, "${sceneFolderName}_$counter")
            counter++
        }
        scDir.mkdirs()
        File(scDir, ASSETS_DIR).mkdirs()

        val html = """
            <div id="stage">
                <div class="scene-container">
                    <h1 class="title">$title</h1>
                </div>
            </div>
        """.trimIndent()

        val css = """
            #stage {
                position: absolute;
                inset: 0;
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #0c0e14;
            }

            .title {
                font-family: 'Montserrat', sans-serif;
                font-size: 48px;
                font-weight: 800;
                color: #ffffff;
                letter-spacing: 1px;
            }
        """.trimIndent()

        val js = """
            console.log("$title initialized");
        """.trimIndent()

        File(scDir, INDEX_HTML).writeText(html, StandardCharsets.UTF_8)
        File(scDir, STYLE_CSS).writeText(css, StandardCharsets.UTF_8)
        File(scDir, SCRIPT_JS).writeText(js, StandardCharsets.UTF_8)

        val newId = UUID.randomUUID().toString().take(8)
        val scJson = JSONObject().apply {
            put("id", newId)
            put("name", title)
            put("duration", 5.0)
            put("script", "")
            put("transition", "FADE")
            put("backgroundColor", "#0c0e14")
            put("backgroundGradient", "")
            put("voice", JSONObject().apply {
                put("speakerName", "Rachel (Pro Studio)")
                put("speed", 1.0)
                put("pitch", 1.0)
                put("volume", 1.0)
                put("audioFile", "")
            })
        }
        File(scDir, SCENE_JSON).writeText(scJson.toString(2), StandardCharsets.UTF_8)

        return HyperFrameScene(
            id = newId,
            index = sceneNumber - 1,
            title = title,
            durationSec = 5.0f,
            composition = SceneComposition(
                customHtml = html,
                customCss = css,
                customJs = js
            )
        )
    }

    /**
     * Duplicates an existing project in HF-projects/
     */
    fun duplicateProject(sourceDir: File): File {
        val root = getHfProjectsRoot()
        val copyName = "${sourceDir.name}_copy"
        var targetDir = File(root, copyName)
        var counter = 1
        while (targetDir.exists()) {
            targetDir = File(root, "${copyName}_$counter")
            counter++
        }
        sourceDir.copyRecursively(targetDir, overwrite = false)

        val projJsonFile = File(targetDir, PROJECT_JSON)
        if (projJsonFile.exists()) {
            try {
                val json = JSONObject(projJsonFile.readText(StandardCharsets.UTF_8))
                val oldName = json.optString("name", sourceDir.name)
                json.put("name", "$oldName (Copy)")
                projJsonFile.writeText(json.toString(2), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating copied project name", e)
            }
        }
        return targetDir
    }

    /**
     * Renames a project on the filesystem.
     */
    fun renameProject(projectDir: File, newName: String): File {
        val projJsonFile = File(projectDir, PROJECT_JSON)
        if (projJsonFile.exists()) {
            try {
                val json = JSONObject(projJsonFile.readText(StandardCharsets.UTF_8))
                json.put("name", newName)
                projJsonFile.writeText(json.toString(2), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming project in JSON", e)
            }
        }
        return projectDir
    }

    /**
     * Deletes a project directory completely from the filesystem.
     */
    fun deleteProject(projectDir: File): Boolean {
        return projectDir.deleteRecursively()
    }

    /**
     * Exports the project into a portable .hfp zip archive.
     */
    fun exportProjectToHfp(projectDir: File, outputHfpFile: File): File {
        if (outputHfpFile.exists()) outputHfpFile.delete()
        outputHfpFile.parentFile?.mkdirs()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputHfpFile))).use { zos ->
            projectDir.walkTopDown().forEach { file ->
                val relPath = file.relativeTo(projectDir).path
                if (relPath.isNotBlank()) {
                    if (file.isDirectory) {
                        zos.putNextEntry(ZipEntry("$relPath/"))
                        zos.closeEntry()
                    } else {
                        zos.putNextEntry(ZipEntry(relPath))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
        }
        return outputHfpFile
    }

    /**
     * Imports a project from an .hfp archive into HF-projects/
     */
    fun importProjectFromHfp(hfpFile: File): File {
        val root = getHfProjectsRoot()
        val baseName = sanitizeDirectoryName(hfpFile.nameWithoutExtension.removeSuffix(".hfp"))
        var targetDir = File(root, baseName)
        var counter = 1
        while (targetDir.exists()) {
            targetDir = File(root, "${baseName}_$counter")
            counter++
        }
        targetDir.mkdirs()

        ZipInputStream(BufferedInputStream(FileInputStream(hfpFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        return targetDir
    }

    private fun sanitizeDirectoryName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_").trim().ifBlank { "Project_${System.currentTimeMillis()}" }
    }

    private fun getDimensionsFor(aspectRatio: AspectRatioType, resolution: RenderResolution): Pair<Int, Int> {
        val baseDim = when (resolution) {
            RenderResolution.SD_540P -> 540
            RenderResolution.HD_720P -> 720
            RenderResolution.FHD_1080P -> 1080
        }
        return when (aspectRatio) {
            AspectRatioType.PORTRAIT_9_16 -> Pair((baseDim / 2) * 2, (((baseDim * 16) / 9) / 2) * 2)
            AspectRatioType.SQUARE_1_1 -> Pair((baseDim / 2) * 2, (baseDim / 2) * 2)
            AspectRatioType.LANDSCAPE_16_9 -> Pair((((baseDim * 16) / 9) / 2) * 2, (baseDim / 2) * 2)
            AspectRatioType.PORTRAIT_4_5 -> Pair((baseDim / 2) * 2, (((baseDim * 5) / 4) / 2) * 2)
        }
    }
}
