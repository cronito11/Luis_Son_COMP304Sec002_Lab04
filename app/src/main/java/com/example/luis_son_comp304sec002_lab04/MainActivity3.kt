package com.example.luis_son_comp304sec002_lab04

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.luis_son_comp304sec002_lab04.ui.theme.MapAttractionsTheme
import com.google.maps.android.compose.rememberCameraPositionState



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
    val activity = LocalContext.current as? ComponentActivity
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
    val location = itemCoordinates[selectedItem] ?: LatLng(0.0, 0.0)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 12f)  // Adjust zoom level as needed
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
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = com.google.maps.android.compose.MarkerState(position = location),
                title = selectedItem,
                snippet = "Location of $selectedItem"
            )
        }
    }
}
