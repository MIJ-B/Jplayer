package com.example.jplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jplayer.R
import com.example.jplayer.databinding.ItemMediaListBinding
import com.example.jplayer.model.MediaItem

class MediaAdapter(
    private val mediaList: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(private val binding: ItemMediaListBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mediaItem: MediaItem) {
            binding.tvTitle.text = mediaItem.name
            binding.tvDuration.text = formatDuration(mediaItem.duration)
            binding.tvSize.text = formatSize(mediaItem.size)
            binding.tvResolution.text = "${mediaItem.width}x${mediaItem.height}"
            
            // Load thumbnail
            Glide.with(binding.root.context)
                .load(mediaItem.uri)
                .placeholder(R.drawable.ic_video_placeholder)
                .error(R.drawable.ic_video_placeholder)
                .centerCrop()
                .into(binding.ivThumbnail)
            
            binding.root.setOnClickListener { 
                onItemClick(mediaItem) 
            }
        }

        private fun formatDuration(duration: Long): String {
            val totalSeconds = duration / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

        private fun formatSize(size: Long): String {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            
            return when {
                gb >= 1 -> String.format("%.2f GB", gb)
                mb >= 1 -> String.format("%.2f MB", mb)
                else -> String.format("%.2f KB", kb)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaListBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(mediaList[position])
    }

    override fun getItemCount(): Int = mediaList.size
}