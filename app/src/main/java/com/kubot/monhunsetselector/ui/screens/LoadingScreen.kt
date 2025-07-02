package com.kubot.monhunsetselector.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kubot.monhunsetselector.R

@Composable
fun LoadingScreen() {
//    var startAnimation by remember { mutableStateOf(false) }
//
//    // Animate the alpha (transparency) of the logo
//    val alphaAnim = animateFloatAsState(
//        targetValue = if (startAnimation) 1f else 0f,
//        animationSpec = tween(durationMillis = 500), // The fade-in duration
//        label = "logoAlpha"
//    )
//
//    // This triggers the animation when the screen is first composed
//    LaunchedEffect(Unit) {
//        startAnimation = true
//    }
//
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.mhwilds_logo), // <-- USE YOUR LOGO'S FILENAME
//            contentDescription = "App Logo",
//            modifier = Modifier
//                .size(240.dp) // Adjust the size as needed
//                .alpha(alphaAnim.value) // Apply the fade-in animation
//        )
//    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
}