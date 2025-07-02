package com.kubot.monhunsetselector.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

//@Composable
fun LazyListScope.SlotDisplayCard(
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit // The content to display inside (e.g., your Text)
) {
    // A Column to hold the label and the card together
    item ()
    {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            // The small label above the card (e.g., "Weapon", "Head")
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            // The main card that provides the background and click effect
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                // This Box handles the click ripple and crops it to the card's shape
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardDefaults.shape)
                        .clickable(onClick = onClick)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 20.dp
                        ) // Generous padding for a good tap area
                ) {
                    // The content (your Text composable) is placed inside the Box
                    content()
                }
            }
        }
    }
}