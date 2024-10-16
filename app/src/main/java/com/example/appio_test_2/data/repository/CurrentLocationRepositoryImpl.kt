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
    override suspend fun getCurrentCoordinate(): Result<Location> {
        val locationResult = CompletableDeferred<Result<Location>>()

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                locationResult.complete(Result.success(location))
            } else {
                locationResult.complete(Result.failure(Exception("Location not found")))
            }
        }.addOnFailureListener { exception ->
            locationResult.complete(Result.failure(exception))
        }

        return locationResult.await()
    }
}
