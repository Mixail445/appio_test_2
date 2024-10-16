package com.example.appio_test_2.domain

import android.location.Location

interface LocationRepository {
    suspend fun getCurrentCoordinate(): Result<Location>
}