package com.example.luis_son_comp304sec002_lab04

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.example.luis_son_comp304sec002_lab04.ui.theme.MapAttractionsTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.location.LocationServices
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import android.os.Looper
import androidx.compose.material3.Button
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import com.google.android.gms.location.*

class MainActivity3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedItem = intent.getStringExtra("selectedItem") ?: "Unknown Item"

        setContent {
            MapAttractionsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(selectedItem)
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(selectedItem: String) {
    val context = LocalContext.current;

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    // Check permission on start
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    if (hasLocationPermission) {
        MyGoogleMap(selectedItem);
    }else {
        Text("Location permission required")
    }
}


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGoogleMap(selectedItem: String) {
    val context = LocalContext.current;
    val activity = context as? ComponentActivity;

    val itemCoordinates = mapOf(
        // Clubs
        "Rebel" to LatLng(43.6418, -79.3541),
        "Lost & Found" to LatLng(43.6445, -79.3984),
        "Whiskey A Go-Go" to LatLng(43.8104, -79.4937),
        "Club Lux" to LatLng(43.6486, -79.3876),

        // Beaches
        "Woodbine Beach" to LatLng(43.6677, -79.3016),
        "Cherry Beach" to LatLng(43.6394, -79.3460),
        "Kew-Balmy Beach" to LatLng(43.6671, -79.2967),
        "Bluffer’s Park Beach" to LatLng(43.7163, -79.2312),


        // Parks
        "High Park" to LatLng(43.6465, -79.4637),
        "Tommy Thompson Park" to LatLng(43.6358, -79.3184),
        "Trinity Bellwoods Park" to LatLng(43.6479, -79.4176),
        "Rouge National Urban Park" to LatLng(43.8065, -79.1867),

        // Fun Activities
        "Woodbine Casino" to LatLng(43.7168, -79.5982),
        "Canada's Wonderland" to LatLng(43.8430, -79.5390),
        "K1 Speed Toronto" to LatLng(43.7471, -79.4828),
        "Drinks" to LatLng(43.6435, -79.39387)
    )
    // Default to a fallback location if the item is unknown
    var isNear by remember { mutableStateOf(false) }

    val geofenceRadius = 200f
    val location = itemCoordinates[selectedItem] ?: LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 13f)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                userLocation = LatLng(location.latitude, location.longitude)
            }
        }
    }

    DisposableEffect(Unit) {

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        ).build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(userLocation, location) {
        userLocation?.let { user ->

            val results = FloatArray(1)

            Location.distanceBetween(
                user.latitude,
                user.longitude,
                location.latitude,
                location.longitude,
                results
            )

            isNear = results[0] < geofenceRadius
        }
    }
    /*LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(it, 15f)
        }
    }*/

    var currentMapType by remember {
        mutableStateOf(MapType.NORMAL) // Initial map type
    }
    //D. Map Properties: isMyLocationEnabled shows the blue dot
    val mapProperties by remember(currentMapType) { // Recompose when currentMapType changes
        mutableStateOf(MapProperties(
            isMyLocationEnabled = true, // Shows the blue dot
            mapType = currentMapType,
            ))
    }

    // E. Map UI Settings: myLocationButtonEnabled adds the "target" icon to the top right
    val uiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = true
        )
    }

    //    new code
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Map for $selectedItem") },
                navigationIcon = {
                    IconButton(onClick = { activity?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu_revert),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {
            if (isNear) {
                Text(
                    text = "You are near $selectedItem!",
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
            GoogleMap(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(paddingValues),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {

                // Optional: Add a marker at the default position or elsewhere
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "You",
                        snippet = "Your current location"
                    )
                }

                Marker(
                    state = MarkerState(position = location),
                    title = selectedItem,
                    snippet = "Location of $selectedItem"
                )

                Circle(
                    center = location,
                    radius = geofenceRadius.toDouble(), // meters
                    strokeColor = Color.Red,
                    fillColor = Color.Red.copy(alpha = 0.2f)
                )
            }
            MapTypeControls(
                currentMapType = currentMapType,
                onMapTypeSelected = { newMapType ->
                    currentMapType = newMapType
                }
            )

        }
    }
}


@Composable
fun MapTypeControls(
    currentMapType: MapType,
    onMapTypeSelected: (MapType) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapTypes = listOf(
        MapType.NORMAL,
        MapType.SATELLITE,
        MapType.TERRAIN,
        MapType.HYBRID,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Map Type:")
        Spacer(modifier = Modifier.height(8.dp))
        // Example using Buttons in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            mapTypes.forEach { mapType ->
                Button(
                    onClick = { onMapTypeSelected(mapType) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    enabled = mapType != currentMapType // Disable button for current type
                ) {
                    Text(mapType.toString())
                }
            }
        }

        // Alternative using RadioButtons
        // mapTypes.forEach { mapType ->
        //     Row(
        //         verticalAlignment = Alignment.CenterVertically,
        //         modifier = Modifier
        //             .fillMaxWidth()
        //             .padding(vertical = 4.dp)
        //             .clickable { onMapTypeSelected(mapType) }
        //     ) {
        //         RadioButton(
        //             selected = (mapType == currentMapType),
        //             onClick = { onMapTypeSelected(mapType) }
        //         )
        //         Text(
        //             text = mapType.toString(),
        //             modifier = Modifier.padding(start = 8.dp)
        //         )
        //     }
        // }
    }
}
