package com.example.model

import java.io.File

data class SavedVideo(
    val id: String,
    val title: String,
    val file: File,
    val createdAt: Long,
    val durationSec: Float,
    val width: Int,
    val height: Int,
    val fps: Int,
    val fileSizeFormatted: String
)
