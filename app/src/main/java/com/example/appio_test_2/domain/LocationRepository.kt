package com.example.appio_test_2.domain

import com.example.appio_test_2.data.repository.LocationResult

interface LocationRepository {
    suspend fun getCurrentCoordinate(): LocationResult
}