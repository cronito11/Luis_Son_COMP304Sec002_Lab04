package com.example.luis_son_comp304sec002_lab04

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.luis_son_comp304sec002_lab04.ui.theme.MapAttractionsTheme
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.location.Location
import android.os.Looper
import androidx.compose.ui.draw.shadow
import com.google.android.gms.location.LocationServices

data class RouteStep(
    val instruction: String,
    val distance: String,
    val duration: String
)

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = mutableListOf<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int; var shift = 0; var result = 0
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lat += if ((result and 1) != 0) (result shr 1).inv() else result shr 1
        shift = 0; result = 0
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lng += if ((result and 1) != 0) (result shr 1).inv() else result shr 1
        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return poly
}

fun modeToOsrmProfile(mode: String) = when (mode) {
    "walking" -> "foot"
    "cycling" -> "bike"
    else      -> "car"
}

fun osrmManeuverToText(type: String, modifier: String?): String {
    return when (type) {
        "depart"            -> "Head ${modifier ?: ""}"
        "arrive"            -> "You have arrived"
        "turn"              -> "Turn ${modifier ?: ""}"
        "new name"          -> "Continue ${modifier ?: "straight"}"
        "merge"             -> "Merge ${modifier ?: ""}"
        "on ramp"           -> "Take the ramp ${modifier ?: ""}"
        "off ramp"          -> "Take the exit ${modifier ?: ""}"
        "fork"              -> "Keep ${modifier ?: "straight"} at the fork"
        "end of road"       -> "Turn ${modifier ?: ""} at the end of the road"
        "roundabout",
        "rotary"            -> "Enter the roundabout"
        "roundabout turn"   -> "At the roundabout, turn ${modifier ?: ""}"
        "continue"          -> "Continue ${modifier ?: "straight"}"
        "use lane"          -> "Use the ${modifier ?: ""} lane"
        else                -> type.replaceFirstChar { it.uppercase() }
    }
}

fun formatDistance(meters: Double): String = when {
    meters >= 1000 -> "${"%.1f".format(meters / 1000)} km"
    else           -> "${meters.toInt()} m"
}

fun formatDuration(seconds: Double): String {
    val mins = (seconds / 60).toInt()
    return if (mins < 1) "< 1 min" else "$mins min"
}

suspend fun fetchDirections(
    origin: LatLng,
    destination: LatLng,
    mode: String
): Pair<List<LatLng>, List<RouteStep>> = withContext(Dispatchers.IO) {
    try {
        val profile = modeToOsrmProfile(mode)

        val url = "https://router.project-osrm.org/route/v1/$profile/" +
                "${origin.longitude},${origin.latitude};" +
                "${destination.longitude},${destination.latitude}" +
                "?steps=true&overview=full&geometries=polyline&annotations=false"

        val response = URL(url).readText()
        val json    = JSONObject(response)

        if (json.getString("code") != "Ok") return@withContext Pair(emptyList(), emptyList<RouteStep>())

        val route    = json.getJSONArray("routes").getJSONObject(0)
        val geometry = route.getString("geometry")
        val points   = decodePolyline(geometry)

        val legs  = route.getJSONArray("legs").getJSONObject(0)
        val steps = mutableListOf<RouteStep>()

        val stepsArray = legs.getJSONArray("steps")
        for (i in 0 until stepsArray.length()) {
            val step      = stepsArray.getJSONObject(i)
            val maneuver  = step.getJSONObject("maneuver")
            val type      = maneuver.getString("type")
            val modifier  = if (maneuver.has("modifier")) maneuver.getString("modifier") else null
            val streetName = if (step.has("name") && step.getString("name").isNotBlank())
                " onto ${step.getString("name")}" else ""
            val instruction = osrmManeuverToText(type, modifier) + streetName
            val dist = formatDistance(step.getDouble("distance"))
            val dur  = formatDuration(step.getDouble("duration"))
            steps.add(RouteStep(instruction, dist, dur))
        }

        Pair(points, steps)

    } catch (e: Exception) {
        Pair(emptyList(), emptyList<RouteStep>())
    }
}
class MainActivity3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedItem = intent.getStringExtra("selectedItem") ?: "Unknown Item"
        setContent {
            MapAttractionsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MapScreen(selectedItem)
                }
            }
        }
    }
}

