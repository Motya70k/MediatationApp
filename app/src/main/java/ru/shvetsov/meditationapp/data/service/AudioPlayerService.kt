package ru.shvetsov.meditationapp.data.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.shvetsov.meditationapp.utils.Constant.ACTION_PAUSE
import ru.shvetsov.meditationapp.utils.Constant.ACTION_RESUME
import ru.shvetsov.meditationapp.utils.Constant.AUDIO_URL
import ru.shvetsov.meditationapp.utils.Constant.PROGRESS
import ru.shvetsov.meditationapp.utils.Constant.UPDATE_PROGRESS

class AudioPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var audioUrl: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val progress = (it.currentPosition * 100) / it.duration
                updateProgress(progress)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PAUSE -> pauseAudio()
            ACTION_RESUME -> resumeAudio()
            else -> {
                audioUrl = intent?.getStringExtra(AUDIO_URL)
                audioUrl?.let {
                    startAudio(it)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startAudio(audioUrl: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    start()
                    handler.post(updateProgressTask)
                }
                prepareAsync()
            }
        } else {
            mediaPlayer?.start()
            handler.post(updateProgressTask)
        }
    }

    private fun updateProgress(progress: Int) {
        val intent = Intent(UPDATE_PROGRESS).apply {
            putExtra(PROGRESS, progress)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
    }

    private fun resumeAudio() {
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}