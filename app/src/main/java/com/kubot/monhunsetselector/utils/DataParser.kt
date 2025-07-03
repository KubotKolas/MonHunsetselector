package com.kubot.monhunsetselector.utils

import androidx.compose.ui.graphics.Color


data class ParsedSkill(val name: String, val level: Int)

data class ParsedMaterial(val name: String, val quantity: Int)

data class ParsedProgressionLevel(val title: String, val description: String)

data class SharpnessSegment(val color: Color, val length: Float)


object DataParser {

    fun parseSkills(skillsString: String?): List<ParsedSkill> {
        if (skillsString.isNullOrBlank()) return emptyList()

        println("PARSE_SKILLS: Trying to parse skills string '$skillsString'.")


        return skillsString.split("|").mapNotNull { skillPart ->
            val trimmedPart = skillPart.trim()
            if (trimmedPart.isBlank()) return@mapNotNull null


            val regex = Regex("""^(.*)\s+(?:Lv\.?|\+)\s*(\d+)$""", RegexOption.IGNORE_CASE)
            val match = regex.find(trimmedPart)

            if (match != null) {

                val skillName = match.groupValues[1].trim()
                val level = match.groupValues[2].toIntOrNull() ?: 0
                if (level > 0) ParsedSkill(skillName, level) else null
            } else {

                ParsedSkill(trimmedPart, 1)
            }
        }
    }

    fun parseMaterials(materialsString: String?): List<ParsedMaterial> {
        if (materialsString.isNullOrBlank()) return emptyList()

        return try {
            materialsString.split("|").mapNotNull { materialPart ->
                val trimmedPart = materialPart.trim()
                when {

                    trimmedPart.endsWith("z", ignoreCase = true) -> {
                        val quantity = trimmedPart.dropLast(1).toIntOrNull() ?: 0
                        if (quantity > 0) ParsedMaterial("Zenny", quantity) else null
                    }

                    trimmedPart.contains(" x") -> {
                        val parts = trimmedPart.split(" x", limit = 2)
                        val materialName = parts[0].trim()
                        val quantity = parts[1].trim().toIntOrNull() ?: 0
                        if (quantity > 0) ParsedMaterial(materialName, quantity) else null
                    }

                    else -> ParsedMaterial(trimmedPart, 1)
                }
            }
        } catch (e: Exception) {
            println("ERROR_PARSE_MATERIALS: Failed to parse materials string '$materialsString'. Error: $e")
            emptyList()
        }
    }

    fun parseProgression(progressionString: String?): List<ParsedProgressionLevel> {
        if (progressionString.isNullOrBlank()) return emptyList()

        return progressionString.split("|").mapNotNull { part ->
            val trimmedPart = part.trim()
            if (trimmedPart.isBlank()) return@mapNotNull null


            val matchResult = Regex("""(Level\s*\d+)\s*[:\-]?\s*(.+)""").find(trimmedPart)

            if (matchResult != null) {
                val title = matchResult.groupValues[1]
                val description = matchResult.groupValues[2]
                ParsedProgressionLevel(title, description)
            } else {

                ParsedProgressionLevel("Info", trimmedPart)
            }
        }
    }

    fun parseSharpness(sharpnessString: String?): List<SharpnessSegment> {
        if (sharpnessString.isNullOrBlank()) return emptyList()


        return try {
            sharpnessString.split("|").mapNotNull { segment ->
                val parts = segment.split(":", limit = 2)
                if (parts.size != 2) return@mapNotNull null

                val colorName = parts[0].trim().lowercase()
                val length = parts[1].trim().toFloatOrNull() ?: return@mapNotNull null

                val color = when (colorName) {
                    "red" -> Color(0xFFC73636)
                    "orange" -> Color(0xFFD6782B)
                    "yellow" -> Color(0xFFDED431)
                    "green" -> Color(0xFF7BD62B)
                    "blue" -> Color(0xFF2B8AD6)
                    "white" -> Color(0xFFD5DDE0)
                    "purple" -> Color(0xFF9045D6)
                    else -> Color.Gray
                }
                SharpnessSegment(color, length)
            }
        } catch (e: Exception) {

            println("ERROR_PARSE_SHARPNESS: Failed to parse sharpness string '$sharpnessString'. Error: $e")
            emptyList()
        }
    }
}

