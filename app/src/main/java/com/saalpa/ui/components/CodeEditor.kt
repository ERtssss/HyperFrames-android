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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBg
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioDanger
import com.saalpa.ui.theme.StudioPink
import com.saalpa.ui.theme.StudioSky
import com.saalpa.ui.theme.StudioSuccess
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary

@Composable
fun CodeEditor(
    html: String,
    css: String,
    js: String,
    isModified: Boolean,
    onCodeChange: (html: String, css: String, js: String) -> Unit,
    onReset: () -> Unit,
    onBackToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("HTML / DOM", "CSS Styles", "GSAP / JavaScript")
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentCode = when (selectedTabIndex) {
        0 -> html
        1 -> css
        else -> js
    }

    val currentSnippets = when (selectedTabIndex) {
        0 -> listOf(
            "<div class=\"scene\" id=\"scene-0\">",
            "<div class=\"layer badge\">",
            "<h1 class=\"headline\">",
            "<div class=\"avatar-frame\">",
            "<img src=\"...\" />"
        )
        1 -> listOf(
            "var(--progress)",
            "animation: pulse 2s infinite;",
            "backdrop-filter: blur(12px);",
            "box-shadow: 0 0 24px rgba(79, 70, 229, 0.4);"
        )
        else -> listOf(
            "gsap.to('.scene', { opacity: 1, duration: 0.5 })",
            "gsap.timeline({ paused: true })",
            "window.HyperFrames.onSceneChange(0, 'scene-0', 'Intro')"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        // Top Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StudioSurface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToStudio,
                        modifier = Modifier.size(36.dp).testTag("code_editor_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад в студию",
                            tint = StudioAccentLight
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "HYPERFRAMES CODE IDE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isModified) "Пользовательский код активен" else "Авто-генерация на базе композиций сцен",
                            fontSize = 9.sp,
                            color = if (isModified) StudioSuccess else StudioTextMuted
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Copy code button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentCode))
                            Toast.makeText(context, "Код скопирован в буфер", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Копировать",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset button
                    if (isModified) {
                        IconButton(
                            onClick = {
                                onReset()
                                Toast.makeText(context, "Код сброшен к композиции проекта", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Сбросить к оригиналу",
                                tint = StudioDanger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Done/Apply button
                    Button(
                        onClick = {
                            onBackToStudio()
                            Toast.makeText(context, "Код применен", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioAccent,
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Применить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Language Tabs (HTML, CSS, JS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val tabColor = when (index) {
                    0 -> StudioPink
                    1 -> StudioSky
                    else -> StudioAccentLight
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedTabIndex = index }
                        .testTag("code_tab_$title"),
                    color = if (isSelected) tabColor.copy(alpha = 0.15f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) tabColor else StudioBorderSubtle
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.Html
                                1 -> Icons.Default.Css
                                else -> Icons.Default.Javascript
                            },
                            contentDescription = null,
                            tint = if (isSelected) tabColor else StudioTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) tabColor else StudioTextMuted
                        )
                    }
                }
            }
        }

        // Quick Snippets Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            currentSnippets.forEach { snippet ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioSurfaceVariant)
                        .border(1.dp, StudioBorderSubtle, RoundedCornerShape(6.dp))
                        .clickable {
                            when (selectedTabIndex) {
                                0 -> onCodeChange(html + "\n" + snippet, css, js)
                                1 -> onCodeChange(html, css + "\n" + snippet, js)
                                2 -> onCodeChange(html, css, js + "\n" + snippet)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = snippet,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioAccentLight
                    )
                }
            }
        }

        // Full Screen Code Text Area (without video preview and without tracks!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
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
                    focusedBorderColor = StudioAccent,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = StudioSurfaceVariant,
                    unfocusedContainerColor = StudioSurfaceVariant,
                    cursorColor = StudioAccentLight
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("code_editor_input")
            )
        }
    }
}
