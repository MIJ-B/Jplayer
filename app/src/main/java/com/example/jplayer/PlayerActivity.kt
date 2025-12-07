package com.example.jplayer

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.jplayer.data.MediaItem as JPlayerMediaItem
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var currentTimeView: TextView
    private lateinit var totalTimeView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: ImageButton
    private lateinit var albumArtView: ImageView
    
    private var isPlaying = false
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (::player.isInitialized && player.isPlaying) {
                val currentPosition = player.currentPosition
                val duration = player.duration
                
                seekBar.max = duration.toInt()
                seekBar.progress = currentPosition.toInt()
                currentTimeView.text = formatTime(currentPosition)
                
                seekBar.postDelayed(this, 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Initialize views
        titleView = findViewById(R.id.player_title)
        artistView = findViewById(R.id.player_artist)
        currentTimeView = findViewById(R.id.current_time)
        totalTimeView = findViewById(R.id.total_time)
        seekBar = findViewById(R.id.seek_bar)
        playPauseButton = findViewById(R.id.play_pause_button)
        albumArtView = findViewById(R.id.player_album_art)

        // Get media item from intent
        val mediaItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("media_item", JPlayerMediaItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("media_item")
        }

        mediaItem?.let {
            setupPlayer(it)
            displayMediaInfo(it)
        }

        setupControls()
    }

    private fun setupPlayer(mediaItem: JPlayerMediaItem) {
        player = ExoPlayer.Builder(this).build()
        
        val exoMediaItem = MediaItem.fromUri(mediaItem.path.toUri())
        player.setMediaItem(exoMediaItem)
        player.prepare()
        player.playWhenReady = true
        
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        totalTimeView.text = formatTime(player.duration)
                        seekBar.max = player.duration.toInt()
                        updateSeekBar()
                    }
                    Player.STATE_ENDED -> {
                        playPauseButton.setImageResource(R.drawable.ic_play)
                        isPlaying = false
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                playPauseButton.setImageResource(
                    if (playing) R.drawable.ic_pause else R.drawable.ic_play
                )
                if (playing) {
                    updateSeekBar()
                }
            }
        })
    }

    private fun displayMediaInfo(mediaItem: JPlayerMediaItem) {
        titleView.text = mediaItem.title
        artistView.text = mediaItem.artist
        
        if (mediaItem.albumArt != null) {
            Glide.with(this)
                .load(mediaItem.albumArt)
                .placeholder(R.drawable.ic_music_note)
                .into(albumArtView)
        }
    }

    private fun setupControls() {
        playPauseButton.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

        findViewById<ImageButton>(R.id.previous_button).setOnClickListener {
            player.seekTo(0)
        }

        findViewById<ImageButton>(R.id.next_button).setOnClickListener {
            // TODO: Implement next track
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun updateSeekBar() {
        seekBar.post(updateSeekBarRunnable)
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
        seekBar.removeCallbacks(updateSeekBarRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (::player.isInitialized) {
            player.pause()
        }
    }
}