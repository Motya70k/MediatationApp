package ru.shvetsov.meditationapp.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.shvetsov.meditationapp.R
import ru.shvetsov.meditationapp.databinding.ActivityMainBinding
import ru.shvetsov.meditationapp.presentation.fragment.FragmentHistory
import ru.shvetsov.meditationapp.presentation.fragment.FragmentHome

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragmentHome = FragmentHome()
    private val fragmentHistory = FragmentHistory()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestNotificationPermission()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentHolder, fragmentHome)
            .commit()
        setBottomNavigationListener()
    }

    private fun setBottomNavigationListener() {
        binding.bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main_menu_item -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, fragmentHome)
                        .commit()
                }

                R.id.meditation_guide_menu_item -> {
                    Log.d("guide", "open")
                }

                R.id.history_menu_item -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, fragmentHistory)
                        .commit()
                }
            }
            true
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}