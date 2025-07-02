package com.kubot.monhunsetselector.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.models.Weapon
import com.kubot.monhunsetselector.utils.DataParser

private val fieldDisplayOrder = listOf(
    "Name", "Rarity", "Rank", "Type", "Levels", "Attack", "Element", "Affinity",
    "Defense", "Sharpness", "Slots", "Fire Res", "Water Res", "Thunder Res", "Ice Res", "Dragon Res",
    "Skills", "Progression", "Description", "Materials", "Forging"
)

@Composable
fun DetailsDialog(
    item: Any,
    isSelectionMode: Boolean,
    onDismissRequest: () -> Unit,
//    onItemSelected: (Any) -> Unit
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
            // Determine the title and details bundle based on the item type
            val title: String
            val description: String
            val detailsBundle: Bundle

            when (item) {
                is Weapon -> {
                    title = item.name
                    description = item.allStats.getString("Description", "") // Assuming weapons might have a description
                    detailsBundle = item.allStats
                }
                is ArmorPiece -> {
                    title = item.name
                    description = item.allStats.getString("Description", "") // Assuming armor might have one
                    detailsBundle = item.allStats
                }
                is Skill -> {
                    title = item.name
                    description = item.description // Skills have a dedicated description field
                    detailsBundle = item.details
                }
                else -> {
                    title = "Unknown Item"
                    description = ""
                    detailsBundle = Bundle()
                }
            }

            // The main layout for the dialog content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Use a LazyColumn to make the content scrollable
                LazyColumn(
                    modifier = Modifier.weight(1f), // Takes up all space except for the buttons
                    contentPadding = PaddingValues(16.dp)
                ) {
//                    // --- Header ---
//                    item {
//                        Text(text = title, style = MaterialTheme.typography.headlineSmall)
//                        if (description.isNotBlank()) {
//                            Spacer(Modifier.height(8.dp))
//                            Text(text = description, style = MaterialTheme.typography.bodyMedium)
//                        }
//                        Divider(modifier = Modifier.padding(vertical = 16.dp))
//                    }
//
//                    // --- All Other Details ---
//                    val detailKeys = detailsBundle.keySet().toList().sorted()
//                    items(detailKeys) { key ->
//                        // Don't re-display keys we already showed in the header
//                        if (key !in listOf("Name", "Description")) {
//                            val value = detailsBundle.get(key)?.toString() ?: "N/A"
//                            StatRow(statName = key, statValue = value)
//                        }
//                    }



//                    val keys = detailsBundle.keySet().toList()
//                    items(keys) { key ->
//                        val valueString = detailsBundle.get(key)?.toString() ?: "N/A"
//
//                        // Use a 'when' statement to give special UI to parsed fields
//                        when (key) {
//                            "Skills", "Skill" -> {
//                                val parsedSkills = DataParser.parseSkills(valueString)
//                                if (parsedSkills.isNotEmpty()) {
//                                    SectionTitle(title = "Skills")
//                                    parsedSkills.forEach { skill ->
//                                        // You could make a small composable for this row
//                                        Text("  • ${skill.name} Lv. ${skill.level}")
//                                    }
//                                }
//                            }
//                            "Materials", "Forging" -> {
//                                val parsedMaterials = DataParser.parseMaterials(valueString)
//                                if (parsedMaterials.isNotEmpty()) {
//                                    SectionTitle(title = "Materials")
//                                    parsedMaterials.forEach { material ->
//                                        StatRow(statName = material.name, statValue = "x${material.quantity}")
//                                    }
//                                }
//                            }
//                            "Progression" -> {
//                                val parsedProgression = DataParser.parseProgression(valueString)
//                                if (parsedProgression.isNotEmpty()) {
//                                    SectionTitle(title = "Progression")
//                                    parsedProgression.forEach { level ->
//                                        Text(
//                                            buildAnnotatedString {
//                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                                    append("${level.title}: ")
//                                                }
//                                                append(level.description)
//                                            },
//                                            modifier = Modifier.padding(bottom = 4.dp)
//                                        )
//                                    }
//                                }
//                            }
//                            // For all other keys, use the generic StatRow
//                            else -> {
//                                StatRow(statName = key, statValue = valueString)
//                            }
//                        }
//                    }


//                    // --- HEADER (Name and primary description) ---
//                    item {
//                        Text(text = title, style = MaterialTheme.typography.headlineSmall)
//                        if (description.isNotBlank()) {
//                            Spacer(Modifier.height(8.dp))
//                            // Use buildAnnotatedString for the bold "Description" title
//                            Text(buildAnnotatedString {
//                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Description: ") }
//                                append(description)
//                            })
//                        }
//                        Divider(modifier = Modifier.padding(vertical = 16.dp))
//                    }
//
//                    // --- STRUCTURED STATS (Iterate through our ordered list) ---
//                    items(fieldDisplayOrder) { key ->
//                        // Check if the bundle actually has this key
//                        if (detailsBundle.containsKey(key)) {
//                            val value = detailsBundle.get(key)?.toString() ?: ""
//                            if (value.isNotBlank()) {
//                                // Use a when block for special UI handling
//                                when (key) {
//                                    "Name", "Description" -> { /* Already handled in header, do nothing */ }
//
//                                    "Sharpness" -> {
//                                        Section(title = "Sharpness") {
//                                            SharpnessBar(segments = DataParser.parseSharpness(value))
//                                        }
//                                    }
//
//                                    "Skills", "Skill" -> {
//                                        Section(title = "Skills") {
//                                            DataParser.parseSkills(value).forEach { skill ->
//                                                Text("• ${skill.name} Lv. ${skill.level}", color = MaterialTheme.colorScheme.primary)
//                                            }
//                                        }
//                                    }
//
//                                    "Progression" -> {
//                                        Section(title = "Progression") {
//                                            DataParser.parseProgression(value).forEach { level ->
//                                                Text(buildAnnotatedString {
//                                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("${level.title}: ") }
//                                                    append(level.description)
//                                                })
//                                            }
//                                        }
//                                    }
//
//                                    "Materials", "Forging" -> {
//                                        Section(title = "Materials") {
//                                            DataParser.parseMaterials(value).forEach { material ->
//                                                StatRow(statName = material.name, statValue = "x${material.quantity}")
//                                            }
//                                        }
//                                    }
//
//                                    // Default case for simple key-value pairs
//                                    else -> {
//                                        StatRow(statName = key, statValue = value)
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // --- Optional: Display any keys not in our ordered list at the end ---
//                    val remainingKeys = detailsBundle.keySet().filter { it !in fieldDisplayOrder }
//                    if (remainingKeys.isNotEmpty()) {
//                        item { SectionTitle("Other Info") }
//                        items(remainingKeys) { key ->
//                            StatRow(statName = key, statValue = detailsBundle.get(key)?.toString() ?: "")
//                        }
//                    }



                    // --- HEADER ---
                    item {
                        Text(text = title, style = MaterialTheme.typography.headlineMedium)
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    // --- STATS SECTION (Key-Value Pairs) ---
                    // We'll define which keys are simple stats
                    val simpleStatKeys = listOf(
                        "Rarity", "Rank", "Type", "Levels", "Attack", "Element", "Affinity", "Defense", "Slots"
                    )
                    items(simpleStatKeys) { key ->
                        if (detailsBundle.containsKey(key)) {
                            StatRow(statName = key, statValue = detailsBundle.get(key).toString())
                        }
                    }

                    // --- SHARPNESS SECTION ---
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

                    // --- SKILLS SECTION (List-Based) ---
                    val skillsString = detailsBundle.getString("Skills", "").ifBlank { detailsBundle.getString("Skill", "") }
                    if (skillsString.isNotBlank()) {
                        item {
                            ListSectionHeader(title = "Skills")
                        }
                        val parsedSkills = DataParser.parseSkills(skillsString)
                        items(parsedSkills) { skill ->
                            Text(
                                text = "• ${skill.name} Lv. ${skill.level}",
                                color = MaterialTheme.colorScheme.primary, // Highlight color
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }
                    }

                    // --- MATERIALS SECTION (List-Based) ---
                    val materialsString = detailsBundle.getString("Materials", "").ifBlank { detailsBundle.getString("Forging", "") }
                    if (materialsString.isNotBlank()) {
                        item {
                            ListSectionHeader(title = "Materials")
                        }
                        val parsedMaterials = DataParser.parseMaterials(materialsString)
                        items(parsedMaterials) { material ->
                            // Use StatRow for alignment
                            StatRow(statName = material.name, statValue = "x${material.quantity}")
                        }
                    }

                    // --- DESCRIPTION SECTION ---
                    if (description.isNotBlank()) {
                        item {
                            ListSectionHeader(title = "Description")
                        }
                        item {
                            Text(description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    item{
                        // --- Footer with Action Buttons ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = if (isSelectionMode) Arrangement.SpaceEvenly else Arrangement.Center
                        ) {
//                    if (isSelectionMode) {
//                        Button(
//                            onClick = { onItemSelected(item) },
//                            modifier = Modifier.weight(1f),
//                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
//                        ) {
//                            Text("Select")
//                        }
//                        Spacer(modifier = Modifier.width(8.dp))
//                    }
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


// Helper composable to reduce repetition
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

// --- NEW: A helper composable to create consistent sections ---
@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

// --- NEW HELPER COMPOSABLE for section titles like "Skills" and "Materials" ---
@Composable
private fun ListSectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge // Use a larger font for these headers
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}