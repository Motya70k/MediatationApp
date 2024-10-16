package ru.shvetsov.meditationapp.data.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AudioPlayerService : Service() {

    companion object {
        const val ACTION_PAUSE = "ru.shvetsov.meditationapp.action.PAUSE"
        const val ACTION_RESUME = "ru.shvetsov.meditationapp.action.RESUME"
    }

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
        Log.d("AudioPlayerService", "Service started")
        val action = intent?.action

        when (action) {
            ACTION_PAUSE -> pauseAudio()
            ACTION_RESUME -> resumeAudio()
            else -> {
                audioUrl = intent?.getStringExtra("AUDIO_URL")
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
        val intent = Intent("UPDATE_PROGRESS").apply {
            putExtra("PROGRESS", progress)
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