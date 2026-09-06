package com.saalpa.ui.theme

import androidx.compose.ui.graphics.Color

// HyperFrames Professional Studio Dark Palette (HeyGen / Modern Creative Studio)
val StudioBg = Color(0xFF0C0E14)                // Dark neutral background
val StudioSurface = Color(0xFF131722)           // Matte surface for panels & inspector
val StudioSurfaceVariant = Color(0xFF1B202E)    // Surface for cards, controls, inputs
val StudioSurfaceElevated = Color(0xFF232A3B)   // Floating toolbars, active scenes
val StudioBorder = Color(0xFF283042)            // Crisp, subtle 1px borders
val StudioBorderSubtle = Color(0xFF38435C)      // Divider lines

// Signature Creative Studio Accents (HeyGen Indigo / Violet)
val StudioAccent = Color(0xFF6366F1)            // Indigo Primary Brand Action
val StudioAccentLight = Color(0xFF818CF8)       // Highlight / Hover
val StudioAccentContainer = Color(0xFF312E81)  // Dark Indigo Container
val OnStudioAccent = Color(0xFFFFFFFF)

// Secondary Functional Accents
val StudioSuccess = Color(0xFF10B981)           // Green (Rendering done, audio ready)
val StudioWarning = Color(0xFFF59E0B)           // Amber (Script timing, keyframe)
val StudioDanger = Color(0xFFEF4444)            // Rose/Red (Delete, Nerf, Alert)
val StudioSky = Color(0xFF0EA5E9)               // Sky blue (Media assets, video clips)
val StudioPurple = Color(0xFFA855F7)            // Purple (Avatar / Voice)

// High-contrast Dark UI Typography
val StudioTextPrimary = Color(0xFFF8FAFC)
val StudioTextSecondary = Color(0xFF94A3B8)
val StudioTextMuted = Color(0xFF64748B)

// Canvas Viewport Background
val ViewportDarkBg = Color(0xFF07080B)
val ViewportBorder = Color(0xFF1F2433)

// Studio Traffic Light Dots
val WindowDotRed = Color(0xFFFF5F56)
val WindowDotYellow = Color(0xFFFFBD2E)
val WindowDotGreen = Color(0xFF27C93F)

// Backwards compatibility mappings for older components during migration
val StudioDarkBg = StudioBg
val StudioCardBorder = StudioBorder
val StudioCardBorderSubtle = StudioBorderSubtle
val ElectricCyan = StudioAccentLight
val PrimaryBrand = StudioAccent
val PrimaryBrandContainer = StudioAccentContainer
val OnPrimaryBrand = OnStudioAccent
val TextPrimary = StudioTextPrimary
val TextSecondary = StudioTextSecondary
val TextMuted = StudioTextMuted

// CapCut legacy aliases for zero compilation breaks during transition
val CapCutBg = StudioBg
val CapCutSurface = StudioSurface
val CapCutSurfaceVariant = StudioSurfaceVariant
val CapCutTrackBg = StudioSurfaceVariant
val CapCutTrackHeader = StudioSurface
val CapCutCardBorder = StudioBorder
val CapCutCardBorderSubtle = StudioBorderSubtle
val CapCutCyan = StudioAccentLight
val CapCutCyanDim = StudioAccent
val CapCutCyanContainer = StudioAccentContainer
val OnCapCutCyan = OnStudioAccent
val CapCutGreen = StudioSuccess
val CapCutYellow = StudioWarning
val CapCutPink = StudioDanger
val CapCutBlue = StudioSky
val CapCutOrange = StudioWarning
val CyberPink = Color(0xFFFF2A85)
val EmeraldGreen = StudioSuccess
val NeonViolet = StudioPurple
val AmberGlow = StudioWarning
val StudioPink = Color(0xFFEC4899)

// Material 3 Color Scheme
val PrimaryDark = StudioAccent
val OnPrimaryDark = OnStudioAccent
val PrimaryContainerDark = StudioAccentContainer
val OnPrimaryContainerDark = Color(0xFFE0E7FF)

val SecondaryDark = Color(0xFF94A3B8)
val OnSecondaryDark = Color(0xFF0F172A)
val SecondaryContainerDark = StudioSurfaceVariant
val OnSecondaryContainerDark = Color(0xFFF1F5F9)

val TertiaryDark = StudioPurple
val OnTertiaryDark = Color(0xFFFFFFFF)
val TertiaryContainerDark = Color(0xFF581C87)
val OnTertiaryContainerDark = Color(0xFFF3E8FF)

val BackgroundDark = StudioBg
val OnBackgroundDark = StudioTextPrimary
val SurfaceDark = StudioSurface
val OnSurfaceDark = StudioTextPrimary
val SurfaceVariantDark = StudioSurfaceVariant
val OnSurfaceVariantDark = StudioTextSecondary
val OutlineDark = StudioBorder
val OutlineVariantDark = StudioBorderSubtle
