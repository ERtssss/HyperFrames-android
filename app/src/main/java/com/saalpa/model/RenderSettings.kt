package com.saalpa.model

enum class AspectRatioType(val title: String, val widthRatio: Int, val heightRatio: Int, val description: String) {
    PORTRAIT_9_16("9:16 Reel / Story", 9, 16, "Vertical (TikTok, Reels, Shorts)"),
    SQUARE_1_1("1:1 Square", 1, 1, "Instagram Feed & Posts"),
    LANDSCAPE_16_9("16:9 Widescreen", 16, 9, "YouTube & Landscape Video"),
    PORTRAIT_4_5("4:5 Portrait", 4, 5, "Social Feeds");

    val ratio: Float get() = widthRatio.toFloat() / heightRatio.toFloat()
    val label: String get() = "$widthRatio:$heightRatio"
    val category: String get() = description
}

enum class RenderResolution(val label: String, val width: Int, val height: Int, val note: String) {
    SD_540P("540p (Fast)", 540, 960, "Ultra fast test renders"),
    HD_720P("720p HD", 720, 1280, "Balanced speed & quality"),
    FHD_1080P("1080p Full HD", 1080, 1920, "High quality final export")
}
