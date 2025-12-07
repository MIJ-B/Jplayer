package com.example.jplayer.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jplayer.R
import com.example.jplayer.data.Playlist
import com.example.jplayer.ui.PlaylistAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlaylistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fabAddPlaylist: FloatingActionButton
    private lateinit var adapter: PlaylistAdapter
    
    private val playlists = mutableListOf<Playlist>()
    private val sharedPrefs by lazy {
        requireContext().getSharedPreferences("playlists", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        emptyView = view.findViewById(R.id.empty_view)
        fabAddPlaylist = view.findViewById(R.id.fab_add_playlist)

        setupRecyclerView()
        setupFab()
        loadPlaylists()
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(
            onItemClick = { playlist ->
                Toast.makeText(context, "Opening: ${playlist.name}", Toast.LENGTH_SHORT).show()
            },
            onMenuClick = { playlist, view ->
                showPlaylistMenu(playlist, view)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun loadPlaylists() {
        playlists.clear()
        val playlistCount = sharedPrefs.getInt("playlist_count", 0)
        
        for (i in 0 until playlistCount) {
            val name = sharedPrefs.getString("playlist_${i}_name", "") ?: continue
            val count = sharedPrefs.getInt("playlist_${i}_count", 0)
            val date = sharedPrefs.getLong("playlist_${i}_date", 0)
            
            playlists.add(Playlist(i.toLong(), name, count, date))
        }
        
        updateUI()
    }

    private fun updateUI() {
        if (playlists.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            adapter.submitList(playlists.toList())
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun showCreatePlaylistDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
        input.hint = "Playlist name"
        input.setPadding(50, 30, 50, 30)

        builder.setTitle("Create Playlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val playlistName = input.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    createPlaylist(playlistName)
                } else {
                    Toast.makeText(context, "Please enter a playlist name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createPlaylist(name: String) {
        val currentCount = sharedPrefs.getInt("playlist_count", 0)
        val currentTime = System.currentTimeMillis() / 1000
        
        sharedPrefs.edit().apply {
            putInt("playlist_count", currentCount + 1)
            putString("playlist_${currentCount}_name", name)
            putInt("playlist_${currentCount}_count", 0)
            putLong("playlist_${currentCount}_date", currentTime)
            apply()
        }
        
        Toast.makeText(context, "Playlist '$name' created", Toast.LENGTH_SHORT).show()
        loadPlaylists()
    }

    private fun showPlaylistMenu(playlist: Playlist, view: View) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
        popup.menu.add("Rename")
        popup.menu.add("Delete")
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Rename" -> {
                    renamePlaylist(playlist)
                    true
                }
                "Delete" -> {
                    deletePlaylist(playlist)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun renamePlaylist(playlist: Playlist) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
        input.setText(playlist.name)
        input.setPadding(50, 30, 50, 30)

        builder.setTitle("Rename Playlist")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    sharedPrefs.edit()
                        .putString("playlist_${playlist.id}_name", newName)
                        .apply()
                    Toast.makeText(context, "Playlist renamed", Toast.LENGTH_SHORT).show()
                    loadPlaylists()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlaylist(playlist: Playlist) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete '${playlist.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                // Simple deletion - just mark as deleted
                sharedPrefs.edit()
                    .remove("playlist_${playlist.id}_name")
                    .remove("playlist_${playlist.id}_count")
                    .remove("playlist_${playlist.id}_date")
                    .apply()
                Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                loadPlaylists()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}