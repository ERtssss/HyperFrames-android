package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.HyperFramesProject
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioPink
import com.saalpa.ui.theme.StudioSky
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import java.io.File

/**
 * Files view representing the project on the device filesystem inside HF-projects/
 */
@Composable
fun ProjectFilesView(
    project: HyperFramesProject,
    currentProjectDir: File?,
    onOpenCodeFile: (sceneId: String, tabIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = StudioAccentLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "HF-projects/${project.name}/",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Файлы проекта на диске • Нажмите для редактирования",
                        fontSize = 10.sp,
                        color = StudioTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // project.json
                item {
                    FileTreeItem(
                        name = "project.json",
                        detail = "Конфигурация проекта (${project.aspectRatio.label}, ${project.fps} FPS)",
                        icon = Icons.Default.DataObject,
                        iconTint = StudioAccentLight,
                        onClick = {}
                    )
                }

                // scenes/ folder
                item {
                    Text(
                        text = "SCENES /",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextMuted,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(project.scenes.size) { idx ->
                    val scene = project.scenes[idx]
                    SceneFolderItem(
                        scene = scene,
                        onOpenHtml = { onOpenCodeFile(scene.id, 0) },
                        onOpenCss = { onOpenCodeFile(scene.id, 1) },
                        onOpenJs = { onOpenCodeFile(scene.id, 2) }
                    )
                }

                // assets/ folder
                item {
                    Text(
                        text = "ASSETS /",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextMuted,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                item {
                    FileTreeItem(
                        name = "assets/",
                        detail = "Локальные медиа, аудио, изображения",
                        icon = Icons.Default.PermMedia,
                        iconTint = StudioTextMuted,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun SceneFolderItem(
    scene: HyperFrameScene,
    onOpenHtml: () -> Unit,
    onOpenCss: () -> Unit,
    onOpenJs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, StudioBorderSubtle, RoundedCornerShape(8.dp)),
        color = StudioSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = StudioAccentLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${scene.id}/ (${scene.title})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = StudioTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // scene.json
                SubFileRow(
                    name = "scene.json",
                    info = "Параметры сцены",
                    icon = Icons.Default.DataObject,
                    tint = StudioTextSecondary,
                    onClick = {}
                )

                // index.html
                SubFileRow(
                    name = "index.html",
                    info = "HTML разметка",
                    icon = Icons.Default.Html,
                    tint = StudioPink,
                    onClick = onOpenHtml,
                    showEditIcon = true
                )

                // style.css
                SubFileRow(
                    name = "style.css",
                    info = "CSS стили & анимации",
                    icon = Icons.Default.Css,
                    tint = StudioSky,
                    onClick = onOpenCss,
                    showEditIcon = true
                )

                // script.js
                SubFileRow(
                    name = "script.js",
                    info = "JavaScript логика & таймлайн",
                    icon = Icons.Default.Javascript,
                    tint = StudioAccentLight,
                    onClick = onOpenJs,
                    showEditIcon = true
                )
            }
        }
    }
}

@Composable
private fun SubFileRow(
    name: String,
    info: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    showEditIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• $info",
                fontSize = 9.5.sp,
                color = StudioTextMuted
            )
        }

        if (showEditIcon) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Редактировать",
                tint = StudioAccentLight,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun FileTreeItem(
    name: String,
    detail: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() },
        color = StudioSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = StudioTextPrimary
                )
                Text(
                    text = detail,
                    fontSize = 9.5.sp,
                    color = StudioTextMuted
                )
            }
        }
    }
}
