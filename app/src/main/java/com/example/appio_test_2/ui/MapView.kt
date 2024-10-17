package com.example.appio_test_2.ui

import com.example.appio_test_2.utils.Constants.EMPTY_STRING
import com.example.appio_test_2.utils.Constants.ZERO_DOUBLE
import com.example.appio_test_2.utils.Constants.ZERO_LONG

interface MapView {
    data class Model(
        val point: List<MapPointUi> = emptyList()
    )

    sealed class Event {
        data class OnLongClickMap(val point: com.yandex.mapkit.geometry.Point) : Event()
        data class OnClickPoint(val point: com.yandex.mapkit.geometry.Point) : Event()
        data class OnClickDeletePoint(val point: com.yandex.mapkit.geometry.Point) : Event()
        data class OnClickDrivingRoute(val endPoint: com.yandex.mapkit.geometry.Point) : Event()
        data class AddNamePoint(
            val point: com.yandex.mapkit.geometry.Point, val pointName: String
        ) : Event()
    }

    sealed class UiLabel {
        data object ShowSystemDialog : UiLabel()
        data class ShowDialogCreateName(val point: com.yandex.mapkit.geometry.Point) : UiLabel()
        data class ShowDialogRouteOrDeletePoint(
            val point: com.yandex.mapkit.geometry.Point, val namePoint: String
        ) : UiLabel()

        data class DrivingRoute(
            val startPoint: com.yandex.mapkit.geometry.Point,
            val endPoint: com.yandex.mapkit.geometry.Point
        ) : UiLabel()

        data class CreatePlaceMark(
            val point: com.yandex.mapkit.geometry.Point, val pointName: String = EMPTY_STRING
        ) : UiLabel()
    }
}

data class MapPointUi(
    val id: Long = ZERO_LONG.toLong(),
    val latitude: Double = ZERO_DOUBLE,
    val longitude: Double = ZERO_DOUBLE,
    val name: String = EMPTY_STRING,
)