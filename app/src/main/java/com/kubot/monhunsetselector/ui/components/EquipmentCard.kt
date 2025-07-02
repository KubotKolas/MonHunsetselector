package com.kubot.monhunsetselector.ui.components

import android.service.autofill.OnClickAction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.models.Weapon


/**
 * A generic card that can display details for any piece of equipment.
 */
@Composable
fun EquipmentCard(
        equipment: Any,
        onClick: () -> Unit = {}
    ) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
//        .clickable(onClick = onClick)
    ) {
        // Use a 'when' statement to render the correct content based on the object's type.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape) // Use the Card's default shape (usually RoundedCornerShape)
                .clickable(onClick = onClick)
        )
        {
            when (equipment) {
                is ArmorPiece -> ArmorContent(armor = equipment)
                is Weapon -> WeaponContent(weapon = equipment)
                is Skill -> SkillContent(skill = equipment)
                // You can add 'is Skill' here later if needed.
                else -> Text("Unknown equipment type", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ArmorContent(armor: ArmorPiece) {
    val stats = armor.allStats
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = armor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Safely access values from the map. Use 'as? Type' to prevent crashes.
        val defense = stats.get("Defense")?.toString() ?: "N/A"
        val rarity = stats.get("Rarity")?.toString() ?: "N/A"

        Text("Rarity: $rarity | Defense: $defense")

        // Here you would parse and display more complex stats like resistances or skills.
        // For example:
        // val skillsString = stats["Skills"] as? String ?: ""
        // Text("Skills: $skillsString")
    }
}

@Composable
private fun WeaponContent(weapon: Weapon) {
    val stats = weapon.allStats
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = weapon.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Safely access values from the map.
        val attack = stats.get("Attack")?.toString() ?: "N/A"
        val affinity = stats.get("Affinity")?.toString() ?: "N/A"

        Text("Attack: $attack | Affinity: $affinity")

        // Here you would parse and display the more complex string stats.
        // For example:
        // val sharpness = stats["Sharpness"] as? String ?: "No sharpness data"
        // DisplaySharpnessBar(sharpness) // A custom composable you would write
    }
}

@Composable
private fun SkillContent(skill: Skill) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = skill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        val levels = skill.details.get("Levels")?.toString() ?: "N/A"
        Text("Max Levels: $levels")

        if (skill.description.isNotBlank()) {
            Text(text = skill.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}