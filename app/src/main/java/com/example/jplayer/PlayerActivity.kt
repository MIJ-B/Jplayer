package com.example.jplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.jplayer.data.MediaItem as JPlayerMediaItem
import com.example.jplayer.data.MediaType
import java.util.concurrent.TimeUnit

@UnstableApi
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
        } ?: run {
            Toast.makeText(this, "No media to play", Toast.LENGTH_SHORT).show()
            finish()
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
        try {
            val uri = mediaItem.path.toUri()
            android.util.Log.d("PlayerActivity", "Media URI: $uri")
            
            // IMPORTANT: Grant URI permission ho an'ny ExoPlayer
            if (uri.scheme == "content") {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    android.util.Log.w("PlayerActivity", "Cannot take persistable permission: ${e.message}")
                }
                
                // Grant temporary permission
                grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            
            // Create DataSource factory
            val dataSourceFactory = DefaultDataSource.Factory(this)
            
            // Create MediaSource factory
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            
            // Build ExoPlayer with proper data source
            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
            
            // Attach player to views
            if (mediaItem.type == MediaType.VIDEO) {
                playerView.player = player
                playerView.useController = false
            }
            
            // Create MediaItem from URI
            val exoMediaItem = MediaItem.Builder()
                .setUri(uri)
                .build()
            
            player.setMediaItem(exoMediaItem)
            player.prepare()
            player.playWhenReady = true
            
            // Add listeners
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("PlayerActivity", "Playback error: ${error.message}", error)
                    android.util.Log.e("PlayerActivity", "Error code: ${error.errorCode}")
                    android.util.Log.e("PlayerActivity", "Error code name: ${error.errorCodeName}")
                    
                    val errorMsg = when (error.errorCode) {
                        androidx.media3.common.PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> {
                            "No permission to access this file"
                        }
                        androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                            "File not found"
                        }
                        androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                            "Unsupported file format"
                        }
                        else -> {
                            "Cannot play this file: ${error.errorCodeName}"
                        }
                    }
                    
                    Toast.makeText(this@PlayerActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    android.util.Log.d("PlayerActivity", "Playback state: $playbackState")
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                            android.util.Log.d("PlayerActivity", "STATE_IDLE")
                        }
                        Player.STATE_BUFFERING -> {
                            android.util.Log.d("PlayerActivity", "STATE_BUFFERING")
                        }
                        Player.STATE_READY -> {
                            android.util.Log.d("PlayerActivity", "STATE_READY")
                            val duration = player.duration
                            android.util.Log.d("PlayerActivity", "Duration: $duration ms")
                            
                            if (duration > 0 && duration != androidx.media3.common.C.TIME_UNSET) {
                                totalTimeView.text = formatTime(duration)
                                seekBar.max = duration.toInt()
                                updateSeekBar()
                            } else {
                                android.util.Log.w("PlayerActivity", "Invalid duration")
                                totalTimeView.text = "--:--"
                            }
                        }
                        Player.STATE_ENDED -> {
                            android.util.Log.d("PlayerActivity", "STATE_ENDED")
                            playPauseButton.setImageDrawable(
                                AppCompatResources.getDrawable(this@PlayerActivity, R.drawable.ic_play)
                            )
                            isPlaying = false
                        }
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    android.util.Log.d("PlayerActivity", "Is playing: $playing")
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
            
        } catch (e: Exception) {
            android.util.Log.e("PlayerActivity", "Error setting up player", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
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
        if (millis < 0 || millis == androidx.media3.common.C.TIME_UNSET) return "00:00"
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