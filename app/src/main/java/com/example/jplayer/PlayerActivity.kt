package com.example.jplayer

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.jplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide system UI for immersive experience
        hideSystemUI()

        val mediaUri = intent.getStringExtra("MEDIA_URI")
        val mediaTitle = intent.getStringExtra("MEDIA_TITLE")

        supportActionBar?.apply {
            title = mediaTitle
            setDisplayHomeAsUpEnabled(true)
        }

        if (mediaUri != null) {
            initializePlayer(mediaUri)
        } else {
            finish()
        }
    }

    private fun hideSystemUI() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    private fun initializePlayer(mediaUri: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            val mediaItem = MediaItem.fromUri(mediaUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = playWhenReady
            exoPlayer.seekTo(currentPosition)
            exoPlayer.prepare()

            // Add listener for playback state
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            // Video finished
                            finish()
                        }
                        Player.STATE_READY -> {
                            // Player is ready
                        }
                        Player.STATE_BUFFERING -> {
                            // Buffering
                        }
                        Player.STATE_IDLE -> {
                            // Idle
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    super.onPlayerError(error)
                    // Handle error
                    finish()
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        if (player == null) {
            intent.getStringExtra("MEDIA_URI")?.let { 
                initializePlayer(it) 
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        player?.playWhenReady = playWhenReady
    }

    override fun onPause() {
        super.onPause()
        player?.let {
            playWhenReady = it.playWhenReady
            currentPosition = it.currentPosition
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let {
            playWhenReady = it.playWhenReady
            currentPosition = it.currentPosition
            it.release()
        }
        player = null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
