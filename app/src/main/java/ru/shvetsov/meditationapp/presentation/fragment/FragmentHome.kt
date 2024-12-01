package ru.shvetsov.meditationapp.presentation.fragment

import android.Manifest
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.shvetsov.meditationapp.R
import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.databinding.FragmentHomeBinding
import ru.shvetsov.meditationapp.presentation.activity.MainActivity
import ru.shvetsov.meditationapp.presentation.viewmodel.MainViewModel
import ru.shvetsov.meditationapp.utils.Constant.CHANNEL_ID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class FragmentHome : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.time.observe(viewLifecycleOwner) {
            binding.timerTextView.text = formatTime(it)
        }

        viewModel.progress.observe(viewLifecycleOwner) {
            binding.timerCircle.setProgress(it)
        }

        binding.playButton.setOnClickListener {
            viewModel.startTimer(
                onTick = {
                    binding.timerTextView.text = formatTime(it)
                    binding.timerCircle.setProgress(it.toFloat())
                },
                onFinish = {
                    binding.timerTextView.text = getString(R.string._00_00)
                    binding.timerCircle.setProgress(0f)
                    saveMeditationRecord()
                    showNotification()
                }
            )
            binding.timerTextView.text = formatTime(viewModel.remainingTime)
        }

        binding.timerTextView.setOnClickListener {
            showTimePicker {
                viewModel.setTime(it)
                binding.timerTextView.text = formatTime(it * 60 * 1000)
            }
        }

        binding.stopButton.setOnClickListener {
            viewModel.stopTimer()
            saveMeditationRecord()
            binding.timerTextView.text = getString(R.string._00_00)
        }

        binding.pauseButton.setOnClickListener {
            viewModel.pauseTimer()
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val hours = (timeInMillis / 1000) / 3600
        val minutes = ((timeInMillis / 1000) % 3600) / 60
        val seconds = (timeInMillis / 1000) % 60
        return String.format(Locale("ru", "RU"), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatDate(): String {
        val date = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
        return date.format(Date())
    }

    private fun showTimePicker(onTimeSelected: (Long) -> Unit) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                val totalMinutes = hour * 60 + minute
                onTimeSelected(totalMinutes.toLong())
            },
            0, 0, true
        )
        timePickerDialog.show()
    }

    private fun showNotification() {
        val intent = Intent(requireActivity().applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification =
            NotificationCompat.Builder(requireActivity().applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_done_64)
                .setContentTitle(getString(R.string.meditation_done))
                .setContentText(getString(R.string.meditation_end))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        with(NotificationManagerCompat.from(requireContext())) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(1, notification)
            } else {
                Log.d("Notification", "Permission not granted for notifications")
            }
        }
    }

    private fun saveMeditationRecord() {
        val duration = viewModel.initialTime - viewModel.remainingTime
        val formattedDate = formatDate()
        val historyRecord = History(null, date = formattedDate, meditationTime = formatTime(duration))
        viewModel.insertHistoryRecord(historyRecord)
    }
}