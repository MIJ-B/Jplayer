package com.example.jplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jplayer.R
import com.example.jplayer.data.Playlist
import java.text.SimpleDateFormat
import java.util.*

class PlaylistAdapter(
    private val onItemClick: (Playlist) -> Unit,
    private val onMenuClick: (Playlist, View) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view, onItemClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(
        itemView: View,
        private val onItemClick: (Playlist) -> Unit,
        private val onMenuClick: (Playlist, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameView: TextView = itemView.findViewById(R.id.playlist_name)
        private val countView: TextView = itemView.findViewById(R.id.playlist_count)
        private val dateView: TextView = itemView.findViewById(R.id.playlist_date)
        private val menuButton: ImageView = itemView.findViewById(R.id.playlist_menu)

        fun bind(playlist: Playlist) {
            nameView.text = playlist.name
            countView.text = "${playlist.itemCount} tracks"
            dateView.text = formatDate(playlist.dateCreated)

            itemView.setOnClickListener { onItemClick(playlist) }
            menuButton.setOnClickListener { onMenuClick(playlist, it) }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp * 1000))
        }
    }

    private class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}