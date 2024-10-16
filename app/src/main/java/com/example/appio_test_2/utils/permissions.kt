package com.example.appio_test_2.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.appio_test_2.MainActivity


class PermissionManager(private val activity: MainActivity) {

    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    init {
        locationPermissionRequest = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    onLocationPermissionGranted()
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    onApproximateLocationPermissionGranted()
                }

                else -> {
                    onLocationPermissionDenied()
                }
            }
        }
    }

    fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun onLocationPermissionGranted() = Unit

    private fun onApproximateLocationPermissionGranted() = Unit

    private fun onLocationPermissionDenied() = Unit

    fun hasLocationPermissions(): Boolean {
        val fineLocationGranted =
            activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted =
            activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }
}