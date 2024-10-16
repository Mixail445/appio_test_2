package com.example.appio_test_2.domain

import com.example.appio_test_2.data.repository.local.PlaceEntity
import kotlinx.coroutines.flow.Flow

interface PlaceLocalSource {
    suspend fun getAllPlace(): Flow<List<PlaceEntity>>
    suspend fun deletePlace(id: Long)
    suspend fun insertPlace(place: PlaceEntity)
    suspend fun updatePlace(place: PlaceEntity)
}
