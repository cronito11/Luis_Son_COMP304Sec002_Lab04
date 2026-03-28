package com.example.luis_son_comp304sec002_lab04
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
        "Beaches" -> listOf("Woodbine Beach", "Cherry Beach", "Kew-Balmy Beach", "Bluffer’s Park Beach")
        "Parks" -> listOf("High Park", "Tommy Thompson Park", "Trinity Bellwoods Park", "Rouge National Urban Park")
        "Fun Activities" -> listOf("Woodbine Casino", "Canada's Wonderland", "K1 Speed Toronto", "Drinks")
        else -> listOf()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text("Explore $category", fontSize = 20.sp)
            },
            navigationIcon = {
                IconButton(onClick = {
                    (context as? Activity)?.onBackPressed()
                }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items.size) { index ->
                CategoryCard(
                    item = items[index],
                    onCardClick = { selectedItem ->
                        navigateToMap(selectedItem, context)
                    },
                    category = category
                )
            }
        }
    }
}

private fun navigateToMap(item: String, context: Context) {
    val intent = Intent(context, MainActivity3::class.java).apply {
        putExtra("selectedItem", item) // Pass the selected item to the map activity
    }
    context.startActivity(intent)
}
@Composable
fun CategoryCard(item: String, onCardClick: (String) -> Unit, category: String) {
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
            "Bluffer’s Park Beach" -> painterResource(id = R.drawable.bluffers)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onCardClick(item) }
            .padding(8.dp)
            .shadow(8.dp),
        shape = MaterialTheme.shapes.medium,

        ) {
        Box {
            Image(
                painter = backgroundImage,
                contentDescription = item,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.7f)
            )

            Text(
                text = item,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 35.sp, color = Color.White, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MapAttractionsTheme {
        ItemListScreen(category = "Attractions")
    }
}
