package com.example.appio_test_2.data.repository.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.appio_test_2.domain.DomainPoint

@Entity(tableName = "PlaceEntity")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val name: String = "",
) {
    fun mapToDomain() = DomainPoint(
        id = id,
        latitude = latitude,
        longitude = longitude,
        name = name
    )
}