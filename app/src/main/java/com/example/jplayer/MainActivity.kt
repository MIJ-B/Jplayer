package com.example.jplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.jplayer.ui.fragments.AudioFragment
import com.example.jplayer.ui.fragments.PlaylistFragment
import com.example.jplayer.ui.fragments.SettingsFragment
import com.example.jplayer.ui.fragments.VideoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        bottomNav = findViewById(R.id.bottom_navigation)
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(AudioFragment())
        }

        // Setup bottom navigation listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_audio -> {
                    loadFragment(AudioFragment())
                    true
                }
                R.id.nav_video -> {
                    loadFragment(VideoFragment())
                    true
                }
                R.id.nav_playlists -> {
                    loadFragment(PlaylistFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}