package ru.shvetsov.meditationapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.shvetsov.meditationapp.data.model.AudioGuide
import ru.shvetsov.meditationapp.databinding.GuideItemBinding
import ru.shvetsov.meditationapp.presentation.viewmodel.MainViewModel

class GuideItemAdapter(
    private var audioList: List<AudioGuide>,
    private val viewModel: MainViewModel,
    private val onPlayClick: (AudioGuide) -> Unit
) : RecyclerView.Adapter<GuideItemAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: GuideItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(audioGuide: AudioGuide) {
            binding.guideNameTextView.text = audioGuide.title
            binding.playButton.setOnClickListener {
                onPlayClick(audioGuide)
            }
            val progress = calculateProgress(audioGuide)
            binding.progressBar.progress = progress
        }
        private fun calculateProgress(audioGuide: AudioGuide): Int {
            // Логика расчета прогресса (например, из viewModel)
            return viewModel.getProgressForAudio(audioGuide.url)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GuideItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audioGuide = audioList[position]
        holder.bind(audioGuide)
    }

    fun updateItems(newItems: List<AudioGuide>) {
        audioList = newItems
        notifyDataSetChanged()
    }
}