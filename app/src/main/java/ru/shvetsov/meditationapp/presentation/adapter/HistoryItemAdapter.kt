package ru.shvetsov.meditationapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.databinding.HistoryItemBinding

class HistoryItemAdapter(private var historyItems: List<History>) :
    RecyclerView.Adapter<HistoryItemAdapter.ViewHolder>() {

        class ViewHolder(private val binding: HistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(historyItem: History) {
                binding.meditationDateTextView.text = historyItem.date
                binding.meditationTimeTextView.text = historyItem.meditationTime.toString()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HistoryItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyItems[position]
        holder.bind(historyItem)
    }

    fun updateItems(newItems: List<History>) {
        historyItems = newItems
        notifyDataSetChanged()
    }
}