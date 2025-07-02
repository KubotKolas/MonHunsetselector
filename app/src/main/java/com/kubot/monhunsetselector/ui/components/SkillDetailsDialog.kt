package com.kubot.monhunsetselector.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.repository.GameDataRepository
import com.kubot.monhunsetselector.ui.viewmodel.SetBuilderViewModel


@Composable
fun SkillDetailsDialog(
    skillName: String,
    gameDataRepository: GameDataRepository,
    onDismissRequest: () -> Unit
) {
    var skill by remember { mutableStateOf<Skill?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    println("DEBUG_DIALOG: Dialog recomposed for skill: '$skillName'")

    LaunchedEffect(skillName) {
        println("DEBUG_DIALOG: LaunchedEffect started for '$skillName'. Setting isLoading=true.")
        isLoading = true
        // This is a suspend call, so it will wait for the result
        val fetchedSkill = gameDataRepository.getSkillByName(skillName)

        println("DEBUG_DIALOG: LaunchedEffect received skill from VM: ${fetchedSkill?.name ?: "NULL"}")

        // Update the local state
        skill = fetchedSkill
        isLoading = false
        println("DEBUG_DIALOG: LaunchedEffect finished. isLoading=false, skill name is now: ${skill?.name ?: "NULL"}")
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card() {
            if (isLoading) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (skill != null) {
                // Use a LazyColumn to handle potentially long lists of details
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Section
                    item {
                        Text(skill!!.name, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(skill!!.description, style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    // --- NEW: Display all items from the 'details' bundle ---
//                    val detailKeys = skill!!.details.keySet().toList().sorted()
//                    items(detailKeys) { key ->
//                        val value = skill!!.details.get(key)?.toString() ?: "N/A"
//                        // Use your existing StatRow or a similar composable
//                        StatRow(statName = key, statValue = value)
//                    }
                    // Progression Section
                    val progressionString = skill!!.details.getString("Progression")
                    if (!progressionString.isNullOrBlank()) {
                        item {
                            Text(
                                text = "Progression",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Parse the string and display each level
                        val parsedLevels = parseProgression(progressionString)
                        items(parsedLevels) { (levelTitle, levelDesc) ->
                            // Use buildAnnotatedString to style the title differently
                            Text(
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                        append("$levelTitle: ")
                                    }
                                    append(levelDesc)
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    // Other Details Section
                    val detailKeys = skill!!.details.keySet().toList().sorted()
                    items(detailKeys) { key ->
                        // Don't show Progression again, as we've handled it
                        if (key != "Progression") {
                            val value = skill!!.details.get(key)?.toString() ?: "N/A"
                            StatRow(statName = key, statValue = value)
                        }
                    }

                    // Footer Section
                    item {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onDismissRequest) { Text("Close") }
                    }
                }
            } else {
                // ... Not found state ...
            }
        }
    }
}

/**
 * A robust parser for a progression string. It handles multiple delimiters (':', '-')
 * and falls back gracefully.
 */
private fun parseProgression(progressionString: String): List<Pair<String, String>> {
    return progressionString
        .split("|") // Split into individual level parts (e.g., "Level 1: ...", "Level 2: ...")
        .mapNotNull { part ->
            val trimmedPart = part.trim()
            if (trimmedPart.isBlank()) return@mapNotNull null

            var title = ""
            var description = ""

            // --- The new, safer parsing logic ---

            // 1. Try to split by ":" first
            var parts = trimmedPart.split(":", limit = 2)
            if (parts.size == 2) {
                title = parts[0].trim()
                description = parts[1].trim()
                return@mapNotNull Pair(title, description)
            }

            // 2. If that failed, try to split by "-"
            parts = trimmedPart.split("-", limit = 2)
            if (parts.size == 2) {
                title = parts[0].trim()
                description = parts[1].trim()
                return@mapNotNull Pair(title, description)
            }

            // 3. If that also failed, try to split by the first space after a number
            // This handles "Level 1 Does something"
            val matchResult = Regex("""(Level\s*\d+)\s+(.+)""").find(trimmedPart)
            if (matchResult != null) {
                title = matchResult.groupValues[1]
                description = matchResult.groupValues[2]
                return@mapNotNull Pair(title, description)
            }

            // 4. As a final fallback, if no pattern matched, use the whole part as the description
            // and an empty string for the title to prevent data loss.
            title = ""
            description = trimmedPart
            Pair(title, description)
        }
}