package com.example.appio_test_2.domain

import com.example.appio_test_2.ui.MapPointUi

data class DomainPoint(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val name: String,
) {
    fun mapToUi() = MapPointUi(
        id, latitude, longitude, name
    )
}
