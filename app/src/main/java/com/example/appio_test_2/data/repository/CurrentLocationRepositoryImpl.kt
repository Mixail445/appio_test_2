package com.example.appio_test_2.data.repository

import android.annotation.SuppressLint
import android.location.Location
import com.example.appio_test_2.domain.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class CurrentLocationRepositoryImpl @Inject constructor(private val fusedLocationClient: FusedLocationProviderClient) :
    LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentCoordinate(): LocationResult {
        val locationResult = CompletableDeferred<LocationResult>()

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                locationResult.complete(LocationResult.Success(location))
            } else {
                locationResult.complete(LocationResult.NotFound)
            }
        }.addOnFailureListener { exception ->
            locationResult.complete(LocationResult.Error(exception))
        }

        return locationResult.await()
    }
}

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data object NotFound : LocationResult()
    data object PermissionDenied : LocationResult()
    data class Error(val exception: Exception) : LocationResult()
}