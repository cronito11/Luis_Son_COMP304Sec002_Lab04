package com.example.luis_son_comp304sec002_lab04

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luis_son_comp304sec002_lab04.ui.theme.MapAttractionsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapAttractionsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CategoryList(onCategoryClick = { category ->
                        val intent = Intent(this, MainActivity2::class.java).apply {
                            putExtra("category", category)
                        }
                        startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun CategoryList(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        "Clubs" to R.drawable.rebel,
        "Beaches" to R.drawable.beach,
        "Parks" to R.drawable.parks,
        "Fun Activities" to R.drawable.k1
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Heading
        Text(
            text = "Toronto Landmark Navigator",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Category List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(categories.size) { index ->
                val (category, backgroundImage) = categories[index]
                CategoryCard(category, backgroundImage, onCategoryClick)
            }
        }
    }
}

@Composable
fun CategoryCard(category: String, backgroundImage: Int, onCategoryClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCategoryClick(category) }
            .shadow(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = backgroundImage),
                contentDescription = "$category Background",
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = category,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 35.sp, // Increase font size
                    fontWeight = FontWeight.Bold // Make the text bold
                ),

                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
