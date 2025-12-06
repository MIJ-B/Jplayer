package com.example.jplayer.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val name: String,
    val uri: Uri,
    val duration: Long
)
