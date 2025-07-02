package com.kubot.monhunsetselector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kubot.monhunsetselector.utils.SharpnessSegment

@Composable
fun SharpnessBar(segments: List<SharpnessSegment>) {
    // --- THE KEY FIX: Filter out any invalid segments ---
    // Only keep segments where the length is a positive number.
    val validSegments = segments.filter { it.length > 0f }

    // If there are no valid segments left, don't draw anything.
    if (validSegments.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
    ) {
        // Now, iterate through the *valid* segments.
        validSegments.forEach { segment ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    // This is now guaranteed to be a positive weight.
                    .weight(segment.length)
                    .background(segment.color)
            )
        }
    }
}