package com.kubot.monhunsetselector.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kubot.monhunsetselector.data.models.*


@Composable
fun EquipmentDetailsDialog(
    equipment: Any,
    isSelectionMode: Boolean,
    onDismissRequest: () -> Unit,
    onEquipmentSelected: (Any) -> Unit
) {
    // The Dialog composable provides the overlay window.
    Dialog(onDismissRequest = onDismissRequest) {
        // We use a Card for the background and shape.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                // Constrain the height to 80% of the screen to make it feel like an overlay
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Determine the name and stats map based on the equipment type
                val name: String
                val stats: Bundle

                when (equipment) {
                    is ArmorPiece -> {
                        name = equipment.name
                        stats = equipment.allStats
                    }
                    is Weapon -> {
                        name = equipment.name
                        stats = equipment.allStats
                    }
                    is Skill -> {
                        name = equipment.name
                        stats = equipment.details
                    }
                    else -> {
                        // Fallback for unknown types
                        name = "Unknown Item"
                        stats = Bundle()
                    }
                }

                // Header
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable list for all stats
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Iterate through every key-value pair in the stats map
                    val keys = stats.keySet().toList()
                    items(keys) { key ->
                        val value = stats.get(key)?.toString() ?: "null"
                        StatRow(statName = key, statValue = value)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    // Only show the "Select" button if we are in selection mode
                    if (isSelectionMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onEquipmentSelected(equipment) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Select")
                        }
                    }
                }
            }
        }
    }
}

/**
 * A simple row to display a stat's name and its value.
 */
@Composable
fun StatRow(statName: String, statValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = statName, fontWeight = FontWeight.Bold)
        Text(text = statValue)
    }
}