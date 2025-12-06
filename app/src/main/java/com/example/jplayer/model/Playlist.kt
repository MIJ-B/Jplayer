package com.example.jplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val mediaIds: List<Long>,
    val createdAt: Long,
    val thumbnailPath: String? = null
) : Parcelable
