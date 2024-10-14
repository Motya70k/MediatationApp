package ru.shvetsov.meditationapp.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.shvetsov.meditationapp.databinding.FragmentGuideBinding
import ru.shvetsov.meditationapp.presentation.adapter.GuideItemAdapter
import ru.shvetsov.meditationapp.presentation.viewmodel.MainViewModel

@AndroidEntryPoint
class FragmentGuide : Fragment() {

    private lateinit var binding: FragmentGuideBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var guideItemAdapter: GuideItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observe()
        viewModel.loadAudioGuides()
    }

    private fun initRecyclerView() {
        binding.rcViewGuide.apply {
            layoutManager = LinearLayoutManager(context)
            guideItemAdapter = GuideItemAdapter(emptyList(), viewModel) { audioGuide ->
                viewModel.startOrPauseAudio(audioGuide)
            }
            adapter = guideItemAdapter
        }
    }

    private fun observe() {
        viewModel.audioList.observe(viewLifecycleOwner) { audioGuides ->
            guideItemAdapter.updateItems(audioGuides)
        }
    }
}