package com.example.jplayer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jplayer.PlayerActivity  // ← OVA ITO (esory .ui)
import com.example.jplayer.adapter.MediaAdapter
import com.example.jplayer.databinding.FragmentAudioBinding
import com.example.jplayer.repository.MediaRepository

class AudioFragment : Fragment() {
    private var _binding: FragmentAudioBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var mediaRepository: MediaRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mediaRepository = MediaRepository(requireContext())
        setupRecyclerView()
        loadAudioFiles()
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter { mediaItem ->
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("media_item", mediaItem)
            startActivity(intent)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mediaAdapter
        }
    }

    private fun loadAudioFiles() {
        val audioFiles = mediaRepository.getAudioFiles()
        mediaAdapter.submitList(audioFiles)
        
        binding.emptyView.visibility = if (audioFiles.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}