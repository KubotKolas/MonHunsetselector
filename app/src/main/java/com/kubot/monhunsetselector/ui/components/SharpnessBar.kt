package com.kubot.monhunsetselector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kubot.monhunsetselector.utils.SharpnessSegment

@Composable
fun SharpnessBar(segments: List<SharpnessSegment>) {


    val validSegments = segments.filter { it.length > 0f }


    if (validSegments.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
    ) {

        validSegments.forEach { segment ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()

                    .weight(segment.length)
                    .background(segment.color)
            )
        }
    }
}