package com.saalpa.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saalpa.data.HFProjectSummary
import com.saalpa.model.AspectRatioType
import com.saalpa.model.RenderResolution
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBg
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioDanger
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectManagerDialog(
    projects: List<HFProjectSummary>,
    currentProjectDir: File?,
    onDismiss: () -> Unit,
    onOpenProject: (File) -> Unit,
    onCreateNewProject: (name: String, ratio: AspectRatioType, res: RenderResolution) -> Unit,
    onDuplicateProject: (File) -> Unit,
    onRenameProject: (File, String) -> Unit,
    onDeleteProject: (File) -> Unit,
    onExportHfp: (File) -> Unit,
    onImportHfp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<HFProjectSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<HFProjectSummary?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(16.dp)),
            color = StudioBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioAccent)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "HF-projects",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Менеджер проектов",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "Файловая система устройства • Источник истины",
                                fontSize = 11.sp,
                                color = StudioTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = StudioTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar: New Project & Import
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showNewProjectDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_create_project")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Новый проект", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onImportHfp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_import_hfp")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = StudioAccentLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Импорт .hfp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ПРОЕКТЫ (${projects.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = StudioTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Проектов в HF-projects/ пока нет", color = StudioTextSecondary, fontSize = 13.sp)
                            Text("Создайте чистый проект выше", color = StudioTextMuted, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(projects, key = { it.dir.absolutePath }) { summary ->
                            val isCurrent = currentProjectDir?.absolutePath == summary.dir.absolutePath
                            ProjectCard(
                                summary = summary,
                                isCurrent = isCurrent,
                                formattedDate = dateFormat.format(Date(summary.lastModified)),
                                onOpen = {
                                    onOpenProject(summary.dir)
                                    onDismiss()
                                },
                                onRename = { renameTarget = summary },
                                onDuplicate = { onDuplicateProject(summary.dir) },
                                onDelete = { deleteTarget = summary },
                                onExport = { onExportHfp(summary.dir) }
                            )
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        var newProjName by remember { mutableStateOf("New Video") }
        var selectedRatio by remember { mutableStateOf(AspectRatioType.PORTRAIT_9_16) }
        var selectedRes by remember { mutableStateOf(RenderResolution.HD_720P) }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Создать чистый проект", color = StudioTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Будет создана папка в HF-projects/ с файлами project.json, index.html, style.css, script.js.",
                        color = StudioTextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newProjName,
                        onValueChange = { newProjName = it },
                        label = { Text("Название проекта") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAccent,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_project_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AspectRatioType.values().forEach { r ->
                            val isSel = selectedRatio == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) StudioAccent.copy(alpha = 0.2f) else StudioSurfaceVariant)
                                    .border(1.dp, if (isSel) StudioAccent else StudioBorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { selectedRatio = r }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = r.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) StudioAccentLight else StudioTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjName.isNotBlank()) {
                            onCreateNewProject(newProjName.trim(), selectedRatio, selectedRes)
                            showNewProjectDialog = false
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioAccent)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Отмена", color = StudioTextSecondary)
                }
            },
            containerColor = StudioSurfaceElevated
        )
    }

    // Rename Dialog
    renameTarget?.let { target ->
        var renameText by remember { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Переименовать проект", color = StudioTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Новое имя") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAccent,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameProject(target.dir, renameText.trim())
                            renameTarget = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioAccent)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text("Отмена", color = StudioTextSecondary)
                }
            },
            containerColor = StudioSurfaceElevated
        )
    }

    // Delete Confirmation Dialog
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Удалить проект?", color = StudioDanger) },
            text = {
                Text(
                    "Вы действительно хотите удалить проект «${target.name}» и всю его папку из HF-projects/? Это действие необратимо.",
                    color = StudioTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(target.dir)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioDanger)
                ) {
                    Text("Удалить навсегда")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Отмена", color = StudioTextSecondary)
                }
            },
            containerColor = StudioSurfaceElevated
        )
    }
}

@Composable
private fun ProjectCard(
    summary: HFProjectSummary,
    isCurrent: Boolean,
    formattedDate: String,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                if (isCurrent) StudioAccent else StudioBorder,
                RoundedCornerShape(10.dp)
            ),
        color = if (isCurrent) StudioSurfaceVariant else StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isCurrent) StudioAccentLight else StudioTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = summary.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StudioAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "АКТИВЕН",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioAccentLight
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${summary.sceneCount} сцен • ${summary.totalDurationSec}с • ${summary.aspectRatio.label} • $formattedDate",
                            fontSize = 10.5.sp,
                            color = StudioTextMuted
                        )
                    }
                }

                // Quick Open Button
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrent) StudioSurfaceElevated else StudioAccent,
                        contentColor = if (isCurrent) StudioTextPrimary else Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCurrent) "В студии" else "Открыть", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action toolbar: Rename, Duplicate, Export .hfp, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = "Переименовать",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Дублировать",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(onClick = onExport, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = "Экспорт в .hfp",
                        tint = StudioAccentLight,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = StudioDanger,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
