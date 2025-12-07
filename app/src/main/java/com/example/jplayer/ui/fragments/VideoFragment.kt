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
import com.example.jplayer.PlayerActivity
import com.example.jplayer.data.MediaItem
import com.example.jplayer.ui.MediaAdapter
import com.example.jplayer.utils.MediaScanner
import kotlinx.coroutines.launch

class VideoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: MediaAdapter
    
    private var allVideoFiles = listOf<MediaItem>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 124
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
        return inflater.inflate(R.layout.fragment_video, container, false)
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
                playVideo(mediaItem)
            },
            onMenuClick = { mediaItem, menuView ->
                showMediaMenu(mediaItem, menuView)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun playVideo(mediaItem: MediaItem) {
        val intent = android.content.Intent(requireContext(), PlayerActivity::class.java)
        intent.putExtra("media_item", mediaItem)
        startActivity(intent)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadVideoFiles()
        }
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            loadVideoFiles()
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
                loadVideoFiles()
            } else {
                Toast.makeText(context, "Permission required to access video files", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadVideoFiles() {
        lifecycleScope.launch {
            swipeRefresh.isRefreshing = true
            allVideoFiles = MediaScanner.scanVideoFiles(requireContext())
            adapter.submitList(allVideoFiles)
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
                filterVideo(newText ?: "")
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

    private fun filterVideo(query: String) {
        val filteredList = allVideoFiles.filter {
            it.title.contains(query, ignoreCase = true)
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
        val options = arrayOf("Title", "Date Added", "Duration", "Size")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Sort by")
        builder.setItems(options) { _, which ->
            val sorted = when (which) {
                0 -> allVideoFiles.sortedBy { it.title }
                1 -> allVideoFiles.sortedByDescending { it.dateAdded }
                2 -> allVideoFiles.sortedByDescending { it.duration }
                3 -> allVideoFiles.sortedByDescending { it.size }
                else -> allVideoFiles
            }
            adapter.submitList(sorted)
        }
        builder.show()
    }

    private fun showDetailsDialog(mediaItem: MediaItem) {
        val details = """
            Title: ${mediaItem.title}
            Duration: ${formatDuration(mediaItem.duration)}
            Size: ${formatFileSize(mediaItem.size)}
            Path: ${mediaItem.path}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Video Details")
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