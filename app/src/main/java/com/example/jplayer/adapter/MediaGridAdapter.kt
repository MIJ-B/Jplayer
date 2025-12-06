package com.example.jplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jplayer.R
import com.example.jplayer.databinding.ItemMediaGridBinding
import com.example.jplayer.model.MediaItem

class MediaGridAdapter(
    private val mediaList: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaGridAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(private val binding: ItemMediaGridBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mediaItem: MediaItem) {
            binding.tvTitle.text = mediaItem.name
            binding.tvDuration.text = formatDuration(mediaItem.duration)
            
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
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaGridBinding.inflate(
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
