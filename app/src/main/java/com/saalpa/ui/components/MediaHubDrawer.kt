package com.saalpa.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.data.ImportedZipAsset
import com.saalpa.model.MediaType
import com.saalpa.ui.StudioUiState
import com.saalpa.ui.StudioViewModel
import com.saalpa.ui.theme.CapCutBlue
import com.saalpa.ui.theme.CapCutCardBorder
import com.saalpa.ui.theme.CapCutCardBorderSubtle
import com.saalpa.ui.theme.CapCutCyan
import com.saalpa.ui.theme.CapCutGreen
import com.saalpa.ui.theme.CapCutPink
import com.saalpa.ui.theme.CapCutSurface
import com.saalpa.ui.theme.CapCutSurfaceVariant
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary

enum class MediaFilterType(val label: String, val mediaType: MediaType?) {
    ALL("Все файлы", null),
    AUDIO("🎵 Аудио (MP3)", MediaType.BGM),
    VIDEO("🎬 Видео (MP4)", MediaType.VIDEO),
    IMAGE("🖼️ Фото (PNG/JPG)", MediaType.PHOTO)
}

@Composable
fun MediaHubDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedFilter by remember { mutableStateOf(MediaFilterType.ALL) }

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importZipArchive(it) }
    }

    val filesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMediaFiles(uris)
        }
    }

    val filteredAssets = remember(state.importedAssets, selectedFilter) {
        if (selectedFilter.mediaType == null) {
            state.importedAssets
        } else {
            state.importedAssets.filter { it.mediaType == selectedFilter.mediaType }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CapCutSurface)
    ) {
        // Top Action Bar: Upload ZIP & Files
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CapCutSurfaceVariant,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upload ZIP Button (Prominent)
                    Button(
                        onClick = { zipPicker.launch("application/zip") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CapCutCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("upload_zip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Импорт ZIP-папок",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Upload Single/Multiple Files Button
                    OutlinedButton(
                        onClick = { filesPicker.launch("*/*") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CapCutCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_files_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Файлы",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MediaFilterType.values().forEach { filter ->
                        val count = if (filter.mediaType == null) {
                            state.importedAssets.size
                        } else {
                            state.importedAssets.count { it.mediaType == filter.mediaType }
                        }

                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CapCutCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CapCutCyan,
                                containerColor = Color.Transparent,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = if (selectedFilter == filter) CapCutCyan else CapCutCardBorderSubtle
                            )
                        )
                    }
                }
            }
        }

        // Content / List
        if (state.isImportingZip) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = CapCutCyan, modifier = Modifier.size(32.dp))
                    Text(
                        text = "Распаковка ZIP-архива и каталогов...",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Поиск MP3, MP4, PNG, JPEG",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        } else if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Медиа-хаб пуст",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = "Загрузите ZIP-архив со структурой папок или файлы (MP3, MP4, PNG, JPG) для использования в ролике и HTML/CSS шаблоне.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredAssets, key = { it.id }) { asset ->
                    ImportedAssetCard(
                        asset = asset,
                        onAddToTimeline = {
                            viewModel.addImportedAssetToTimeline(asset)
                            Toast.makeText(context, "Добавлено на таймлайн: ${asset.name}", Toast.LENGTH_SHORT).show()
                        },
                        onCopyCode = {
                            val codeSnippet = when (asset.mediaType) {
                                MediaType.PHOTO -> "<img src=\"${asset.dataUrl ?: asset.file.name}\" class=\"custom-asset\" />"
                                MediaType.VIDEO -> "<video src=\"${asset.dataUrl ?: asset.file.name}\" autoplay loop muted></video>"
                                MediaType.BGM, MediaType.VOICEOVER -> "<audio src=\"${asset.dataUrl ?: asset.file.name}\"></audio>"
                                else -> asset.dataUrl ?: asset.file.name
                            }
                            clipboardManager.setText(AnnotatedString(codeSnippet))
                            Toast.makeText(context, "Код скопирован в буфер", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { viewModel.deleteImportedAsset(asset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportedAssetCard(
    asset: ImportedZipAsset,
    onAddToTimeline: () -> Unit,
    onCopyCode: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CapCutSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon & Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (asset.mediaType) {
                                MediaType.BGM, MediaType.VOICEOVER -> CapCutPink.copy(alpha = 0.2f)
                                MediaType.VIDEO -> CapCutBlue.copy(alpha = 0.2f)
                                else -> CapCutCyan.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (asset.mediaType) {
                            MediaType.BGM, MediaType.VOICEOVER -> Icons.Default.MusicNote
                            MediaType.VIDEO -> Icons.Default.Movie
                            else -> Icons.Default.Image
                        },
                        contentDescription = null,
                        tint = when (asset.mediaType) {
                            MediaType.BGM, MediaType.VOICEOVER -> CapCutPink
                            MediaType.VIDEO -> CapCutBlue
                            else -> CapCutCyan
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = asset.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = asset.folderName,
                                fontSize = 8.sp,
                                color = CapCutCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = asset.formattedSize,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Quick Actions: Add to Timeline, Copy Code, Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add to Timeline Button
                Button(
                    onClick = onAddToTimeline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CapCutCyan.copy(alpha = 0.2f),
                        contentColor = CapCutCyan
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("+ Слой", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Copy HTML tag button
                IconButton(onClick = onCopyCode, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Копировать код",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Delete button
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
