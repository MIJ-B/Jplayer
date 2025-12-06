package com.example.jplayer.ui.fragments

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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlaylistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fabAddPlaylist: FloatingActionButton

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
        
        // Show empty state for now
        showEmptyState()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        // TODO: Add adapter when playlist data is ready
    }

    private fun setupFab() {
        fabAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = "No playlists yet\n\nTap + to create a new playlist"
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
        // TODO: Implement playlist creation
        Toast.makeText(context, "Playlist '$name' created", Toast.LENGTH_SHORT).show()
    }
}
