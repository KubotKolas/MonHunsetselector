package com.kubot.monhunsetselector.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun EquipmentSlotCard(
    label: String,
    // The selected equipment object, can be null
    selectedEquipment: Any?,
    onCardClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // If nothing is selected, show a placeholder card
        if (selectedEquipment == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Give it a fixed height
                    .clickable(onClick = onCardClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Tap to select $label",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // If an item is selected, display it using the real EquipmentCard
            // We wrap it in a Box to make the whole area clickable, even if the
            // EquipmentCard itself didn't have a click modifier.
            Box(modifier = Modifier.clickable(onClick = onCardClick)) {
                EquipmentCard(equipment = selectedEquipment, onClick = {})
            }
        }
    }
}