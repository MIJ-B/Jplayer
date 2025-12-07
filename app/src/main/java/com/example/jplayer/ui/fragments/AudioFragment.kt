package com.example.jplayer.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jplayer.R
import com.example.jplayer.data.MediaItem
import com.example.jplayer.ui.MediaAdapter
import com.example.jplayer.utils.MediaScanner
import kotlinx.coroutines.launch

class AudioFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: MediaAdapter
    
    private var allAudioFiles = listOf<MediaItem>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)

        setupRecyclerView()
        setupSwipeRefresh()
        checkPermissions()
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter(
            onItemClick = { mediaItem ->
                playAudio(mediaItem)
            },
            onMenuClick = { mediaItem, menuView ->
                showMediaMenu(mediaItem, menuView)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun playAudio(mediaItem: MediaItem) {
        val intent = android.content.Intent(requireContext(), PlayerActivity::class.java)
        intent.putExtra("media_item", mediaItem)
        startActivity(intent)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadAudioFiles()
        }
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            loadAudioFiles()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
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
                loadAudioFiles()
            } else {
                Toast.makeText(context, "Permission required to access audio files", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadAudioFiles() {
        lifecycleScope.launch {
            swipeRefresh.isRefreshing = true
            allAudioFiles = MediaScanner.scanAudioFiles(requireContext())
            adapter.submitList(allAudioFiles)
            swipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAudio(newText ?: "")
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterAudio(query: String) {
        val filteredList = allAudioFiles.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        adapter.submitList(filteredList)
    }

    private fun showMediaMenu(mediaItem: MediaItem, menuView: View) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), menuView)
        popup.menuInflater.inflate(R.menu.media_item_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_play -> {
                    Toast.makeText(context, "Playing: ${mediaItem.title}", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_add_to_playlist -> {
                    Toast.makeText(context, "Add to playlist", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_share -> {
                    Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_details -> {
                    showDetailsDialog(mediaItem)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showSortDialog() {
        val options = arrayOf("Title", "Artist", "Album", "Date Added", "Duration")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Sort by")
        builder.setItems(options) { _, which ->
            val sorted = when (which) {
                0 -> allAudioFiles.sortedBy { it.title }
                1 -> allAudioFiles.sortedBy { it.artist }
                2 -> allAudioFiles.sortedBy { it.album }
                3 -> allAudioFiles.sortedByDescending { it.dateAdded }
                4 -> allAudioFiles.sortedByDescending { it.duration }
                else -> allAudioFiles
            }
            adapter.submitList(sorted)
        }
        builder.show()
    }

    private fun showDetailsDialog(mediaItem: MediaItem) {
        val details = """
            Title: ${mediaItem.title}
            Artist: ${mediaItem.artist}
            Album: ${mediaItem.album}
            Duration: ${formatDuration(mediaItem.duration)}
            Size: ${formatFileSize(mediaItem.size)}
            Path: ${mediaItem.path}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Audio Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1) {
            String.format("%.2f MB", mb)
        } else {
            String.format("%.2f KB", kb)
        }
    }
}