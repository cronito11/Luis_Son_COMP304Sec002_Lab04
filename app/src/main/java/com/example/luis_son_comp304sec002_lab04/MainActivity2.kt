package com.example.luis_son_comp304sec002_lab04

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luis_son_comp304sec002_lab04.ui.theme.MapAttractionsTheme

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapAttractionsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val category = intent.getStringExtra("category") ?: "Unknown"
                    ItemListScreen(category = category)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(category: String) {
    val context = LocalContext.current

    val items = when (category) {
        "Clubs" -> listOf("Rebel", "Lost & Found", "Whiskey A Go-Go", "Club Lux")
        "Beaches" -> listOf("Woodbine Beach", "Cherry Beach", "Kew-Balmy Beach", "Bluffer's Park Beach")
        "Parks" -> listOf("High Park", "Tommy Thompson Park", "Trinity Bellwoods Park", "Rouge National Urban Park")
        "Fun Activities" -> listOf("Woodbine Casino", "Canada's Wonderland", "K1 Speed Toronto", "Drinks")
        else -> listOf()
    }

    val categoryIcon = when (category) {
        "Clubs" -> "Nightlife"
        "Beaches" -> "Waterfront"
        "Parks" -> "Nature"
        "Fun Activities" -> "Adventure"
        else -> "Explore"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0D0D), Color(0xFF1A1A2E))
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { (context as? Activity)?.onBackPressed() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "EXPLORE",
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                            color = Color(0xFFE8B84B),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = category,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${items.size} LOCATIONS NEARBY",
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(items.size) { index ->
                val item = items[index]
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = index * 80)) +
                            slideInVertically(tween(400, delayMillis = index * 80)) { it / 4 }
                ) {
                    AttractionCard(
                        item = item,
                        index = index + 1,
                        onCardClick = { selectedItem -> navigateToMap(selectedItem, context) },
                        category = category
                    )
                }
            }
        }
    }
}

private fun navigateToMap(item: String, context: Context) {
    val intent = Intent(context, MainActivity3::class.java).apply {
        putExtra("selectedItem", item)
    }
    context.startActivity(intent)
}

@Composable
fun AttractionCard(item: String, index: Int, onCardClick: (String) -> Unit, category: String) {
    val backgroundImage = when (category) {
        "Clubs" -> when (item) {
            "Rebel" -> painterResource(id = R.drawable.rebel)
            "Lost & Found" -> painterResource(id = R.drawable.lostandfound)
            "Whiskey A Go-Go" -> painterResource(id = R.drawable.gogo)
            "Club Lux" -> painterResource(id = R.drawable.lux)
            else -> painterResource(id = R.drawable.rebel)
        }
        "Beaches" -> when (item) {
            "Woodbine Beach" -> painterResource(id = R.drawable.woodbine)
            "Cherry Beach" -> painterResource(id = R.drawable.cherry)
            "Kew-Balmy Beach" -> painterResource(id = R.drawable.kew)
            "Bluffer's Park Beach" -> painterResource(id = R.drawable.bluffers)
            else -> painterResource(id = R.drawable.beach)
        }
        "Parks" -> when (item) {
            "High Park" -> painterResource(id = R.drawable.highpark)
            "Tommy Thompson Park" -> painterResource(id = R.drawable.tommy)
            "Trinity Bellwoods Park" -> painterResource(id = R.drawable.trinity)
            "Rouge National Urban Park" -> painterResource(id = R.drawable.rogue)
            else -> painterResource(id = R.drawable.parks)
        }
        "Fun Activities" -> when (item) {
            "Woodbine Casino" -> painterResource(id = R.drawable.casino1)
            "Canada's Wonderland" -> painterResource(id = R.drawable.wonderland)
            "Drinks" -> painterResource(id = R.drawable.lcbo)
            "K1 Speed Toronto" -> painterResource(id = R.drawable.k1)
            else -> painterResource(id = R.drawable.noimg)
        }
        else -> painterResource(id = R.drawable.noimg)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick(item) }
    ) {
        Image(
            painter = backgroundImage,
            contentDescription = item,
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .padding(14.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8B84B))
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .padding(14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .align(Alignment.TopEnd)
        ) {
            Text(
                text = "View on map →",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = item,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Tap to get directions",
                fontSize = 12.sp,
                color = Color(0xFFE8B84B).copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}