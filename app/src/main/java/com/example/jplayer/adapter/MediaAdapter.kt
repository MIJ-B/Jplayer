package com.example.jplayer.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jplayer.R
import com.example.jplayer.data.MediaItem
import com.example.jplayer.data.MediaType
import java.util.concurrent.TimeUnit

class MediaAdapter(
    private val onItemClick: (MediaItem) -> Unit,
    private val onMenuClick: ((MediaItem, View) -> Unit)? = null
) : ListAdapter<MediaItem, MediaAdapter.MediaViewHolder>(MediaDiffCallback()) {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById(R.id.media_thumbnail)
        private val title: TextView = itemView.findViewById(R.id.media_title)
        private val subtitle: TextView = itemView.findViewById(R.id.media_subtitle)
        private val duration: TextView = itemView.findViewById(R.id.media_duration)
        private val menuButton: ImageButton? = itemView.findViewById(R.id.media_menu)

        fun bind(mediaItem: MediaItem) {
            title.text = mediaItem.title
            duration.text = formatDuration(mediaItem.duration)

            when (mediaItem.type) {
                MediaType.AUDIO -> {
                    subtitle.text = "${mediaItem.artist} • ${mediaItem.album}"
                    
                    // Load album art
                    if (mediaItem.albumArt != null) {
                        Glide.with(itemView.context)
                            .load(mediaItem.albumArt)
                            .placeholder(R.drawable.ic_music_note)
                            .error(R.drawable.ic_music_note)
                            .centerCrop()
                            .into(thumbnail)
                    } else {
                        thumbnail.setImageResource(R.drawable.ic_music_note)
                    }
                }
                MediaType.VIDEO -> {
                    subtitle.text = formatFileSize(mediaItem.size)
                    
                    // Load video thumbnail
                    Glide.with(itemView.context)
                        .load(Uri.parse(mediaItem.path))
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.ic_video_placeholder)
                        .error(R.drawable.ic_video_placeholder)
                        .centerCrop()
                        .into(thumbnail)
                }
            }

            itemView.setOnClickListener {
                onItemClick(mediaItem)
            }

            menuButton?.setOnClickListener {
                onMenuClick?.invoke(mediaItem, it)
            }
        }

        private fun formatDuration(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

        private fun formatFileSize(size: Long): String {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0

            return when {
                gb >= 1 -> String.format("%.1f GB", gb)
                mb >= 1 -> String.format("%.1f MB", mb)
                else -> String.format("%.1f KB", kb)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class MediaDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}