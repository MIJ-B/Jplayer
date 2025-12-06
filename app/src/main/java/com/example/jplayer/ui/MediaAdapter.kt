package com.example.jplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jplayer.R
import com.example.jplayer.data.MediaItem
import java.util.concurrent.TimeUnit

class MediaAdapter(
    private val onItemClick: (MediaItem) -> Unit,
    private val onMenuClick: (MediaItem, View) -> Unit
) : ListAdapter<MediaItem, MediaAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view, onItemClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaViewHolder(
        itemView: View,
        private val onItemClick: (MediaItem) -> Unit,
        private val onMenuClick: (MediaItem, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.media_title)
        private val artistView: TextView = itemView.findViewById(R.id.media_artist)
        private val durationView: TextView = itemView.findViewById(R.id.media_duration)
        private val albumArtView: ImageView = itemView.findViewById(R.id.media_artwork)
        private val menuButton: ImageView = itemView.findViewById(R.id.media_menu)

        fun bind(item: MediaItem) {
            titleView.text = item.title
            artistView.text = item.artist
            durationView.text = formatDuration(item.duration)

            Glide.with(itemView.context)
                .load(item.albumArt)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(albumArtView)

            itemView.setOnClickListener { onItemClick(item) }
            menuButton.setOnClickListener { onMenuClick(item, it) }
        }

        private fun formatDuration(duration: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
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
