package com.example.jplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jplayer.R
import com.example.jplayer.adapter.MediaAdapter
import com.example.jplayer.adapter.MediaGridAdapter
import com.example.jplayer.databinding.ActivityMainBinding
import com.example.jplayer.model.MediaItem
import com.example.jplayer.model.SortOrder
import com.example.jplayer.model.ViewMode
import com.example.jplayer.utils.PreferencesManager
import com.example.jplayer.utils.ThumbnailGenerator
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    
    private lateinit var videoAdapter: MediaAdapter
    private lateinit var videoGridAdapter: MediaGridAdapter
    
    private val videoList = mutableListOf<MediaItem>()
    private val audioList = mutableListOf<MediaItem>()
    
    private var currentTab = TAB_VIDEOS

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val TAB_VIDEOS = 0
        private const val TAB_AUDIO = 1
        private const val TAB_PLAYLISTS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)
        
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupSwipeRefresh()
        checkPermissions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "IPlayer"
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Videos"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Audio"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Playlists"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: TAB_VIDEOS
                updateView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        videoAdapter = MediaAdapter(videoList) { mediaItem ->
            openVideoPlayer(mediaItem)
        }
        
        videoGridAdapter = MediaGridAdapter(videoList) { mediaItem ->
            openVideoPlayer(mediaItem)
        }

        updateRecyclerViewLayout()
    }

    private fun updateRecyclerViewLayout() {
        when (prefsManager.viewMode) {
            ViewMode.LIST -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = videoAdapter
            }
            ViewMode.GRID -> {
                binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerView.adapter = videoGridAdapter
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadMedia()
        }
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                needPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            loadMedia()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadMedia()
            } else {
                Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMedia() {
        lifecycleScope.launch {
            loadVideos()
            loadAudio()
            binding.swipeRefresh.isRefreshing = false
            updateView()
        }
    }

    private suspend fun loadVideos() {
        videoList.clear()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            getSortOrder()
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val path = cursor.getString(pathColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                
                val uri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                videoList.add(
                    MediaItem(
                        id, name, uri, duration, size, 
                        dateAdded, path, null, width, height
                    )
                )
            }
        }
    }

    private fun loadAudio() {
        audioList.clear()
        // Similar to loadVideos but for audio
    }

    private fun getSortOrder(): String {
        return when (prefsManager.sortOrder) {
            SortOrder.NAME_ASC -> "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
            SortOrder.NAME_DESC -> "${MediaStore.Video.Media.DISPLAY_NAME} DESC"
            SortOrder.DATE_ADDED_DESC -> "${MediaStore.Video.Media.DATE_ADDED} DESC"
            SortOrder.DATE_ADDED_ASC -> "${MediaStore.Video.Media.DATE_ADDED} ASC"
            SortOrder.DURATION_DESC -> "${MediaStore.Video.Media.DURATION} DESC"
            SortOrder.DURATION_ASC -> "${MediaStore.Video.Media.DURATION} ASC"
            SortOrder.SIZE_DESC -> "${MediaStore.Video.Media.SIZE} DESC"
            SortOrder.SIZE_ASC -> "${MediaStore.Video.Media.SIZE} ASC"
        }
    }

    private fun updateView() {
        when (currentTab) {
            TAB_VIDEOS -> {
                videoAdapter.notifyDataSetChanged()
                videoGridAdapter.notifyDataSetChanged()
            }
            TAB_AUDIO -> {
                // Update audio view
            }
            TAB_PLAYLISTS -> {
                // Update playlists view
            }
        }
    }

    private fun openVideoPlayer(mediaItem: MediaItem) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("MEDIA_ITEM", mediaItem)
            putParcelableArrayListExtra("PLAYLIST", ArrayList(videoList))
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_view_mode -> {
                toggleViewMode()
                true
            }
            R.id.action_sort -> {
                showSortMenu()
                true
            }
            R.id.action_settings -> {
                // Open settings
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleViewMode() {
        prefsManager.viewMode = when (prefsManager.viewMode) {
            ViewMode.LIST -> ViewMode.GRID
            ViewMode.GRID -> ViewMode.LIST
        }
        updateRecyclerViewLayout()
        updateView()
    }

    private fun showSortMenu() {
        // Show sort options popup
    }
}
