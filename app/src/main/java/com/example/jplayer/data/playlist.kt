package com.example.jplayer.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val itemCount: Int,
    val dateCreated: Long
) : Parcelable