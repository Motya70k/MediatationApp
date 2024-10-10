package ru.shvetsov.meditationapp.presentation.viewmodel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.shvetsov.meditationapp.data.db.HistoryDataBase
import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.domain.usecase.HistoryUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val historyUseCase: HistoryUseCase,
    private val historyDataBase: HistoryDataBase
) : ViewModel() {

    private val _time = MutableLiveData<Long>()
    val time: LiveData<Long> = _time

    private val _progress = MutableLiveData<Float>()
    val progress: LiveData<Float> = _progress

    private val _historyItem = MutableLiveData<List<History>>()
    val historyItem: LiveData<List<History>> = _historyItem

    private var timer: CountDownTimer? = null
    var remainingTime: Long = 0L
    private var isPaused = false
    private var initialTime: Long = 0L

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

        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                onTick(millisUntilFinished)
                val progressValue = millisUntilFinished.toFloat() / initialTime
                _progress.value = progressValue
            }

            override fun onFinish() {
                onFinish()
                _progress.value = 0f
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
    }

    fun loadHistoryItem() {
        viewModelScope.launch {
            try {
                _historyItem.value = historyUseCase.getAllHistory()
            } catch (e: Exception) {
                Log.d("LoadItems", "Failed")
            }
        }
    }

    fun insertHistoryRecord(historyRecord: History) {
        Log.d("InsertRecord", "Inserting record: $historyRecord")
        viewModelScope.launch {
            try {
                historyUseCase.insertHistoryRecord(historyRecord)
                val currentHistoryItems = _historyItem.value?.toMutableList() ?: mutableListOf()
                currentHistoryItems.add(historyRecord)
                _historyItem.postValue(currentHistoryItems)
                Log.d("Insert", "Success")
            } catch (e: Exception) {
                Log.d("Load", "Failed")
            }
        }
    }
}