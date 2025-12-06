package com.example.jplayer.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.jplayer.R
import com.example.jplayer.databinding.ActivityPlayerBinding
import com.example.jplayer.databinding.DialogSpeedControlBinding
import com.example.jplayer.databinding.DialogAbRepeatBinding
import com.example.jplayer.model.MediaItem as AppMediaItem
import com.example.jplayer.utils.PreferencesManager
import com.google.android.material.slider.Slider
import kotlin.math.abs

@UnstableApi
class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var prefsManager: PreferencesManager
    private var player: ExoPlayer? = null
    
    private var currentMediaItem: AppMediaItem? = null
    private var playlist: List<AppMediaItem> = emptyList()
    private var currentIndex = 0
    
    private var playWhenReady = true
    private var currentPosition = 0L
    
    // A-B Repeat
    private var pointA: Long = -1
    private var pointB: Long = -1
    private var isABRepeatEnabled = false
    
    // Playback speed
    private var playbackSpeed = 1.0f
    
    // Gestures
    private lateinit var gestureDetector: GestureDetectorCompat
    private var brightness = 0f
    private var volume = 0
    
    // Lock orientation
    private var isOrientationLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)
        
        hideSystemUI()
        
        currentMediaItem = intent.getParcelableExtra("MEDIA_ITEM")
        playlist = intent.getParcelableArrayListExtra<AppMediaItem>("PLAYLIST") ?: emptyList()
        currentIndex = playlist.indexOfFirst { it.id == currentMediaItem?.id }
        
        if (currentIndex == -1) currentIndex = 0

        setupPlayer()
        setupControls()
        setupGestures()
        loadSettings()
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

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            currentMediaItem?.let { media ->
                val mediaItem = MediaItem.fromUri(media.uri)
                exoPlayer.setMediaItem(mediaItem)
            }
            
            exoPlayer.playWhenReady = playWhenReady
            exoPlayer.seekTo(currentPosition)
            exoPlayer.prepare()

            // Set playback speed
            playbackSpeed = prefsManager.playbackSpeed
            exoPlayer.setPlaybackSpeed(playbackSpeed)
            
            // Set repeat mode
            exoPlayer.repeatMode = prefsManager.repeatMode

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            handleVideoEnded()
                        }
                        Player.STATE_READY -> {
                            updateVideoInfo()
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseButton(isPlaying)
                }
            })
            
            // A-B Repeat checker
            startABRepeatCheck()
        }
    }

    private fun setupControls() {
        // Title
        binding.tvTitle.text = currentMediaItem?.name ?: "Unknown"
        
        // Play/Pause
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }
        
        // Previous
        binding.btnPrevious.setOnClickListener {
            playPrevious()
        }
        
        // Next
        binding.btnNext.setOnClickListener {
            playNext()
        }
        
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Speed control
        binding.btnSpeed.setOnClickListener {
            showSpeedDialog()
        }
        
        // A-B Repeat
        binding.btnABRepeat.setOnClickListener {
            handleABRepeat()
        }
        
        // Repeat mode
        binding.btnRepeat.setOnClickListener {
            cycleRepeatMode()
        }
        
        // Lock orientation
        binding.btnLock.setOnClickListener {
            toggleOrientationLock()
        }
        
        // Rotate
        binding.btnRotate.setOnClickListener {
            rotateScreen()
        }
        
        // Settings/More
        binding.btnMore.setOnClickListener {
            showMoreOptions()
        }
        
        // SeekBar
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
        
        updateRepeatModeIcon()
        updateNavigationButtons()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val screenWidth = binding.playerView.width
                if (e.x < screenWidth / 3) {
                    // Left side - rewind 10s
                    player?.seekTo(maxOf(0, (player?.currentPosition ?: 0) - 10000))
                    showFeedback("⏪ -10s")
                } else if (e.x > screenWidth * 2 / 3) {
                    // Right side - forward 10s
                    player?.seekTo(minOf(
                        player?.duration ?: 0,
                        (player?.currentPosition ?: 0) + 10000
                    ))
                    showFeedback("⏩ +10s")
                } else {
                    // Center - play/pause
                    togglePlayPause()
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleControlsVisibility()
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1 == null) return false
                
                val deltaY = e1.y - e2.y
                val screenWidth = binding.playerView.width
                
                if (e1.x < screenWidth / 2) {
                    // Left side - brightness
                    adjustBrightness(deltaY)
                } else {
                    // Right side - volume
                    adjustVolume(deltaY)
                }
                return true
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
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
            loadMedia(playlist[currentIndex])
        }
    }

    private fun playNext() {
        if (currentIndex < playlist.size - 1) {
            currentIndex++
            loadMedia(playlist[currentIndex])
        }
    }

    private fun loadMedia(media: AppMediaItem) {
        currentMediaItem = media
        player?.let {
            val mediaItem = MediaItem.fromUri(media.uri)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
        binding.tvTitle.text = media.name
        resetABRepeat()
        updateNavigationButtons()
    }

    private fun handleVideoEnded() {
        if (!isABRepeatEnabled) {
            when (player?.repeatMode) {
                Player.REPEAT_MODE_OFF -> {
                    if (currentIndex < playlist.size - 1) {
                        playNext()
                    } else {
                        finish()
                    }
                }
                Player.REPEAT_MODE_ONE -> {
                    player?.seekTo(0)
                    player?.play()
                }
                Player.REPEAT_MODE_ALL -> {
                    if (currentIndex < playlist.size - 1) {
                        playNext()
                    } else {
                        currentIndex = 0
                        loadMedia(playlist[0])
                    }
                }
            }
        }
    }

    private fun showSpeedDialog() {
        val dialogBinding = DialogSpeedControlBinding.inflate(layoutInflater)
        
        dialogBinding.sliderSpeed.value = playbackSpeed
        dialogBinding.tvSpeedValue.text = String.format("%.2fx", playbackSpeed)
        
        dialogBinding.sliderSpeed.addOnChangeListener { _, value, _ ->
            dialogBinding.tvSpeedValue.text = String.format("%.2fx", value)
        }
        
        // Preset buttons
        dialogBinding.btn025x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 0.25f
        }
        dialogBinding.btn05x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 0.5f
        }
        dialogBinding.btn075x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 0.75f
        }
        dialogBinding.btn1x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 1.0f
        }
        dialogBinding.btn125x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 1.25f
        }
        dialogBinding.btn15x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 1.5f
        }
        dialogBinding.btn2x.setOnClickListener {
            dialogBinding.sliderSpeed.value = 2.0f
        }

        AlertDialog.Builder(this)
            .setTitle("Playback Speed")
            .setView(dialogBinding.root)
            .setPositiveButton("Apply") { _, _ ->
                playbackSpeed = dialogBinding.sliderSpeed.value
                player?.setPlaybackSpeed(playbackSpeed)
                prefsManager.playbackSpeed = playbackSpeed
                binding.tvSpeed.text = String.format("%.2fx", playbackSpeed)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleABRepeat() {
        val currentPos = player?.currentPosition ?: 0
        
        when {
            pointA == -1L -> {
                // Set point A
                pointA = currentPos
                binding.btnABRepeat.text = "Set B"
                showFeedback("Point A set at ${formatTime(pointA)}")
            }
            pointB == -1L -> {
                // Set point B
                if (currentPos > pointA) {
                    pointB = currentPos
                    isABRepeatEnabled = true
                    binding.btnABRepeat.text = "Clear"
                    binding.btnABRepeat.setBackgroundResource(R.drawable.bg_button_active)
                    showFeedback("A-B Repeat: ${formatTime(pointA)} - ${formatTime(pointB)}")
                } else {
                    showFeedback("Point B must be after Point A")
                }
            }
            else -> {
                // Clear A-B
                resetABRepeat()
            }
        }
    }

    private fun resetABRepeat() {
        pointA = -1
        pointB = -1
        isABRepeatEnabled = false
        binding.btnABRepeat.text = "A-B"
        binding.btnABRepeat.setBackgroundResource(R.drawable.bg_button_normal)
    }

    private fun startABRepeatCheck() {
        binding.playerView.postDelayed(object : Runnable {
            override fun run() {
                if (isABRepeatEnabled && pointB != -1L) {
                    val currentPos = player?.currentPosition ?: 0
                    if (currentPos >= pointB) {
                        player?.seekTo(pointA)
                    }
                }
                binding.playerView.postDelayed(this, 100)
            }
        }, 100)
    }

    private fun cycleRepeatMode() {
        player?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
            prefsManager.repeatMode = it.repeatMode
            updateRepeatModeIcon()
        }
    }

    private fun updateRepeatModeIcon() {
        val iconRes = when (player?.repeatMode) {
            Player.REPEAT_MODE_OFF -> R.drawable.ic_repeat_off
            Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
            Player.REPEAT_MODE_ALL -> R.drawable.ic_repeat_all
            else -> R.drawable.ic_repeat_off
        }
        binding.btnRepeat.setImageResource(iconRes)
    }

    private fun toggleOrientationLock() {
        isOrientationLocked = !isOrientationLocked
        prefsManager.lockOrientation = isOrientationLocked
        
        if (isOrientationLocked) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            binding.btnLock.setImageResource(R.drawable.ic_lock)
            showFeedback("Orientation locked")
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            binding.btnLock.setImageResource(R.drawable.ic_lock_open)
            showFeedback("Orientation unlocked")
        }
    }

    private fun rotateScreen() {
        requestedOrientation = when (requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else -> 
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun adjustBrightness(delta: Float) {
        val window = window
        val layoutParams = window.attributes
        brightness = layoutParams.screenBrightness + (delta / 1000)
        brightness = brightness.coerceIn(0f, 1f)
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
        
        showFeedback("🔆 Brightness: ${(brightness * 100).toInt()}%")
    }

    private fun adjustVolume(delta: Float) {
        // Volume control implementation
        showFeedback("🔊 Volume")
    }

    private fun toggleControlsVisibility() {
        if (binding.controlsContainer.visibility == View.VISIBLE) {
            binding.controlsContainer.visibility = View.GONE
            binding.topControls.visibility = View.GONE
        } else {
            binding.controlsContainer.visibility = View.VISIBLE
            binding.topControls.visibility = View.VISIBLE
            hideControlsDelayed()
        }
    }

    private fun hideControlsDelayed() {
        binding.controlsContainer.postDelayed({
            if (player?.isPlaying == true) {
                binding.controlsContainer.visibility = View.GONE
                binding.topControls.visibility = View.GONE
            }
        }, 5000)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun updateNavigationButtons() {
        binding.btnPrevious.isEnabled = currentIndex > 0
        binding.btnNext.isEnabled = currentIndex < playlist.size - 1
    }

    private fun updateVideoInfo() {
        player?.let {
            val duration = it.duration
            binding.tvDuration.text = formatTime(duration)
            binding.seekBar.max = duration.toInt()
            
            // Update current time
            binding.playerView.postDelayed(object : Runnable {
                override fun run() {
                    val currentPos = it.currentPosition
                    binding.tvCurrentTime.text = formatTime(currentPos)
                    binding.seekBar.progress = currentPos.toInt()
                    binding.playerView.postDelayed(this, 500)
                }
            }, 500)
        }
    }

    private fun showMoreOptions() {
        // Show additional options (subtitle, audio track, etc.)
    }

    private fun showFeedback(message: String) {
        binding.tvFeedback.text = message
        binding.tvFeedback.visibility = View.VISIBLE
        binding.tvFeedback.postDelayed({
            binding.tvFeedback.visibility = View.GONE
        }, 1500)
    }

    private fun loadSettings() {
        playbackSpeed = prefsManager.playbackSpeed
        isOrientationLocked = prefsManager.lockOrientation
        
        if (isOrientationLocked) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            binding.btnLock.setImageResource(R.drawable.ic_lock)
        }
        
        binding.tvSpeed.text = String.format("%.2fx", playbackSpeed)
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
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
        player?.release()
        player = null
    }
}
