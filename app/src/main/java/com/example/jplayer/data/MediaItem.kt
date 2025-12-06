package com.example.jplayer.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArt: String?,
    val type: MediaType,
    val size: Long,
    val dateAdded: Long
) : Parcelable

enum class MediaType {
    AUDIO, VIDEO
}
