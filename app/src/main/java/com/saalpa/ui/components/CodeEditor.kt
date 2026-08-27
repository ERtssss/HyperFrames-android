package com.saalpa.ui.components

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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.ui.theme.CyberPink
import com.saalpa.ui.theme.ElectricCyan
import com.saalpa.ui.theme.EmeraldGreen
import com.saalpa.ui.theme.NeonViolet
import com.saalpa.ui.theme.PrimaryBrand
import com.saalpa.ui.theme.PrimaryBrandContainer
import com.saalpa.ui.theme.StudioCardBorder
import com.saalpa.ui.theme.StudioCardBorderSubtle
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary

@Composable
fun CodeEditor(
    html: String,
    css: String,
    js: String,
    isModified: Boolean,
    onCodeChange: (html: String, css: String, js: String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("HTML", "CSS", "JavaScript")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = StudioSurface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Tabs + Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Code Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        val tabColor = when (index) {
                            0 -> PrimaryBrand
                            1 -> NeonViolet
                            else -> EmeraldGreen
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) tabColor.copy(alpha = 0.15f) else StudioSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) tabColor else StudioCardBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedTabIndex = index }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("code_tab_$title")
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) tabColor else TextSecondary
                            )
                        }
                    }
                }

                // Reset Action
                if (isModified) {
                    OutlinedButton(
                        onClick = onReset,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Template",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Snippet Chips Bar
            val currentSnippets = when (selectedTabIndex) {
                0 -> listOf("<div>", "<h1>", "<span>", "<canvas>", "class=\"\"")
                1 -> listOf("var(--progress)", "var(--time)", "transform: scale()", "filter: blur()", "opacity:")
                else -> listOf("window.HyperFrames", "progress", "Math.sin()", "canvas.getContext()", "draw()")
            }

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentSnippets.forEach { snippet ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceVariant)
                            .border(0.8.dp, StudioCardBorderSubtle, RoundedCornerShape(8.dp))
                            .clickable {
                                when (selectedTabIndex) {
                                    0 -> onCodeChange(html + " " + snippet, css, js)
                                    1 -> onCodeChange(html, css + "\n" + snippet, js)
                                    2 -> onCodeChange(html, css, js + "\n" + snippet)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = snippet,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBrand
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code Text Editor
            val currentCode = when (selectedTabIndex) {
                0 -> html
                1 -> css
                else -> js
            }

            OutlinedTextField(
                value = currentCode,
                onValueChange = { newText ->
                    when (selectedTabIndex) {
                        0 -> onCodeChange(newText, css, js)
                        1 -> onCodeChange(html, newText, js)
                        else -> onCodeChange(html, css, newText)
                    }
                },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBrand,
                    unfocusedBorderColor = StudioCardBorderSubtle,
                    focusedContainerColor = StudioSurfaceVariant,
                    unfocusedContainerColor = StudioSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("code_editor_input")
            )
        }
    }
}

