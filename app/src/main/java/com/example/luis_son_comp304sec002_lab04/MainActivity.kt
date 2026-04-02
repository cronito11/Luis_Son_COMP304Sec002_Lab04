package com.example.luis_son_comp304sec002_lab04

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        Triple("Clubs", R.drawable.rebel, "Nightlife & Entertainment"),
        Triple("Beaches", R.drawable.beach, "Waterfront Escapes"),
        Triple("Parks", R.drawable.parks, "Nature & Green Spaces"),
        Triple("Fun Activities", R.drawable.k1, "Adventure & Thrills")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0D0D),
                            Color(0xFF1A1A2E)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 32.dp)
                ) {
                    Column {
                        Text(
                            text = "TORONTO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp,
                            color = Color(0xFFE8B84B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Landmark\nNavigator",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 44.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Discover the city's best spots",
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            items(categories.size) { index ->
                val (category, backgroundImage, subtitle) = categories[index]
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = index * 100)) +
                            slideInVertically(tween(400, delayMillis = index * 100)) { it / 3 }
                ) {
                    CategoryCard(category, subtitle, backgroundImage, onCategoryClick)
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: String,
    subtitle: String,
    backgroundImage: Int,
    onCategoryClick: (String) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                pressed = true
                onCategoryClick(category)
            }
    ) {
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = "$category Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .width(4.dp)
                .height(190.dp)
                .align(Alignment.CenterStart)
                .background(Color(0xFFE8B84B))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 18.dp, end = 16.dp)
        ) {
            Text(
                text = category.uppercase(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFFE8B84B),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = "→",
            fontSize = 22.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
        )
    }
}