package com.example.jplayer

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.jplayer.data.MediaItem as JPlayerMediaItem
import com.example.jplayer.data.MediaType
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    
    // Common views
    private lateinit var titleView: TextView
    private lateinit var currentTimeView: TextView
    private lateinit var totalTimeView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    
    // Audio-only views
    private lateinit var artistView: TextView
    private lateinit var albumArtView: ImageView
    private lateinit var audioContainer: View
    
    // Video-only views
    private lateinit var playerView: PlayerView
    private lateinit var videoContainer: View
    
    private var currentMediaItem: JPlayerMediaItem? = null
    private var isPlaying = false
    
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (::player.isInitialized && player.isPlaying) {
                val currentPosition = player.currentPosition
                val duration = player.duration
                
                if (duration > 0) {
                    seekBar.max = duration.toInt()
                    seekBar.progress = currentPosition.toInt()
                    currentTimeView.text = formatTime(currentPosition)
                }
                
                seekBar.postDelayed(this, 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initializeViews()
        
        val mediaItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("media_item", JPlayerMediaItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("media_item")
        }

        mediaItem?.let {
            currentMediaItem = it
            setupUI(it)
            setupPlayer(it)
            setupControls()
        }
    }

    private fun initializeViews() {
        // Common views
        titleView = findViewById(R.id.player_title)
        currentTimeView = findViewById(R.id.current_time)
        totalTimeView = findViewById(R.id.total_time)
        seekBar = findViewById(R.id.seek_bar)
        playPauseButton = findViewById(R.id.play_pause_button)
        previousButton = findViewById(R.id.previous_button)
        nextButton = findViewById(R.id.next_button)
        backButton = findViewById(R.id.back_button)
        
        // Audio views
        artistView = findViewById(R.id.player_artist)
        albumArtView = findViewById(R.id.player_album_art)
        audioContainer = findViewById(R.id.audio_container)
        
        // Video views
        playerView = findViewById(R.id.player_view)
        videoContainer = findViewById(R.id.video_container)
    }

    private fun setupUI(mediaItem: JPlayerMediaItem) {
        titleView.text = mediaItem.title
        
        when (mediaItem.type) {
            MediaType.AUDIO -> {
                // Show audio UI
                audioContainer.visibility = View.VISIBLE
                videoContainer.visibility = View.GONE
                
                artistView.text = mediaItem.artist
                
                if (mediaItem.albumArt != null) {
                    Glide.with(this)
                        .load(mediaItem.albumArt)
                        .placeholder(R.drawable.ic_music_note)
                        .error(R.drawable.ic_music_note)
                        .into(albumArtView)
                } else {
                    albumArtView.setImageDrawable(
                        AppCompatResources.getDrawable(this, R.drawable.ic_music_note)
                    )
                }
            }
            MediaType.VIDEO -> {
                // Show video UI
                audioContainer.visibility = View.GONE
                videoContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPlayer(mediaItem: JPlayerMediaItem) {
        player = ExoPlayer.Builder(this).build()
        
        // Attach player to views
        if (mediaItem.type == MediaType.VIDEO) {
            playerView.player = player
            playerView.useController = false // Use custom controls
        }
        
        val exoMediaItem = MediaItem.fromUri(mediaItem.path.toUri())
        player.setMediaItem(exoMediaItem)
        player.prepare()
        player.playWhenReady = true
        
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = player.duration
                        if (duration > 0) {
                            totalTimeView.text = formatTime(duration)
                            seekBar.max = duration.toInt()
                            updateSeekBar()
                        }
                    }
                    Player.STATE_ENDED -> {
                        playPauseButton.setImageDrawable(
                            AppCompatResources.getDrawable(this@PlayerActivity, R.drawable.ic_play)
                        )
                        isPlaying = false
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                val iconRes = if (playing) R.drawable.ic_pause else R.drawable.ic_play
                playPauseButton.setImageDrawable(
                    AppCompatResources.getDrawable(this@PlayerActivity, iconRes)
                )
                if (playing) {
                    updateSeekBar()
                }
            }
        })
    }

    private fun setupControls() {
        playPauseButton.setOnClickListener {
            if (::player.isInitialized) {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }

        previousButton.setOnClickListener {
            if (::player.isInitialized) {
                player.seekTo(0)
            }
        }

        nextButton.setOnClickListener {
            // TODO: Implement playlist navigation
        }

        backButton.setOnClickListener {
            finish()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::player.isInitialized) {
                    player.seekTo(progress.toLong())
                    currentTimeView.text = formatTime(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (::player.isInitialized && player.isPlaying) {
                    player.pause()
                }
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (::player.isInitialized) {
                    player.play()
                }
            }
        })
    }

    private fun updateSeekBar() {
        seekBar.removeCallbacks(updateSeekBarRunnable)
        seekBar.post(updateSeekBarRunnable)
    }

    private fun formatTime(millis: Long): String {
        if (millis < 0) return "00:00"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        seekBar.removeCallbacks(updateSeekBarRunnable)
        if (::player.isInitialized) {
            player.release()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::player.isInitialized) {
            player.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::player.isInitialized && isPlaying) {
            player.play()
        }
    }
}
