package com.example.appio_test_2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appio_test_2.databinding.ActivityMainBinding
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        enableEdgeToEdge()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
