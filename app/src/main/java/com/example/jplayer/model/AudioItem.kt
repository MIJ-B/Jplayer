package com.example.jplayer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioItem(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val path: String,
    val albumArtUri: Uri?
) : Parcelable
