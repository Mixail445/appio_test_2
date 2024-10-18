package com.example.appio_test_2.ui

import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.appio_test_2.data.repository.LocationResult
import com.example.appio_test_2.data.repository.local.PlaceEntity
import com.example.appio_test_2.domain.LocationRepository
import com.example.appio_test_2.domain.PlaceLocalSource
import com.example.appio_test_2.utils.Constants.DELTA
import com.example.appio_test_2.utils.Constants.DELTA_TWO
import com.example.appio_test_2.utils.Constants.EMPTY_STRING
import com.example.appio_test_2.utils.Constants.ERROR_MESSAGE
import com.example.appio_test_2.utils.Constants.NOT_FOUND_MESSAGE
import com.example.appio_test_2.utils.Constants.TAG_ERROR
import com.example.appio_test_2.utils.Constants.TAG_NOT_FOUND
import com.example.appio_test_2.utils.Constants.ZERO_INT
import com.example.appio_test_2.utils.SingleLiveData
import com.yandex.mapkit.geometry.Point
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs


class MapVm @AssistedInject constructor(
    @Assisted("stringResOne") private val stringResOne: String,
    @Assisted("stringResTwo") private val stringResTwo: String,
    private val locationRepository: LocationRepository,
    private val placeLocalSource: PlaceLocalSource,
) : ViewModel() {


    private val _uiState = MutableStateFlow(MapView.Model(listOf(MapPointUi())))
    val uiState: StateFlow<MapView.Model> = _uiState.asStateFlow()

    private val _uiLabels = SingleLiveData<MapView.UiLabel>()
    val uiLabels: LiveData<MapView.UiLabel> get() = _uiLabels

    var currentCoordinate: Point? = null


    init {
        viewModelScope.launch {
            fetchCurrentCoordinate()
            fetchAllPoints()
        }
    }

    private suspend fun fetchCurrentCoordinate() {
        when (val result = locationRepository.getCurrentCoordinate()) {
            is LocationResult.Success -> {
                currentCoordinate = Point(result.location.latitude, result.location.longitude)
                saveCurrentLocation()
                emitCreatePlaceMark()
            }

            is LocationResult.NotFound -> {
                Log.d(TAG_NOT_FOUND, NOT_FOUND_MESSAGE)
            }

            is LocationResult.PermissionDenied -> {
                _uiLabels.postValue(MapView.UiLabel.ShowSystemDialog)
            }

            is LocationResult.Error -> {
                Log.d(TAG_ERROR, ERROR_MESSAGE)
            }
        }
    }

    private suspend fun saveCurrentLocation() {
        currentCoordinate?.let { coordinate ->
            val placeEntity = PlaceEntity(
                id = ZERO_INT.toLong(),
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                name = stringResOne
            )
            placeLocalSource.insertPlace(placeEntity)

            _uiState.update { state ->
                state.copy(
                    point = listOf(
                        MapPointUi(
                            coordinate.latitude.toLong(),
                            coordinate.longitude
                        )
                    )
                )
            }
        }
    }

    private fun emitCreatePlaceMark() {
        currentCoordinate?.let { coordinate ->
            _uiLabels.postValue(MapView.UiLabel.CreatePlaceMark(coordinate, stringResOne))
        }
    }


    fun onEvent(event: MapView.Event) {
        when (event) {
            is MapView.Event.OnLongClickMap -> handlerLongClick(event.point)
            is MapView.Event.OnClickDeletePoint -> handlerDeletePoint(event.point)
            is MapView.Event.OnClickDrivingRoute -> handleClickDriveRoute(event.endPoint)
            is MapView.Event.AddNamePoint -> handlerAddName(event.point, event.pointName)
            is MapView.Event.OnClickPoint -> handlerClickPoint(event.point)
            MapView.Event.RequestCoordinate -> handlerCurrentCoordinate()
        }
    }

    private fun handlerCurrentCoordinate() {
        viewModelScope.launch {
            fetchCurrentCoordinate()
        }
    }

    private fun handlerAddName(point: Point, pointName: String) {
        viewModelScope.launch {
            val places = placeLocalSource.getAllPlace().first()

            val existingPlace = places.firstOrNull {
                abs(it.latitude - point.latitude) < DELTA && abs(it.longitude - point.longitude) < DELTA
            }

            existingPlace?.let { place ->
                val updatedPlaceEntity = place.copy(name = pointName)
                placeLocalSource.updatePlace(updatedPlaceEntity)
            } ?: run {
                val newPlaceEntity = PlaceEntity(
                    id = ZERO_INT.toLong(),
                    latitude = point.latitude,
                    longitude = point.longitude,
                    name = pointName
                )
                placeLocalSource.insertPlace(newPlaceEntity)
            }

            _uiLabels.postValue(MapView.UiLabel.CreatePlaceMark(point, pointName))
            fetchAllPoints()
        }
    }

    private fun handleClickDriveRoute(endPoint: Point) {
        _uiLabels.postValue(currentCoordinate?.let { MapView.UiLabel.DrivingRoute(it, endPoint) })
    }

    private fun handlerClickPoint(point: Point) {
        viewModelScope.launch {
            val places = placeLocalSource.getAllPlace().first()

            val existingPlace = places.firstOrNull {
                abs(it.latitude - point.latitude) < DELTA_TWO && abs(it.longitude - point.longitude) < DELTA_TWO
            }

            existingPlace?.let { place ->
                val pointName = place.name
                _uiLabels.postValue(MapView.UiLabel.ShowDialogRouteOrDeletePoint(point, pointName))
            } ?: run {
                _uiLabels.postValue(
                    MapView.UiLabel.ShowDialogRouteOrDeletePoint(
                        point,
                        stringResTwo
                    )
                )
            }
        }
    }

    private fun handlerDeletePoint(point: Point) {
        viewModelScope.launch {
            val delta = DELTA

            val placeToDelete = _uiState.value.point.find {
                abs(it.latitude - point.latitude) < delta && abs(it.longitude - point.longitude) < delta
            }

            placeToDelete?.let { place ->

                placeLocalSource.deletePlace(place.id)

                fetchAllPoints()
            }
        }
    }

    private fun handlerLongClick(point: Point) {
        _uiLabels.postValue(MapView.UiLabel.ShowDialogCreateName(point))
        viewModelScope.launch {
            val placeEntity = PlaceEntity(
                id = ZERO_INT.toLong(),
                latitude = point.latitude,
                longitude = point.longitude,
                name = EMPTY_STRING
            )
            placeLocalSource.insertPlace(placeEntity)
            fetchAllPoints()
        }
    }

    private fun fetchAllPoints() {
        viewModelScope.launch {
            placeLocalSource.getAllPlace().collect { places ->
                _uiState.update { state ->
                    state.copy(
                        point = places.map { it.mapToDomain().mapToUi() }
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun build(
            @Assisted("stringResOne") stringResOne: String,
            @Assisted("stringResTwo") stringResTwo: String,
        ): MapVm
    }
}


@Suppress("UNCHECKED_CAST")
class LambdaFactory<T : ViewModel>(
    savedStateRegistryOwner: SavedStateRegistryOwner,
    private val create: (handle: SavedStateHandle) -> T,
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return create(handle) as T
    }
}


