package com.example.jplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jplayer.adapter.MediaAdapter
import com.example.jplayer.databinding.ActivityMainBinding
import com.example.jplayer.model.MediaItem

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaAdapter: MediaAdapter
    private val mediaList = mutableListOf<MediaItem>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        checkPermissions()
    }

    private fun setupUI() {
        supportActionBar?.title = "IPlayer"
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(mediaList) { mediaItem ->
            openPlayer(mediaItem)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mediaAdapter
            setHasFixedSize(true)
        }
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, 
                arrayOf(permission), 
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
            if (grantResults.isNotEmpty() && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMedia()
            } else {
                Toast.makeText(
                    this, 
                    "Permission denied. Cannot load videos.", 
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadMedia() {
    mediaList.clear()
    
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT
    )

    val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

    val cursor = contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
        val widthColumn = it.getColumnIndex(MediaStore.Video.Media.WIDTH)
        val heightColumn = it.getColumnIndex(MediaStore.Video.Media.HEIGHT)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn)
            val duration = it.getLong(durationColumn)
            val size = it.getLong(sizeColumn)
            val path = it.getString(pathColumn)
            val dateAdded = it.getLong(dateColumn)
            val width = if (widthColumn >= 0) it.getInt(widthColumn) else 0
            val height = if (heightColumn >= 0) it.getInt(heightColumn) else 0
            
            val uri = Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
                id.toString()
            )

            mediaList.add(
                MediaItem(
                    id = id,
                    name = name,
                    uri = uri,
                    duration = duration,
                    size = size,
                    dateAdded = dateAdded,
                    path = path,
                    width = width,
                    height = height
                )
            )
        }
    }

    mediaAdapter.notifyDataSetChanged()
    
    if (mediaList.isEmpty()) {
        Toast.makeText(this, "No videos found", Toast.LENGTH_SHORT).show()
    }
}

    private fun openPlayer(mediaItem: MediaItem) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("MEDIA_URI", mediaItem.uri.toString())
            putExtra("MEDIA_TITLE", mediaItem.name)
        }
        startActivity(intent)
    }
}
