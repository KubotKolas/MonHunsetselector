package com.kubot.monhunsetselector.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.models.Weapon
import com.kubot.monhunsetselector.utils.DataParser

@Composable
fun DetailsDialog(
    item: Any,
    isSelectionMode: Boolean,
    onDismissRequest: () -> Unit,

    onItemSelected: ((Any) -> Unit)? = null
) {
    println("DEBUG_DIALOG_BUTTONS: Dialog composed. onItemSelected is null? --> ${onItemSelected == null}")
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {

            val title: String
            val description: String
            val detailsBundle: Bundle

            when (item) {
                is Weapon -> {
                    title = item.name
                    description = item.allStats.getString("Description", "")
                    detailsBundle = item.allStats
                }

                is ArmorPiece -> {
                    title = item.name
                    description = item.allStats.getString("Description", "")
                    detailsBundle = item.allStats
                }

                is Skill -> {
                    title = item.name
                    description = item.description
                    detailsBundle = item.details
                }

                else -> {
                    title = "Unknown Item"
                    description = ""
                    detailsBundle = Bundle()
                }
            }


            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(text = title, style = MaterialTheme.typography.headlineMedium)
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                    }


                    val simpleStatKeys = listOf(
                        "Rarity",
                        "Rank",
                        "Type",
                        "Levels",
                        "Attack",
                        "Element",
                        "Affinity",
                        "Defense",
                        "Slots"
                    )
                    items(simpleStatKeys) { key ->
                        if (detailsBundle.containsKey(key)) {
                            StatRow(statName = key, statValue = detailsBundle.get(key).toString())
                        }
                    }


                    val sharpnessString = detailsBundle.getString("Sharpness")
                    if (!sharpnessString.isNullOrBlank()) {
                        item {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text("Sharpness", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                SharpnessBar(segments = DataParser.parseSharpness(sharpnessString))
                            }
                        }
                    }


                    val skillsString = detailsBundle.getString("Skills", "")
                        .ifBlank { detailsBundle.getString("Skill", "") }
                    if (skillsString.isNotBlank()) {
                        item {
                            ListSectionHeader(title = "Skills")
                        }
                        val parsedSkills = DataParser.parseSkills(skillsString)
                        items(parsedSkills) { skill ->
                            Text(
                                text = "• ${skill.name} Lv. ${skill.level}",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }
                    }


                    val materialsString = detailsBundle.getString("Materials", "")
                        .ifBlank { detailsBundle.getString("Forging", "") }
                    if (materialsString.isNotBlank()) {
                        item {
                            ListSectionHeader(title = "Materials")
                        }
                        val parsedMaterials = DataParser.parseMaterials(materialsString)
                        items(parsedMaterials) { material ->

                            StatRow(statName = material.name, statValue = "x${material.quantity}")
                        }
                    }


                    if (description.isNotBlank()) {
                        item {
                            ListSectionHeader(title = "Description")
                        }
                        item {
                            Text(description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    item {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = if (isSelectionMode) Arrangement.SpaceEvenly else Arrangement.Center
                        ) {

                            if (onItemSelected != null) {
                                Button(
                                    onClick = { onItemSelected(item) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Select")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Button(
                                onClick = onDismissRequest,
                                modifier = if (isSelectionMode) Modifier.weight(1f) else Modifier
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }


            }


        }
    }
}


@Composable
private fun ListSectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}