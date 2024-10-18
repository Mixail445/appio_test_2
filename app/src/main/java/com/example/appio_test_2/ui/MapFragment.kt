package com.example.appio_test_2.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.appio_test_2.R
import com.example.appio_test_2.databinding.MapFragmentBinding
import com.example.appio_test_2.utils.Constants.ANCHOR_POINT
import com.example.appio_test_2.utils.Constants.DEFAULT_ZOOM_LEVEL
import com.example.appio_test_2.utils.Constants.EMPTY_STRING
import com.example.appio_test_2.utils.Constants.ICON_SCALE
import com.example.appio_test_2.utils.Constants.ONE_INT
import com.example.appio_test_2.utils.Constants.POLYLINE_STROKE_WIDTH
import com.example.appio_test_2.utils.Constants.ZERO_FLOAT
import com.example.appio_test_2.utils.CustomDialogCreator
import com.example.appio_test_2.utils.PermissionGrande
import com.example.appio_test_2.utils.PermissionManager
import com.example.appio_test_2.utils.subscribe
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), PermissionGrande {
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: Map
    private lateinit var placeMarkMapObject: PlacemarkMapObject

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var factory: MapVm.Factory

    @Inject
    lateinit var customDialogCreator: Lazy<CustomDialogCreator>

    private val viewModel: MapVm by viewModels {
        LambdaFactory(this) {
            factory.build(
                stringResOne = getString(R.string.fragment_map_current_point),
                stringResTwo = getString(
                    R.string.fragment_map_no_search
                )
            )
        }
    }

    private var isCameraMoved = false

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) = Unit

        override fun onMapLongTap(map: Map, point: Point) {
            handleLongClick(point)
        }
    }

    private fun initViewModel() {
        with(viewModel) {
            subscribe(uiLabels, ::handleUiLabel)
        }
    }

    private fun handleLongClick(point: Point) {
        viewModel.onEvent(MapView.Event.OnLongClickMap(point))
    }

    private fun handleTap(point: Point) {
        viewModel.onEvent(MapView.Event.OnClickPoint(point))
    }

    private fun handleUiLabel(uiLabel: MapView.UiLabel): Unit = when (uiLabel) {
        MapView.UiLabel.ShowSystemDialog -> showDialogHandlerToSettings()
        is MapView.UiLabel.ShowDialogCreateName -> showDialogCreateName(uiLabel.point)
        is MapView.UiLabel.ShowDialogRouteOrDeletePoint -> showDialogRoute(
            uiLabel.point, uiLabel.namePoint
        )

        is MapView.UiLabel.DrivingRoute -> drivingRouter(uiLabel.endPoint, uiLabel.startPoint)
        is MapView.UiLabel.CreatePlaceMark -> createPlaceMark(uiLabel.point, uiLabel.pointName)
    }

    private fun showDialogHandlerToSettings() {
        customDialogCreator.value.showSettingsDialog {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun showDialogRoute(point: Point, namePoint: String) {
        customDialogCreator.value.showRouteDialog(point, namePoint,
            onPositiveClick = {
                viewModel.onEvent(MapView.Event.OnClickDrivingRoute(point))
            },
            onNeutralClick = {
                viewModel.onEvent(MapView.Event.OnClickDeletePoint(point))
            }
        )
    }

    private fun showDialogCreateName(point: Point) {
        customDialogCreator.value.showCreateNameDialog { pointName ->
            viewModel.onEvent(MapView.Event.AddNamePoint(point, pointName))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)

        initViewModel()
        map = binding.mapview.mapWindow.map
        checkLocationPermissions()
        map.addInputListener(inputListener)

        return binding.root
    }

    private fun checkLocationPermissions() {
        moveToCurrentLocation()
        if (!permissionManager.hasLocationPermissions()) {
            permissionManager.requestLocationPermissions()
        } else {
            Unit
        }
    }

    private fun moveToCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                map.mapObjects.clear()

                state.point.forEach { uiPoint ->
                    createPlaceMark(
                        Point(uiPoint.latitude, uiPoint.longitude), uiPoint.name
                    )
                }
                viewModel
                if (!isCameraMoved && viewModel.currentCoordinate != null) {
                    val cameraPosition = CameraPosition(
                        viewModel.currentCoordinate ?: Point(),
                        DEFAULT_ZOOM_LEVEL,
                        ZERO_FLOAT,
                        ZERO_FLOAT
                    )

                    map.move(cameraPosition)
                    isCameraMoved = true
                }
            }
        }
    }


    private fun createPlaceMark(point: Point, pointName: String = EMPTY_STRING) {
        placeMarkMapObject = map.mapObjects.addPlacemark().apply {
            geometry = point
            setText(pointName)

            setIcon(
                ImageProvider.fromResource(
                    requireActivity(),
                    com.yandex.maps.mobile.R.drawable.search_layer_pin_icon_default
                ),
                IconStyle().apply {
                    anchor = ANCHOR_POINT
                    scale = ICON_SCALE
                }
            )

            isDraggable = true


            addTapListener { _, _ ->
                handleTap(point)
                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(this.context)
    }

    private fun drivingRouter(startPoint: Point, endPoint: Point) {
        val drivingRouter =
            DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)

        val drivingOptions = DrivingOptions().apply {
            routesCount = ONE_INT
        }

        val vehicleOptions = VehicleOptions()

        val points = buildList {
            add(RequestPoint(startPoint, RequestPointType.WAYPOINT, null, EMPTY_STRING))
            add(RequestPoint(endPoint, RequestPointType.WAYPOINT, null, EMPTY_STRING))
        }

        drivingRouter.requestRoutes(points,
            drivingOptions,
            vehicleOptions,
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
                    if (drivingRoutes.isNotEmpty()) {
                        drivingRoutes.map {
                            val polyline = map.mapObjects.addPolyline(it.geometry)
                            polyline.setStrokeColor(Color.GRAY)
                            polyline.strokeWidth = POLYLINE_STROKE_WIDTH
                        }
                    }
                }

                override fun onDrivingRoutesError(p0: com.yandex.runtime.Error) {
                    Log.e(
                        getString(R.string.map_fragment_name),
                        getString(R.string.map_fragment_error_fetch, p0)
                    )
                }
            })
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onPermissionsGranted() {
        viewModel.onEvent(MapView.Event.RequestCoordinate)
    }

    override fun onPermissionsDenied() {
        Toast.makeText(
            requireContext(),
            getString(R.string.map_fragment_permission), Toast.LENGTH_SHORT
        ).show()
    }

}