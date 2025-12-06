package com.example.jplayer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val id: Long,
    val name: String,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val path: String,
    val thumbnailPath: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    var isFavorite: Boolean = false
) : Parcelable

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    DATE_ADDED_DESC,
    DATE_ADDED_ASC,
    DURATION_DESC,
    DURATION_ASC,
    SIZE_DESC,
    SIZE_ASC
}

enum class ViewMode {
    LIST,
    GRID
}