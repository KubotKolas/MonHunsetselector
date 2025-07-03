package com.kubot.monhunsetselector.ui.components


import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.repository.GameDataRepository


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

        val fetchedSkill = gameDataRepository.getSkillByName(skillName)

        println("DEBUG_DIALOG: LaunchedEffect received skill from VM: ${fetchedSkill?.name ?: "NULL"}")


        skill = fetchedSkill
        isLoading = false
        println("DEBUG_DIALOG: LaunchedEffect finished. isLoading=false, skill name is now: ${skill?.name ?: "NULL"}")
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            if (isLoading) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (skill != null) {

                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    item {
                        Text(skill!!.name, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(skill!!.description, style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                    }


                    val progressionString = skill!!.details.getString("Progression")
                    if (!progressionString.isNullOrBlank()) {
                        item {
                            Text(
                                text = "Progression",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }


                        val parsedLevels = parseProgression(progressionString)
                        items(parsedLevels) { (levelTitle, levelDesc) ->

                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
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


                    val detailKeys = skill!!.details.keySet().toList().sorted()
                    items(detailKeys) { key ->

                        if (key != "Progression") {
                            val value = skill!!.details.get(key)?.toString() ?: "N/A"
                            StatRow(statName = key, statValue = value)
                        }
                    }


                    item {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onDismissRequest) { Text("Close") }
                    }
                }
            } else {

            }
        }
    }
}


private fun parseProgression(progressionString: String): List<Pair<String, String>> {
    return progressionString
        .split("|")
        .mapNotNull { part ->
            val trimmedPart = part.trim()
            if (trimmedPart.isBlank()) return@mapNotNull null

            var title = ""
            var description = ""


            var parts = trimmedPart.split(":", limit = 2)
            if (parts.size == 2) {
                title = parts[0].trim()
                description = parts[1].trim()
                return@mapNotNull Pair(title, description)
            }


            parts = trimmedPart.split("-", limit = 2)
            if (parts.size == 2) {
                title = parts[0].trim()
                description = parts[1].trim()
                return@mapNotNull Pair(title, description)
            }


            val matchResult = Regex("""(Level\s*\d+)\s+(.+)""").find(trimmedPart)
            if (matchResult != null) {
                title = matchResult.groupValues[1]
                description = matchResult.groupValues[2]
                return@mapNotNull Pair(title, description)
            }



            title = ""
            description = trimmedPart
            Pair(title, description)
        }
}