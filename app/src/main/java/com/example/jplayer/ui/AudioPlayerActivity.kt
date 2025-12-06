package com.example.jplayer.ui

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.jplayer.databinding.ActivityAudioPlayerBinding
import com.example.jplayer.model.AudioItem

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    private var player: ExoPlayer? = null
    
    private var currentAudio: AudioItem? = null
    private var playlist: List<AudioItem> = emptyList()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentAudio = intent.getParcelableExtra("AUDIO_ITEM")
        playlist = intent.getParcelableArrayListExtra<AudioItem>("PLAYLIST") ?: emptyList()
        currentIndex = playlist.indexOfFirst { it.id == currentAudio?.id }
        
        if (currentIndex == -1) currentIndex = 0

        setupPlayer()
        setupControls()
        updateUI()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            currentAudio?.let { audio ->
                val mediaItem = MediaItem.fromUri(audio.uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }

            exoPlayer.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseButton(isPlaying)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        playNext()
                    }
                }
            })
            
            startProgressUpdate()
        }
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }
        
        binding.btnPrevious.setOnClickListener {
            playPrevious()
        }
        
        binding.btnNext.setOnClickListener {
            playNext()
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                player?.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    player?.seekTo(it.progress.toLong())
                    player?.play()
                }
            }
        })
    }

    private fun updateUI() {
        currentAudio?.let { audio ->
            binding.tvTitle.text = audio.title
            binding.tvArtist.text = audio.artist ?: "Unknown Artist"
            binding.tvAlbum.text = audio.album ?: "Unknown Album"
            
            // Load album art
            audio.albumArtUri?.let { uri ->
                // Use Glide to load album art
            }
        }
        
        updateNavigationButtons()
    }

    private fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    private fun playPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            loadAudio(playlist[currentIndex])
        }
    }

    private fun playNext() {
        if (currentIndex < playlist.size - 1) {
            currentIndex++
            loadAudio(playlist[currentIndex])
        }
    }

    private fun loadAudio(audio: AudioItem) {
        currentAudio = audio
        player?.let {
            val mediaItem = MediaItem.fromUri(audio.uri)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
        updateUI()
    }

    private fun startProgressUpdate() {
        binding.root.postDelayed(object : Runnable {
            override fun run() {
                player?.let {
                    val currentPos = it.currentPosition
                    val duration = it.duration
                    
                    binding.tvCurrentTime.text = formatTime(currentPos)
                    binding.tvDuration.text = formatTime(duration)
                    binding.seekBar.max = duration.toInt()
                    binding.seekBar.progress = currentPos.toInt()
                }
                binding.root.postDelayed(this, 500)
            }
        }, 500)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        // Update icon
    }

    private fun updateNavigationButtons() {
        binding.btnPrevious.isEnabled = currentIndex > 0
        binding.btnNext.isEnabled = currentIndex < playlist.size - 1
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
