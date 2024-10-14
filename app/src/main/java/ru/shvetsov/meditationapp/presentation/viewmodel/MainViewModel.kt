package ru.shvetsov.meditationapp.presentation.viewmodel

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.data.model.AudioGuide
import ru.shvetsov.meditationapp.domain.usecase.HistoryUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val historyUseCase: HistoryUseCase,
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

    private val handler = Handler(Looper.getMainLooper())
    private var updateProgressTask: Runnable? = null

    private var timer: CountDownTimer? = null
    var remainingTime: Long = 0L
    private var isPaused = false
    var initialTime: Long = 0L

    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    init {
        loadAudioGuides()
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

        onTick(remainingTime)

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
        remainingTime = 0L
        isPaused = false
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun loadHistoryItem() {
        viewModelScope.launch {
            try {
                val historyList = historyUseCase.getAllHistory()
                Log.d("LoadHistory", "History loaded: ${historyList.size} items")
                _historyItem.value = historyList
            } catch (e: Exception) {
                Log.d("LoadItems", "Failed")
            }
        }
    }

    fun insertHistoryRecord(historyRecord: History) {
        Log.d("InsertRecord", "Inserting record: $historyRecord")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyUseCase.insertHistoryRecord(historyRecord)
                val updatedHistoryItems = historyUseCase.getAllHistory()
                _historyItem.postValue(updatedHistoryItems)
                Log.d("Insert", "Success")
            } catch (e: Exception) {
                Log.d("Load", "Failed")
            }
        }
    }

    fun startOrPauseAudio(audioGuide: AudioGuide) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioGuide.url)
                prepare()
                start()
                _isPlaying.value = true
                startUpdatingProgress()

                setOnCompletionListener {
                    stopAudio()
                }
            }
        } else if (_isPlaying.value == true) {
            mediaPlayer?.pause()
            _isPlaying.value = false
            stopUpdatingProgress()
        } else {
            mediaPlayer?.start()
            _isPlaying.value = true
            startUpdatingProgress()
        }
    }

    private fun startUpdatingProgress() {
        updateProgressTask = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val totalDuration = it.duration
                    val currentDuration = it.currentPosition
                    _playerProgress.postValue((currentDuration * 100) / totalDuration)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(updateProgressTask!!)
    }

    private fun stopUpdatingProgress() {
        updateProgressTask?.let { handler.removeCallbacks(it) }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        stopUpdatingProgress()
    }

    fun loadAudioGuides() {
        val guides = listOf(
            AudioGuide("Изучаем ощущения тела через позу 1", "https://victorshiryaev.org/wp-content/uploads/2016/09/01-posture-awareness.mp3"),
            AudioGuide("Изучаем ощущения тела через позу 2", "https://victorshiryaev.org/wp-content/uploads/2016/09/02-posture-awareness-2.mp3"),
            AudioGuide("Расслабление тела, расслабление ума", "https://victorshiryaev.org/wp-content/uploads/2016/09/03-calm-body-calm-mind.mp3"),
            AudioGuide("Практика сканирования тела", "https://victorshiryaev.org/wp-content/uploads/2016/09/04-body-sensations-awareness.mp3"),
            AudioGuide("Исследуем ощущения дыхания", "https://victorshiryaev.org/wp-content/uploads/2016/09/05-concentration-on-breathing.mp3"),
            AudioGuide("Дыхание и расслабление", "https://victorshiryaev.org/wp-content/uploads/2016/09/06-breath-relaxation_final.mp3"),
            AudioGuide("Сосредоточение и отмечание дыхания", "https://victorshiryaev.org/wp-content/uploads/2016/09/07-breath-concentration.mp3"),
        )
        _audioList.value = guides
    }

    fun getProgressForAudio(audioUrl: String): Int {
        return playerProgress.value ?: 0
    }
}