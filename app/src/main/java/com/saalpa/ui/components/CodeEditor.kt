package com.saalpa.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBg
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioPink
import com.saalpa.ui.theme.StudioSky
import com.saalpa.ui.theme.StudioSuccess
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary

@Composable
fun CodeEditor(
    sceneId: String,
    sceneTitle: String,
    scenes: List<HyperFrameScene>,
    html: String,
    css: String,
    js: String,
    isModified: Boolean,
    onCodeChange: (html: String, css: String, js: String) -> Unit,
    onSaveCode: () -> Unit,
    onSelectScene: (String) -> Unit,
    onBackToStudio: () -> Unit,
    previewContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: HTML, 1: CSS, 2: JS
    var isPreviewActive by remember { mutableStateOf(false) }
    var showSceneDropdown by remember { mutableStateOf(false) }
    val tabs = listOf("HTML", "CSS", "JS")
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentFileName = when (selectedTabIndex) {
        0 -> "scenes/$sceneId/index.html"
        1 -> "scenes/$sceneId/style.css"
        else -> "scenes/$sceneId/script.js"
    }

    val currentCode = when (selectedTabIndex) {
        0 -> html
        1 -> css
        else -> js
    }

    val currentSnippets = when (selectedTabIndex) {
        0 -> listOf(
            "<div class=\"headline\">Text</div>",
            "<h1 class=\"title\">Hello</h1>",
            "<p class=\"desc\">Content</p>",
            "<img src=\"assets/image.png\" class=\"media\" />"
        )
        1 -> listOf(
            "animation: fadeIn 1s ease-out;",
            "backdrop-filter: blur(10px);",
            "transform: translate(-50%, -50%);",
            "box-shadow: 0 0 40px rgba(0,0,0,0.8);"
        )
        else -> listOf(
            "gsap.to('.title', { opacity: 1, y: 0, duration: 1 });",
            "console.log('Scene active');",
            "gsap.from('.headline', { scale: 0.8, duration: 0.5 });"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
            .statusBarsPadding()
    ) {
        // Top Bar: [ ← Code ]   [ HTML | CSS | JS ]
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StudioSurface,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Back button and scene picker
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackToStudio,
                            modifier = Modifier.size(34.dp).testTag("code_editor_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = StudioAccentLight
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioSurfaceVariant)
                                    .border(1.dp, StudioBorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { showSceneDropdown = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sceneTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "▾",
                                    fontSize = 10.sp,
                                    color = StudioTextMuted
                                )
                            }

                            DropdownMenu(
                                expanded = showSceneDropdown,
                                onDismissRequest = { showSceneDropdown = false },
                                modifier = Modifier.background(StudioSurfaceElevated)
                            ) {
                                scenes.forEach { sc ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${sc.title} (${sc.id})",
                                                color = if (sc.id == sceneId) StudioAccentLight else StudioTextPrimary,
                                                fontWeight = if (sc.id == sceneId) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            onSelectScene(sc.id)
                                            showSceneDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Center/Right: [ HTML ] [ CSS ] [ JS ] Tabs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            val tabColor = when (index) {
                                0 -> StudioPink
                                1 -> StudioSky
                                else -> StudioAccentLight
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) tabColor.copy(alpha = 0.2f) else StudioSurfaceVariant)
                                    .border(1.dp, if (isSelected) tabColor else StudioBorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { selectedTabIndex = index }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("tab_$title"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) tabColor else StudioTextSecondary
                                )
                            }
                        }

                        // Copy Button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentCode))
                                Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Копировать",
                                tint = StudioTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // File path bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentFileName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        color = StudioTextSecondary
                    )
                    Text(
                        text = if (isModified) "Файл изменён (автосохранение активно)" else "Сохранено на диске",
                        fontSize = 9.5.sp,
                        color = if (isModified) StudioAccentLight else StudioSuccess
                    )
                }
            }
        }

        // Quick Snippets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            currentSnippets.forEach { snippet ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioSurfaceVariant)
                        .border(1.dp, StudioBorderSubtle, RoundedCornerShape(4.dp))
                        .clickable {
                            when (selectedTabIndex) {
                                0 -> onCodeChange(html + "\n" + snippet, css, js)
                                1 -> onCodeChange(html, css + "\n" + snippet, js)
                                2 -> onCodeChange(html, css, js + "\n" + snippet)
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = snippet,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioAccentLight
                    )
                }
            }
        }

        // Main Editor or Split Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isPreviewActive) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black)
                    ) {
                        previewContent()
                    }
                }
            } else {
                OutlinedTextField(
                    value = currentCode,
                    onValueChange = { newText ->
                        when (selectedTabIndex) {
                            0 -> onCodeChange(newText, css, js)
                            1 -> onCodeChange(html, newText, js)
                            2 -> onCodeChange(html, css, newText)
                        }
                    },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = StudioTextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = StudioSurfaceVariant,
                        unfocusedContainerColor = StudioSurfaceVariant,
                        cursorColor = StudioAccentLight
                    ),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_input")
                )
            }
        }

        // Bottom Action Bar: [ Save ]                     [ Preview ]
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StudioSurface,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Button (Immediately writes to filesystem)
                Button(
                    onClick = {
                        onSaveCode()
                        Toast.makeText(context, "Файлы сохранены в HF-projects/", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("btn_save_code")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Preview Button (Toggles live preview of the HTML/CSS/JS)
                OutlinedButton(
                    onClick = { isPreviewActive = !isPreviewActive },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isPreviewActive) StudioAccent.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isPreviewActive) StudioAccentLight else StudioTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPreviewActive) StudioAccent else StudioBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("btn_preview_toggle")
                ) {
                    Icon(
                        imageVector = if (isPreviewActive) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPreviewActive) "Код" else "Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
