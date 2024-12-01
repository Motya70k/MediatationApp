package ru.shvetsov.meditationapp.presentation.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.data.model.AudioGuide
import ru.shvetsov.meditationapp.data.service.AudioPlayerService
import ru.shvetsov.meditationapp.domain.usecase.HistoryUseCase
import ru.shvetsov.meditationapp.utils.Constant.ACTION_PAUSE
import ru.shvetsov.meditationapp.utils.Constant.ACTION_RESUME
import ru.shvetsov.meditationapp.utils.Constant.AUDIO_TITLE
import ru.shvetsov.meditationapp.utils.Constant.AUDIO_URL
import ru.shvetsov.meditationapp.utils.Constant.FIFTH_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.FIFTH_MEDITATION_GUIDE_URL
import ru.shvetsov.meditationapp.utils.Constant.FIRST_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.FIRST_MEDITATION_GUIDE_URL
import ru.shvetsov.meditationapp.utils.Constant.FOURTH_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.FOURTH_MEDITATION_GUIDE_URL
import ru.shvetsov.meditationapp.utils.Constant.SECOND_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.SECOND_MEDITATION_GUIDE_URL
import ru.shvetsov.meditationapp.utils.Constant.SEVENTH_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.SEVENTH_MEDITATION_GUIDE_URL
import ru.shvetsov.meditationapp.utils.Constant.SIXTH_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.SIXTH_MEDITATION_GUIDE_URL
import ru.shvetsov.meditationapp.utils.Constant.THIRD_MEDITATION_GUIDE
import ru.shvetsov.meditationapp.utils.Constant.THIRD_MEDITATION_GUIDE_URL
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val historyUseCase: HistoryUseCase,
    private val appContext: Application
) : ViewModel() {

    private val _time = MutableLiveData<Long>()
    val time: LiveData<Long> = _time

    private val _progress = MutableLiveData<Float>()
    val progress: LiveData<Float> = _progress

    private val _historyItem = MutableLiveData<List<History>>()
    val historyItem: LiveData<List<History>> = _historyItem

    private val _audioList = MutableLiveData<List<AudioGuide>>()
    val audioList: LiveData<List<AudioGuide>> get() = _audioList

    private val _playerProgress = MutableLiveData<Int>()
    val playerProgress: LiveData<Int> = _playerProgress

    private var timer: CountDownTimer? = null
    var remainingTime: Long = 0L
    private var isPaused = false
    var initialTime: Long = 0L

    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    var currentPlayingAudio: AudioGuide? = null

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra("PROGRESS", 0) ?: return
            _playerProgress.value = progress
        }
    }

    init {
        loadAudioGuides()
        LocalBroadcastManager.getInstance(appContext)
            .registerReceiver(progressReceiver, IntentFilter("UPDATE_PROGRESS"))
    }

    fun setTime(selectedTime: Long) {
        _time.value = selectedTime * 60 * 1000
        remainingTime = _time.value ?: 0L
        initialTime = remainingTime
    }

    fun startTimer(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        if (isPaused) {
            isPaused = false
        } else {
            remainingTime = _time.value ?: return
            initialTime = remainingTime
        }

        timer = object : CountDownTimer(remainingTime + 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                onTick(millisUntilFinished)
                if (remainingTime < 0) remainingTime = 0
                onTick(remainingTime)
                val progressValue = millisUntilFinished.toFloat() / initialTime
                _progress.value = progressValue
            }

            override fun onFinish() {
                onFinish()
                _progress.value = 0f
                remainingTime = 0L
            }
        }.start()

        isPaused = false
    }

    fun pauseTimer() {
        timer?.cancel()
        isPaused = true
    }

    fun stopTimer() {
        timer?.cancel()
        _progress.value = 0f
        isPaused = false
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(progressReceiver)
    }

    fun loadHistoryItem() {
        viewModelScope.launch {
            try {
                val historyList = historyUseCase.getAllHistory()
                _historyItem.value = historyList
            } catch (e: Exception) {
                Log.d("LoadItems", "Failed")
            }
        }
    }

    fun insertHistoryRecord(historyRecord: History) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyUseCase.insertHistoryRecord(historyRecord)
                val updatedHistoryItems = historyUseCase.getAllHistory()
                _historyItem.postValue(updatedHistoryItems)
            } catch (e: Exception) {
                Log.d("Load", "Failed")
            }
        }
    }

    fun startOrPauseAudio(audioGuide: AudioGuide) {
        if (currentPlayingAudio == null || currentPlayingAudio != audioGuide) {
            stopAudio()
            currentPlayingAudio = audioGuide
            startAudioService(audioGuide)

        } else {
            if (_isPlaying.value == true) {
                pauseAudioService()
            } else {
                resumeAudioService()
            }
        }
    }

    private fun startAudioService(audioGuide: AudioGuide) {
        val intent = Intent(appContext, AudioPlayerService::class.java).apply {
            putExtra(AUDIO_URL, audioGuide.url)
            putExtra(AUDIO_TITLE, audioGuide.title)
        }
        appContext.startService(intent)
        _isPlaying.value = true
    }

    private fun pauseAudioService() {
        val intent = Intent(appContext, AudioPlayerService::class.java).apply {
            action = ACTION_PAUSE
        }
        appContext.startService(intent)
        _isPlaying.value = false
    }

    private fun resumeAudioService() {
        val intent = Intent(appContext, AudioPlayerService::class.java).apply {
            action = ACTION_RESUME
        }
        appContext.startService(intent)
        _isPlaying.value = true
    }

    private fun stopService() {
        val intent = Intent(appContext, AudioPlayerService::class.java)
        appContext.stopService(intent)
        currentPlayingAudio = null
    }

    private fun stopAudio() {
        stopService()
        _isPlaying.value = false
    }

    fun loadAudioGuides() {
        val guides = listOf(
            AudioGuide(
                FIRST_MEDITATION_GUIDE,
                FIRST_MEDITATION_GUIDE_URL
            ),
            AudioGuide(
                SECOND_MEDITATION_GUIDE,
                SECOND_MEDITATION_GUIDE_URL
            ),
            AudioGuide(
                THIRD_MEDITATION_GUIDE,
                THIRD_MEDITATION_GUIDE_URL
            ),
            AudioGuide(
                FOURTH_MEDITATION_GUIDE,
                FOURTH_MEDITATION_GUIDE_URL
            ),
            AudioGuide(
                FIFTH_MEDITATION_GUIDE,
                FIFTH_MEDITATION_GUIDE_URL
            ),
            AudioGuide(
                SIXTH_MEDITATION_GUIDE,
                SIXTH_MEDITATION_GUIDE_URL
            ),
            AudioGuide(
                SEVENTH_MEDITATION_GUIDE,
                SEVENTH_MEDITATION_GUIDE_URL
            ),
        )
        _audioList.value = guides
    }
}