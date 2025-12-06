package com.example.jplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jplayer.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsItems(view)
    }

    private fun setupSettingsItems(view: View) {
        // Theme setting
        view.findViewById<LinearLayout>(R.id.setting_theme).setOnClickListener {
            showThemeDialog()
        }

        // Audio quality setting
        view.findViewById<LinearLayout>(R.id.setting_audio_quality).setOnClickListener {
            showAudioQualityDialog()
        }

        // Video quality setting
        view.findViewById<LinearLayout>(R.id.setting_video_quality).setOnClickListener {
            showVideoQualityDialog()
        }

        // Storage location
        view.findViewById<LinearLayout>(R.id.setting_storage).setOnClickListener {
            Toast.makeText(context, "Storage settings", Toast.LENGTH_SHORT).show()
        }

        // Equalizer
        view.findViewById<LinearLayout>(R.id.setting_equalizer).setOnClickListener {
            Toast.makeText(context, "Equalizer coming soon", Toast.LENGTH_SHORT).show()
        }

        // About
        view.findViewById<LinearLayout>(R.id.setting_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Theme")
            .setItems(themes) { _, which ->
                val theme = themes[which]
                Toast.makeText(context, "Theme: $theme", Toast.LENGTH_SHORT).show()
                // TODO: Implement theme change
            }
            .show()
    }

    private fun showAudioQualityDialog() {
        val qualities = arrayOf("Low (64 kbps)", "Normal (128 kbps)", "High (320 kbps)")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Audio Quality")
            .setItems(qualities) { _, which ->
                val quality = qualities[which]
                Toast.makeText(context, "Audio quality: $quality", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showVideoQualityDialog() {
        val qualities = arrayOf("Auto", "360p", "480p", "720p", "1080p")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Video Quality")
            .setItems(qualities) { _, which ->
                val quality = qualities[which]
                Toast.makeText(context, "Video quality: $quality", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAboutDialog() {
        val message = """
            JPlayer v1.0.0
            
            A modern media player for Android
            
            Features:
            • Audio & Video playback
            • Playlist management
            • Equalizer support
            • Material Design UI
            
            © 2024 JPlayer
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("About JPlayer")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
