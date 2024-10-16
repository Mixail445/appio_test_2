package com.example.appio_test_2.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.appio_test_2.R
import com.example.appio_test_2.databinding.MapFragmentBinding
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
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: Map
    private lateinit var placeMarkMapObject: PlacemarkMapObject

    @Inject
    lateinit var factory: MapVm.Factory
    private val viewModel: MapVm by viewModels {
        LambdaFactory(this) {
            factory.build(
                it,
                stringResOne = getString(R.string.fragment_map_current_point),
                stringResTwo = getString(
                    R.string.fragment_map_no_search
                )
            )
        }
    }

    private val placeMarkTapListener = MapObjectTapListener { _, point ->
        viewModel.onEvent(MapView.Event.OnClickPoint(point))
        true
    }
    private var isCameraMoved = false

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) = Unit

        override fun onMapLongTap(map: Map, point: Point) {
            viewModel.onEvent(MapView.Event.OnLongClickMap(point))
        }
    }

    private fun initViewModel() {
        with(viewModel) {
            subscribe(uiLabels, ::handleUiLabel)
        }
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.fragment_map_on_geo))
        builder.setMessage(getString(R.string.fragment_map_geo_text))

        builder.setPositiveButton(getString(R.string.fragment_map_geo_settings)) { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        builder.setNegativeButton(getString(R.string.fragment_map_geo_cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    @SuppressLint("StringFormatMatches")
    private fun showDialogRoute(point: Point, namePoint: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle(
            getString(
                R.string.fragment_map_names, namePoint, point.latitude, point.longitude
            )
        )
        dialogBuilder.setMessage(getString(R.string.fragment_map_question))

        dialogBuilder.setPositiveButton(getString(R.string.fragment_map_yes)) { _, _ ->
            viewModel.onEvent(MapView.Event.OnClickDrivingRoute(point))
        }

        dialogBuilder.setNegativeButton(getString(R.string.fragment_map_no)) { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton(getString(R.string.fragment_map_delete_poits)) { _, _ ->
            viewModel.onEvent(MapView.Event.OnClickDeletePoint(point))
        }
        dialogBuilder.create().show()
    }

    private fun showDialogCreateName(point: Point) {
        val editText = EditText(requireContext()).apply {
            hint = context.getString(R.string.fragment_map_create_name_hint)
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle(getString(R.string.fragment_map_name_point))
        dialogBuilder.setMessage(getString(R.string.fragment_map_question_nme))

        dialogBuilder.setView(editText)

        dialogBuilder.setNegativeButton(R.string.fragment_map_no) { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.setPositiveButton(R.string.fragment_map_yes) { _, _ ->
            val pointName = editText.text.toString()
            viewModel.onEvent(MapView.Event.AddNamePoint(point, pointName))
        }

        dialogBuilder.create().show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        initViewModel()
        map = binding.mapview.mapWindow.map

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                map.mapObjects.clear()

                state.point.forEach { domainPoint ->
                    createPlaceMark(
                        Point(domainPoint.latitude, domainPoint.longitude), domainPoint.name
                    )
                }

                if (!isCameraMoved && viewModel.currentCoordinate != null) {
                    val cameraPosition = CameraPosition(
                        viewModel.currentCoordinate ?: Point(), 15f, 0f, 0f
                    )

                    map.move(cameraPosition)
                    isCameraMoved = true
                }
            }
        }

        map.addInputListener(inputListener)

        return binding.root
    }


    private fun createPlaceMark(point: Point, pointName: String = "") {
        placeMarkMapObject = map.mapObjects.addPlacemark().apply {
            setText(pointName)
            setIcon(ImageProvider.fromResource(requireActivity(), R.drawable.map),
                IconStyle().apply {
                    anchor = PointF(0.5f, 1f)
                    scale = 0.09f
                })
            geometry = point
            isDraggable = true
            addTapListener(placeMarkTapListener)
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
            routesCount = 1
        }

        val vehicleOptions = VehicleOptions()

        val points = buildList {
            add(RequestPoint(startPoint, RequestPointType.WAYPOINT, null, ""))
            add(RequestPoint(endPoint, RequestPointType.WAYPOINT, null, ""))
        }
        //map.mapObjects.clear()
        drivingRouter.requestRoutes(points,
            drivingOptions,
            vehicleOptions,
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
                    if (drivingRoutes.isNotEmpty()) {
                        drivingRoutes.map {
                            val polyline = map.mapObjects.addPolyline(it.geometry)
                            polyline.setStrokeColor(Color.GRAY)
                            polyline.strokeWidth = 10f
                        }
                    }
                }

                override fun onDrivingRoutesError(p0: com.yandex.runtime.Error) = Unit
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
}