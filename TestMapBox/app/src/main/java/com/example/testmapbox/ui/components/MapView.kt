package com.example.testmapbox.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testmapbox.ui.screen.MapViewCallback
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean,
    onMapReady: (MapViewCallback) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }

    val indicatorListener = remember {
        OnIndicatorPositionChangedListener { point ->
            mapboxMap?.easeTo(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build(),
                MapAnimationOptions.mapAnimationOptions {
                    duration(1000L)
                }
            )
        }
    }

    LaunchedEffect(hasLocationPermission, mapView) {
        if (hasLocationPermission && mapView != null) {
            val locationPlugin = mapView!!.location
            locationPlugin.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            locationPlugin.addOnIndicatorPositionChangedListener(indicatorListener)
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView?.location?.removeOnIndicatorPositionChangedListener(indicatorListener)
        }
    }

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                mapView = this

                getMapboxMap().apply {
                    mapboxMap = this
                    loadStyleUri(com.mapbox.maps.Style.MAPBOX_STREETS)
                    setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(105.8342, 21.0278))
                            .zoom(12.0)
                            .build()
                    )
                }

                gestures.apply {
                    pinchToZoomEnabled = true
                    rotateEnabled = true
                    scrollEnabled = true
                    doubleTapToZoomInEnabled = true
                    doubleTouchToZoomOutEnabled = true
                    quickZoomEnabled = true
                }

                // Tạo callback
                val callback = object : MapViewCallback {
                    override fun animateToLocation(longitude: Double, latitude: Double) {
                        mapboxMap?.easeTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(longitude, latitude))
                                .zoom(14.0)
                                .build(),
                            MapAnimationOptions.mapAnimationOptions {
                                duration(2000L)
                            }
                        )
                    }

                    override fun addMarker(longitude: Double, latitude: Double) {
                        annotations.let { annotationApi ->
                            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
                            pointAnnotationManager.deleteAll()

                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(Point.fromLngLat(longitude, latitude))
                                .withIconImage("marker")

                            pointAnnotationManager.create(pointAnnotationOptions)
                        }
                    }

                    override fun changeMapStyle(style: String) {
                        mapboxMap?.loadStyleUri(style)
                    }

                    override fun zoomIn() {
                        mapboxMap?.let { map ->
                            val currentZoom = map.cameraState.zoom
                            map.setCamera(
                                CameraOptions.Builder()
                                    .zoom(currentZoom + 1)
                                    .build()
                            )
                        }
                    }

                    override fun zoomOut() {
                        mapboxMap?.let { map ->
                            val currentZoom = map.cameraState.zoom
                            map.setCamera(
                                CameraOptions.Builder()
                                    .zoom(currentZoom - 1)
                                    .build()
                            )
                        }
                    }

                    override fun resetView() {
                        mapView?.location?.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                        }
                    }
                }

                onMapReady(callback)
            }
        },
        modifier = modifier
    )
}