package com.saalpa.model

enum class MediaCategoryType {
    IMAGES,
    VIDEOS,
    AUDIO
}

data class MediaAssetItem(
    val id: String,
    val name: String,
    val uri: String,
    val category: MediaCategoryType
)