@Composable
fun MapScreen(selectedItem: String) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasLocationPermission = isGranted }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (hasLocationPermission) {
        MyGoogleMap(selectedItem)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Location permission required",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8B84B))
                ) {
                    Text("Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGoogleMap(selectedItem: String) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()


    val itemCoordinates = mapOf(
        "Rebel" to LatLng(43.6418, -79.3541),
        "Lost & Found" to LatLng(43.6445, -79.3984),
        "Whiskey A Go-Go" to LatLng(43.8104, -79.4937),
        "Club Lux" to LatLng(43.6486, -79.3876),
        "Woodbine Beach" to LatLng(43.6677, -79.3016),
        "Cherry Beach" to LatLng(43.6394, -79.3460),
        "Kew-Balmy Beach" to LatLng(43.6671, -79.2967),
        "Bluffer's Park Beach" to LatLng(43.7163, -79.2312),
        "High Park" to LatLng(43.6465, -79.4637),
        "Tommy Thompson Park" to LatLng(43.6358, -79.3184),
        "Trinity Bellwoods Park" to LatLng(43.6479, -79.4176),
        "Rouge National Urban Park" to LatLng(43.8065, -79.1867),
        "Woodbine Casino" to LatLng(43.7168, -79.5982),
        "Canada's Wonderland" to LatLng(43.8430, -79.5390),
        "K1 Speed Toronto" to LatLng(43.7471, -79.4828),
        "Drinks" to LatLng(43.6435, -79.39387)
    )

    val destinationLatLng = itemCoordinates[selectedItem] ?: LatLng(43.651070, -79.347015)

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isNear by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeSteps by remember { mutableStateOf<List<RouteStep>>(emptyList()) }
    var selectedMode by remember { mutableStateOf("driving") }
    var isLoadingRoute by remember { mutableStateOf(false) }
    var showDirectionsPanel by remember { mutableStateOf(false) }
    var routeSummary by remember { mutableStateOf("") }
    var tappedLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentMapType by remember { mutableStateOf(MapType.NORMAL) }

    val geofenceRadius = 200f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destinationLatLng, 13f)
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    userLocation = LatLng(loc.latitude, loc.longitude)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { user ->
            val results = FloatArray(1)
            Location.distanceBetween(
                user.latitude, user.longitude,
                destinationLatLng.latitude, destinationLatLng.longitude,
                results
            )
            isNear = results[0] < geofenceRadius
        }
    }

    LaunchedEffect(userLocation, selectedMode) {
        val user = userLocation ?: return@LaunchedEffect
        isLoadingRoute = true
        scope.launch {
            val (points, steps) = fetchDirections(user, destinationLatLng, selectedMode)
            routePoints = points
            routeSteps = steps
            isLoadingRoute = false
            if (steps.isNotEmpty()) {
                routeSummary = "${steps.size} steps · ${steps.sumOf { step ->
                    step.duration.replace(" mins", "").replace(" min", "")
                        .trim().toIntOrNull() ?: 0
                }} min"
            }
        }
    }

    val mapStyleJson = """
        [
          {"featureType":"poi","stylers":[{"visibility":"off"}]},
          {"featureType":"transit","stylers":[{"visibility":"off"}]}
        ]
    """.trimIndent()

    val mapProperties by remember(currentMapType) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,
                mapType = currentMapType,
                mapStyleOptions = MapStyleOptions(mapStyleJson)
            )
        )
    }

    val uiSettings = remember {
        MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
    }

    val routeColor = when (selectedMode) {
        "walking" -> Color(0xFF4CAF50)
        "transit" -> Color(0xFF2196F3)
        else -> Color(0xFFE8B84B)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedItem,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        if (routeSummary.isNotEmpty()) {
                            Text(
                                text = routeSummary,
                                fontSize = 12.sp,
                                color = Color(0xFFE8B84B)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D0D))
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapClick = { latLng -> tappedLocation = latLng; showDirectionsPanel = false },
                onMapLongClick = { latLng -> tappedLocation = latLng }
            ) {
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "You are here",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }

                Marker(
                    state = MarkerState(position = destinationLatLng),
                    title = selectedItem,
                    snippet = "Tap for details",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    onClick = { showDirectionsPanel = true; false }
                )

                Circle(
                    center = destinationLatLng,
                    radius = geofenceRadius.toDouble(),
                    strokeColor = Color(0xFFE8B84B),
                    strokeWidth = 2f,
                    fillColor = Color(0xFFE8B84B).copy(alpha = 0.15f)
                )

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = routeColor,
                        width = 10f,
                        geodesic = true
                    )
                } else {
                    userLocation?.let {
                        Polyline(
                            points = listOf(it, destinationLatLng),
                            color = routeColor.copy(alpha = 0.4f),
                            width = 6f,
                            pattern = listOf(Dot(), Gap(15f))
                        )
                    }
                }

                tappedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Pinned",
                        snippet = "${it.latitude.format(4)}, ${it.longitude.format(4)}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                    )
                }
            }

            AnimatedVisibility(
                visible = isNear,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8B84B))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "You've arrived at $selectedItem!",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (isLoadingRoute) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFE8B84B), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Getting route...", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = if (showDirectionsPanel) 280.dp else 100.dp)
            ) {
                val modes = listOf(
                    "driving" to "Drive",
                    "walking" to "Walk",
                    "transit" to "Transit"
                )
                modes.forEach { (mode, label) ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .shadow(6.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFE8B84B) else Color(0xFF1C1C1E))
                            .clickable { selectedMode = mode }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (showDirectionsPanel) 280.dp else 100.dp),
                horizontalAlignment = Alignment.End
            ) {
                val types = listOf(MapType.NORMAL to "Map", MapType.SATELLITE to "Sat", MapType.HYBRID to "Hyb")
                types.forEach { (type, label) ->
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .shadow(6.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentMapType == type) Color(0xFFE8B84B) else Color(0xFF1C1C1E))
                            .clickable { currentMapType = type }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentMapType == type) Color.Black else Color.White
                        )
                    }
                }
            }

            if (routeSteps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showDirectionsPanel) 280.dp else 16.dp)
                        .shadow(10.dp, RoundedCornerShape(50.dp))
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF1C1C1E))
                        .clickable { showDirectionsPanel = !showDirectionsPanel }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (showDirectionsPanel) "▼  Hide Directions" else "▲  Show Directions (${routeSteps.size} steps)",
                        color = Color(0xFFE8B84B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = showDirectionsPanel && routeSteps.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF0D0D0D)))
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Turn-by-Turn Directions",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            val modeLabel = selectedMode.replaceFirstChar { it.uppercase() }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8B84B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    modeLabel,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(routeSteps.size) { i ->
                                val step = routeSteps[i]
                                Box(
                                    modifier = Modifier
                                        .width(240.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE8B84B)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "${i + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Row {
                                                Text(
                                                    step.distance,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFE8B84B),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    " · ${step.duration}",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            step.instruction,
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            lineHeight = 18.sp,
                                            maxLines = 3
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)