package com.saalpa.ui.components

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.MediaOverlayItem
import com.saalpa.model.MediaType
import com.saalpa.model.VideoEffectType
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
import java.util.Locale

enum class ElementsCategory(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TEXT("Текст", Icons.Default.TextFields),
    OVERLAYS("Слои (PIP)", Icons.Default.Layers),
    EFFECTS("Эффекты (FX)", Icons.Default.AutoAwesome)
}

@Composable
fun ElementsDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ElementsCategory.TEXT) }
    val selectedItem = state.selectedItem

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CapCutSurface)
    ) {
        // Sub-Navigation Categories Bar (Text, Overlays, FX)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CapCutSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElementsCategory.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedCategory = cat }
                        .testTag("element_cat_${cat.name}"),
                    color = if (isSelected) CapCutCyan.copy(alpha = 0.18f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CapCutCyan else CapCutCardBorderSubtle
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = null,
                            tint = if (isSelected) CapCutCyan else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = cat.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CapCutCyan else TextMuted
                        )
                    }
                }
            }
        }

        // Active Inspector or Category Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (selectedItem != null && (
                        (selectedCategory == ElementsCategory.TEXT && selectedItem.type == MediaType.TEXT) ||
                        (selectedCategory == ElementsCategory.OVERLAYS && (selectedItem.type == MediaType.PHOTO || selectedItem.type == MediaType.VIDEO)) ||
                        (selectedCategory == ElementsCategory.EFFECTS && selectedItem.type == MediaType.EFFECT)
                    )) {
                // Item Inspector for currently selected item on timeline
                ElementInspector(
                    item = selectedItem,
                    onUpdate = { viewModel.updateMediaOverlay(it) },
                    onDelete = { viewModel.removeMediaOverlay(selectedItem.id) },
                    onDuplicate = { viewModel.duplicateSelectedClip() },
                    onClose = { viewModel.setSelectedElement(null) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Category Specific Creator
                when (selectedCategory) {
                    ElementsCategory.TEXT -> {
                        TextSection(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ElementsCategory.OVERLAYS -> {
                        OverlaysSection(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ElementsCategory.EFFECTS -> {
                        EffectsSection(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextSection(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    var newText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#00F0FF") }
    var selectedAnim by remember { mutableStateOf(MediaAnimationType.ZOOM_IN) }

    val presetColors = listOf("#00F0FF", "#FF007F", "#00FF66", "#FFE600", "#FFFFFF", "#B829FF", "#FF5722")
    val textItems = state.mediaOverlays.filter { it.type == MediaType.TEXT }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Add Text Card
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CapCutSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCardBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ДОБАВИТЬ ТЕКСТ / СУБТИТРЫ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CapCutCyan,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        placeholder = { Text("Введите надпись...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CapCutCyan,
                            unfocusedBorderColor = CapCutCardBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CapCutCyan
                        )
                    )

                    Button(
                        onClick = {
                            if (newText.isNotBlank()) {
                                viewModel.addTextOverlay(
                                    text = newText,
                                    colorHex = selectedColor,
                                    animation = selectedAnim
                                )
                                newText = ""
                            }
                        },
                        enabled = newText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CapCutCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Color palette row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Цвет:", fontSize = 10.sp, color = TextSecondary)
                    presetColors.forEach { hex ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (selectedColor == hex) 2.dp else 1.dp,
                                    if (selectedColor == hex) Color.White else CapCutCardBorderSubtle,
                                    CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        }

        // Active Text Elements List
        if (textItems.isNotEmpty()) {
            Text(
                text = "ТЕКСТОВЫЕ СЛОИ НА ТАЙМЛАЙНЕ (${textItems.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            textItems.forEach { item ->
                ElementRowItem(
                    item = item,
                    isSelected = item.id == state.selectedElementId,
                    onSelect = { viewModel.setSelectedElement(item.id) },
                    onDelete = { viewModel.removeMediaOverlay(item.id) }
                )
            }
        }
    }
}

@Composable
private fun OverlaysSection(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhotoFromUri(it, "Фото слой") }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addVideoFromUri(it, "Видео слой") }
    }

    val overlayItems = state.mediaOverlays.filter { it.type == MediaType.PHOTO || it.type == MediaType.VIDEO }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Add Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { photoPicker.launch("image/*") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = CapCutCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Добавить Фото", fontSize = 11.sp, color = CapCutCyan, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { videoPicker.launch("video/*") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CapCutBlue.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CapCutBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = CapCutBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Добавить Видео", fontSize = 11.sp, color = CapCutBlue, fontWeight = FontWeight.Bold)
            }
        }

        // Active Overlays List
        if (overlayItems.isNotEmpty()) {
            Text(
                text = "СЛОИ НА ТАЙМЛАЙНЕ (${overlayItems.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            overlayItems.forEach { item ->
                ElementRowItem(
                    item = item,
                    isSelected = item.id == state.selectedElementId,
                    onSelect = { viewModel.setSelectedElement(item.id) },
                    onDelete = { viewModel.removeMediaOverlay(item.id) }
                )
            }
        }
    }
}

@Composable
private fun EffectsSection(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val effects = VideoEffectType.values()
    val activeFx = state.mediaOverlays.filter { it.type == MediaType.EFFECT }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "КАТАЛОГ ШЕЙДЕРНЫХ ЭФФЕКТОВ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CapCutCyan,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            effects.forEach { fx ->
                Surface(
                    modifier = Modifier
                        .width(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.addEffectOverlay(fx) },
                    color = CapCutSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CapCutPink,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fx.title.split("(").first().trim(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("+ Применить", fontSize = 9.sp, color = CapCutCyan)
                    }
                }
            }
        }

        // Active FX on timeline
        if (activeFx.isNotEmpty()) {
            Text(
                text = "АКТИВНЫЕ ЭФФЕКТЫ НА ТАЙМЛАЙНЕ (${activeFx.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            activeFx.forEach { item ->
                ElementRowItem(
                    item = item,
                    isSelected = item.id == state.selectedElementId,
                    onSelect = { viewModel.setSelectedElement(item.id) },
                    onDelete = { viewModel.removeMediaOverlay(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ElementRowItem(
    item: MediaOverlayItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect),
        color = if (isSelected) CapCutCyan.copy(alpha = 0.15f) else CapCutSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CapCutCyan else CapCutCardBorderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.type) {
                        MediaType.TEXT -> Icons.Default.TextFields
                        MediaType.PHOTO -> Icons.Default.Image
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.EFFECT -> Icons.Default.AutoAwesome
                        else -> Icons.Default.Layers
                    },
                    contentDescription = null,
                    tint = if (isSelected) CapCutCyan else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CapCutCyan else TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = String.format(Locale.US, "%.1fs - %.1fs (длительность %.1fs)", item.startTimeSec, item.endTimeSec, item.durationSec),
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ElementInspector(
    item: MediaOverlayItem,
    onUpdate: (MediaOverlayItem) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Inspector Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("НАСТРОЙКА: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CapCutCyan, maxLines = 1)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Дублировать", tint = CapCutGreen, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = CapCutPink, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Text specific controls
        if (item.type == MediaType.TEXT) {
            OutlinedTextField(
                value = item.textContent,
                onValueChange = { onUpdate(item.copy(textContent = it, title = "Текст: $it")) },
                label = { Text("Текст надписи", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CapCutCyan,
                    unfocusedBorderColor = CapCutCardBorderSubtle
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Размер: ${item.fontSizeSp}sp", fontSize = 10.sp, color = TextSecondary)
                    Slider(
                        value = item.fontSizeSp.toFloat(),
                        onValueChange = { onUpdate(item.copy(fontSizeSp = it.toInt())) },
                        valueRange = 14f..72f,
                        colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                    )
                }
            }
        }

        // Position X / Y and Scale
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Позиция X: ${item.xPercent.toInt()}%", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = item.xPercent,
                    onValueChange = { onUpdate(item.copy(xPercent = it)) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Позиция Y: ${item.yPercent.toInt()}%", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = item.yPercent,
                    onValueChange = { onUpdate(item.copy(yPercent = it)) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                )
            }
        }

        // Scale and Opacity
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(String.format(Locale.US, "Масштаб: %.2fx", item.scale), fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = item.scale,
                    onValueChange = { onUpdate(item.copy(scale = it)) },
                    valueRange = 0.2f..3.0f,
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Прозрачность: ${(item.opacity * 100).toInt()}%", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = item.opacity,
                    onValueChange = { onUpdate(item.copy(opacity = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                )
            }
        }

        // Animation selection
        Text("АНИМАЦИЯ ПОЯВЛЕНИЯ:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MediaAnimationType.values().forEach { anim ->
                FilterChip(
                    selected = item.animation == anim,
                    onClick = { onUpdate(item.copy(animation = anim)) },
                    label = { Text(anim.title.split("(").first().trim(), fontSize = 9.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapCutCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CapCutCyan
                    )
                )
            }
        }
    }
}
