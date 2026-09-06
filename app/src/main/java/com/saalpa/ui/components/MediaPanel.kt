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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.saalpa.model.MediaAssetItem
import com.saalpa.model.MediaCategoryType
import com.saalpa.model.project.ElementType
import com.saalpa.model.project.ProjectAsset
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentContainer
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioSky
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary

@Composable
fun MediaPanel(
    importedAssets: List<MediaAssetItem>,
    onImportLocalMedia: () -> Unit,
    onImportZipArchive: () -> Unit,
    onAddAssetToScene: (MediaAssetItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(MediaCategoryType.IMAGES) }

    val presetAssets = remember {
        listOf(
            MediaAssetItem("pr_1", "Cyberpunk Grid", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop&q=80", MediaCategoryType.IMAGES),
            MediaAssetItem("pr_2", "Esports Stage", "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80", MediaCategoryType.IMAGES),
            MediaAssetItem("pr_3", "Tech Neon", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=500&auto=format&fit=crop&q=80", MediaCategoryType.IMAGES),
            MediaAssetItem("pr_4", "Abstract Motion", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=80", MediaCategoryType.IMAGES)
        )
    }

    val displayAssets = (importedAssets.filter { it.category == selectedCategory } + presetAssets.filter { it.category == selectedCategory })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioSurface)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STUDIO MEDIA HUB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Импорт файлов и ZIP архивов",
                    fontSize = 10.sp,
                    color = StudioTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Import Actions: File and ZIP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onImportLocalMedia,
                colors = ButtonDefaults.buttonColors(containerColor = StudioAccent, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).height(32.dp).testTag("btn_import_file")
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Загрузить файл", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onImportZipArchive,
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant, contentColor = StudioSky),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).height(32.dp).border(1.dp, StudioBorder, RoundedCornerShape(6.dp)).testTag("btn_import_zip")
            ) {
                Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(14.dp), tint = StudioSky)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Импорт ZIP", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf(
                MediaCategoryType.IMAGES to "Фото",
                MediaCategoryType.VIDEOS to "Видео",
                MediaCategoryType.AUDIO to "Аудио"
            )
            categories.forEach { (cat, title) ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) StudioAccentContainer else StudioSurfaceVariant)
                        .border(1.dp, if (isSelected) StudioAccentLight else StudioBorder, RoundedCornerShape(6.dp))
                        .clickable { selectedCategory = cat }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else StudioTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Media Assets
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayAssets) { asset ->
                Surface(
                    modifier = Modifier
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { onAddAssetToScene(asset) },
                    color = StudioSurfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (asset.category == MediaCategoryType.IMAGES) {
                            AsyncImage(
                                model = asset.uri,
                                contentDescription = asset.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (asset.category == MediaCategoryType.AUDIO) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(StudioSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = StudioAccentLight, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(StudioSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = StudioSky, modifier = Modifier.size(32.dp))
                            }
                        }

                        // Bottom Title Bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0xCC0E1015))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = asset.name,
                                    fontSize = 9.5.sp,
                                    color = StudioTextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.Add, contentDescription = "Добавить", tint = StudioAccentLight, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
