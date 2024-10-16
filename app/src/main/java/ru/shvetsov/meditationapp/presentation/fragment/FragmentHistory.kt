package ru.shvetsov.meditationapp.presentation.fragment

import ru.shvetsov.meditationapp.presentation.adapter.HistoryItemAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.shvetsov.meditationapp.databinding.FragmentHistoryBinding
import ru.shvetsov.meditationapp.presentation.viewmodel.MainViewModel

@AndroidEntryPoint
class FragmentHistory : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var historyItemAdapter: HistoryItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observe()
        viewModel.loadHistoryItem()
    }

    private fun initRecyclerView() {
        binding.rcViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            historyItemAdapter = HistoryItemAdapter(emptyList())
            adapter = historyItemAdapter
        }
    }

    private fun observe() {
        viewModel.historyItem.observe(viewLifecycleOwner) { historyList ->
            historyItemAdapter.updateItems(historyList)
        }
    }
}