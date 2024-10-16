package ru.shvetsov.meditationapp.data.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class AudioPlayerService : Service() {

    companion object {
        const val ACTION_PAUSE = "ru.shvetsov.meditationapp.action.PAUSE"
        const val ACTION_RESUME = "ru.shvetsov.meditationapp.action.RESUME"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioUrl: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        Log.d("AudioPlayerService", "Starting audio with URL: $audioUrl")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    start()
                }
                prepareAsync()
            }
        } else {
            mediaPlayer?.start()
        }
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