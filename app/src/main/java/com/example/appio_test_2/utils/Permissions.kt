package com.example.appio_test_2.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import javax.inject.Inject


class LocationPermissionManager @Inject constructor(
    private val fragment: Fragment,
    private val callback: PermissionGrande
) : PermissionManager {
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>> =
        fragment.requireActivity().registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    callback.onPermissionsGranted()
                }

                else -> {
                    callback.onPermissionsDenied()
                }
            }
        }

    override fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun hasLocationPermissions(): Boolean {
        val fineLocationGranted =
            fragment.requireActivity()
                .checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted =
            fragment.requireActivity()
                .checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }
}

interface PermissionManager {
    fun requestLocationPermissions()
    fun hasLocationPermissions(): Boolean
}

interface PermissionGrande {
    fun onPermissionsGranted()
    fun onPermissionsDenied()
}