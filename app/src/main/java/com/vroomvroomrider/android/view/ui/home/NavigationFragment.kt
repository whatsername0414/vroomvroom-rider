package com.vroomvroomrider.android.view.ui.home

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.building.api.MapboxBuildingsApi
import com.mapbox.navigation.ui.maps.building.model.BuildingError
import com.mapbox.navigation.ui.maps.building.model.BuildingValue
import com.mapbox.navigation.ui.maps.building.view.MapboxBuildingView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.ncorti.slidetoact.SlideToActView
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.data.model.order.StatusEnum
import com.vroomvroomrider.android.data.model.user.UserEntity
import com.vroomvroomrider.android.databinding.FragmentNavigationBinding
import com.vroomvroomrider.android.utils.Utils.createLocationRequest
import com.vroomvroomrider.android.utils.Utils.hasLocationPermission
import com.vroomvroomrider.android.utils.Utils.requestLocationPermission
import com.vroomvroomrider.android.utils.Utils.safeNavigate
import com.vroomvroomrider.android.view.resource.Resource
import com.vroomvroomrider.android.view.ui.base.BaseFragment
import com.vroomvroomrider.android.view.ui.common.ClickType
import com.vroomvroomrider.android.view.ui.common.CompleteType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NavigationFragment : BaseFragment<FragmentNavigationBinding>(
    FragmentNavigationBinding::inflate
), EasyPermissions.PermissionCallbacks {

    private val args: NavigationFragmentArgs by navArgs()
    private var order: Order? = null
    private lateinit var user: UserEntity

    private val onSlideCompleteListener = object: SlideToActView.OnSlideCompleteListener {
        override fun onSlideComplete(view: SlideToActView) {
            if (order?.status?.label.orEmpty() == StatusEnum.ARRIVED.label) {
                orderViewModel.deliveredOrder(args.order.id)
            } else {
                orderViewModel.arrived(args.order.id)
            }
        }
    }

    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(requireContext())
                    .accessToken(getString(R.string.mapbox_access_token))
                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }
    }
    private val navigationLocationProvider by lazy { NavigationLocationProvider() }
    private val options by lazy {
        MapboxRouteLineOptions.Builder(requireContext())
            .waypointLayerIconAnchor(IconAnchor.CENTER)
            .withRouteLineBelowLayerId("road-label")
            .build()
    }

    private val tripProgressFormatter by lazy {
        val distanceFormatterOptions =
            DistanceFormatterOptions.Builder(requireContext()).build()
        TripProgressUpdateFormatter.Builder(requireContext())
            .distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatterOptions))
            .timeRemainingFormatter(TimeRemainingFormatter(requireContext()))
            .estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(requireContext()))
            .build()
    }
    private val tripProgressApi: MapboxTripProgressApi by lazy {
        MapboxTripProgressApi(tripProgressFormatter)
    }

    private val mapboxReplayer = MapboxReplayer()
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    private val routeLineView by lazy { MapboxRouteLineView(options) }
    private val routeLineApi by lazy { MapboxRouteLineApi(options) }
    private val routeArrowApi by lazy { MapboxRouteArrowApi() }
    private val buildingView by lazy { MapboxBuildingView() }
    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(requireContext())
            .withAboveLayerId(RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID)
            .build()
    }
    private val buildingsApi by lazy { MapboxBuildingsApi(mapboxMap) }
    private val routeArrowView by lazy { MapboxRouteArrowView(routeArrowOptions) }

    private lateinit var mapboxMap: MapboxMap
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var routeOptions: RouteOptions
    private lateinit var buildingApi: MapboxBuildingsApi
    private var percentDistanceTraveledThreshold: Double = 95.0
    private var distanceRemainingThresholdInMeters = 30
    private var arrivalNotificationHasDisplayed = false
    private val routerCallback = object : NavigationRouterCallback {

        override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: RouterOrigin) {
            mapboxNavigation.setNavigationRoutes(routes)
            routeLineApi.setNavigationRoutes(routes) { value ->
                mapboxMap.getStyle()?.let { routeLineView.renderRouteDrawData(it, value) }
            }
            initBottomSheet()
            mapboxNavigation.startTripSession()
            startSimulation(routes.first().directionsRoute)
        }

        override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
        }

        override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
        }
    }

    private val callback =
        MapboxNavigationConsumer<Expected<BuildingError, BuildingValue>> { expected ->
            expected.fold(
                {
                    Log.e(
                        "ShowBuildingExtrusionsActivity",
                        "error: ${it.errorMessage}"
                    )
                },
                { value ->
                    mapboxMap.getStyle { style ->
                        buildingView.highlightBuilding(style, value.buildings)
                    }
                }
            )
        }

    private val routeProgressObserver = RouteProgressObserver { progress ->
        val tripProgress = tripProgressApi.getTripProgress(progress)
        binding.navigationBottomSheet.tripProgressView.render(tripProgress)
        viewportDataSource.onRouteProgressChanged(progress)
        viewportDataSource.evaluate()
        val updatedManeuverArrow = routeArrowApi.addUpcomingManeuverArrow(progress)
        mapboxMap.getStyle()?.let { routeArrowView.renderManeuverUpdate(it, updatedManeuverArrow) }
        val totalDistance = progress.distanceTraveled + progress.distanceRemaining
        val percentDistanceTraveled = (progress.distanceTraveled / totalDistance) * 100
        if (
            percentDistanceTraveled >= percentDistanceTraveledThreshold &&
            progress.distanceRemaining <= distanceRemainingThresholdInMeters &&
            !arrivalNotificationHasDisplayed
        ) {
            arrivalNotificationHasDisplayed = true
            buildingApi.queryBuildingOnFinalDestination(progress, callback)
            mapboxNavigation.stopTripSession()
        }
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            updateCamera(
                Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude),
                enhancedLocation.bearing.toDouble())
        }
    }

    override fun onStart() {
        super.onStart()
        mapboxNavigation.run {
            requestRoutes(routeOptions, routerCallback)
            registerLocationObserver(locationObserver)
            registerRouteProgressObserver(routeProgressObserver)
            registerRouteProgressObserver(replayProgressObserver)
        }

        userViewModel.user.observe(viewLifecycleOwner){
            user = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.order.apply {
            binding.navigationBottomSheet.customerInfoLayout.order = this
            val origin = "${merchant.location[1]},${merchant.location[0]}"
            val destination = "${deliveryAddress.coordinates[1]},${deliveryAddress.coordinates[0]}"
            routeOptions = getRouteOptions(merchant.name, customer.name, origin, destination)
            updateCamera(
                Point.fromLngLat(merchant.location[1], merchant.location[0]),
                null)
        }
        mapboxMap = binding.mapView.getMapboxMap()
        buildingApi = MapboxBuildingsApi(mapboxMap)
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            binding.mapView.camera,
            viewportDataSource
        )
        if (hasLocationPermission(requireContext())) {
            createLocationRequest(requireActivity()) {
                mapboxNavigation.startTripSession()
                initListeners()
            }
        } else {
            requestLocationPermission(this)
        }
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        observeOrder()
        mapViewPadding()
        initLocationProvider()

        binding.sliderLayout.apply {
            pickOrderSlide.text = getString(R.string.arrived)
            pickOrderSlide.onSlideCompleteListener = onSlideCompleteListener
        }
    }

    private fun mapViewPadding() {
        val pixelDensity = context?.resources?.displayMetrics?.density ?: 0f
        viewportDataSource.followingPadding = EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
        viewportDataSource.overviewPadding = EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private fun initLocationProvider() {
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            enabled = true
        }
    }

    private fun initBottomSheet() {
        BottomSheetBehavior.from(binding.navigationBottomSheet.root).apply {
            peekHeight = binding.appBarLayout.height * 2
            state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            binding.navigationBottomSheet
                                .customerInfoLayout.root.visibility = View.GONE
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            binding.navigationBottomSheet
                                .customerInfoLayout.root.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    private fun observeOrder() {
        orderViewModel.order.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    order = response.data
                    if (response.data.status.label == StatusEnum.ARRIVED.label) {
                        binding.sliderLayout.pickOrderSlide.apply {
                            text = getString(R.string.delivered)
                            resetSlider()
                        }
                    } else {
                        userViewModel.updatePickedOrder(user.id, null)
                        findNavController().safeNavigate(NavigationFragmentDirections
                            .actionNavigationFragmentToCommonCompleteFragment(
                                getString(R.string.delivered_toolbar_title),
                                R.drawable.ic_package_delivered,
                                getString(R.string.delivery_completed),
                                getString(R.string.delivery_completed_desc),
                                getString(R.string.back_to_home),
                                CompleteType.DELIVERED
                            ))
                    }
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.network_error),
                        response.exception.message ?: getString(R.string.general_error_message),
                        getString(R.string.cancel),
                        getString(R.string.ok),
                        isButtonLeftVisible = false,
                        isCancellable = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> dialog.dismiss()
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest(requireActivity()) {
                initListeners()
            }
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}

    private fun initListeners() {
        mapboxMap.getStyle()?.apply {
            routeLineView.hideAlternativeRoutes(this)
        }
    }

    //Use this to simulate route progress
    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            pushRealLocation(requireContext(), 0.0)
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    private fun updateCamera(point: Point, bearing: Double?) {
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .pitch(PITCH)
                .zoom(ZOOM)
                .padding(EdgeInsets(INSET_TOP, INSET_LEFT, INSET_BOTTOM, INSET_RIGHT))
                .build(),
            mapAnimationOptionsBuilder.build()
        )
    }

    private fun getRouteOptions(
        merchant: String,
        customer: String,
        origin: String,
        destination: String
    ): RouteOptions {
        return RouteOptions.builder()
            .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
            .annotationsList(
                listOf(
                    DirectionsCriteria.ANNOTATION_CONGESTION_NUMERIC,
                    DirectionsCriteria.ANNOTATION_DISTANCE
                )
            )
            .steps(true)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .waypointNamesList(listOf(merchant, customer))
            .coordinates("$origin;$destination")
            .build()
    }

    private fun unregisterObservers() {
        mapboxNavigation.run {
            unregisterLocationObserver(locationObserver)
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterRouteProgressObserver(replayProgressObserver)
            stopTripSession()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapboxReplayer.finish()
        routeLineApi.cancel()
        routeLineView.cancel()
        buildingsApi.cancel()
        unregisterObservers()
        MapboxNavigationProvider.destroy()
    }

    companion object {
        const val PITCH = 45.0
        const val ZOOM = 17.0
        const val INSET_TOP = 800.0
        const val INSET_BOTTOM = 200.0
        const val INSET_LEFT = 0.0
        const val INSET_RIGHT = 0.0
    }

}