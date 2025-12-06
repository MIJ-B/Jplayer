package com.example.jplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jplayer.databinding.ItemMediaBinding
import com.example.jplayer.model.MediaItem

class MediaAdapter(
    private val mediaList: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(private val binding: ItemMediaBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mediaItem: MediaItem) {
            binding.textTitle.text = mediaItem.name
            binding.textDuration.text = formatDuration(mediaItem.duration)
            
            binding.root.setOnClickListener { 
                onItemClick(mediaItem) 
            }
        }

        private fun formatDuration(duration: Long): String {
            if (duration <= 0) return "0:00"
            
            val totalSeconds = duration / 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            val hours = totalSeconds / 3600
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
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
