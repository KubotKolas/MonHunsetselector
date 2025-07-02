package com.kubot.monhunsetselector.utils

import androidx.compose.ui.graphics.Color


// A data class to hold parsed skill information
data class ParsedSkill(val name: String, val level: Int)

// A data class to hold parsed material information
data class ParsedMaterial(val name: String, val quantity: Int)

// A data class for a single level of progression
data class ParsedProgressionLevel(val title: String, val description: String)

// NEW: Data class to hold a single segment of the sharpness bar
data class SharpnessSegment(val color: Color, val length: Float)

/**
 * A singleton object containing all the logic for parsing complex strings from the database.
 */
object DataParser {

    /**
     * Parses a string like "Attack Boost Lv. 2|Critical Eye Lv. 1"
     * into a List of ParsedSkill objects. Handles various delimiters.
     */
    fun parseSkills(skillsString: String?): List<ParsedSkill> {
        if (skillsString.isNullOrBlank()) return emptyList()

        println("PARSE_SKILLS: Trying to parse skills string '$skillsString'.")

        // --- First, split the string into individual skill parts ---
        return skillsString.split("|").mapNotNull { skillPart ->
            val trimmedPart = skillPart.trim()
            if (trimmedPart.isBlank()) return@mapNotNull null

            // --- Now, apply parsing logic to EACH part ---

            // Regex to find the level at the end of the string, e.g., "... Lv. 1" or "... +1"
            // It captures the name and the number.
            val regex = Regex("""^(.*)\s+(?:Lv\.?|\+)\s*(\d+)$""", RegexOption.IGNORE_CASE)
            val match = regex.find(trimmedPart)

            if (match != null) {
                // Format is "Skill Name Lv. X" or "Skill Name +X"
                val skillName = match.groupValues[1].trim()
                val level = match.groupValues[2].toIntOrNull() ?: 0
                if (level > 0) ParsedSkill(skillName, level) else null
            } else {
                // If the regex doesn't match, assume the whole string is the name and the level is 1
                ParsedSkill(trimmedPart, 1)
            }
        }
    }

    /**
     * Parses a string like "Iron Ore x5|Monster Bone+ x2|3000z"
     * into a List of ParsedMaterial objects.
     */
    fun parseMaterials(materialsString: String?): List<ParsedMaterial> {
        if (materialsString.isNullOrBlank()) return emptyList()

        return try
        {
            materialsString.split("|").mapNotNull { materialPart ->
                val trimmedPart = materialPart.trim()
                when {
                    // Handle the zenny case
                    trimmedPart.endsWith("z", ignoreCase = true) -> {
                        val quantity = trimmedPart.dropLast(1).toIntOrNull() ?: 0
                        if (quantity > 0) ParsedMaterial("Zenny", quantity) else null
                    }
                    // Handle the "Name xQuantity" case
                    trimmedPart.contains(" x") -> {
                        val parts = trimmedPart.split(" x", limit = 2)
                        val materialName = parts[0].trim()
                        val quantity = parts[1].trim().toIntOrNull() ?: 0
                        if (quantity > 0) ParsedMaterial(materialName, quantity) else null
                    }
                    // Handle items with no quantity (assumes quantity is 1)
                    else -> ParsedMaterial(trimmedPart, 1)
                }
            }
        }
        catch (e: Exception){
            println("ERROR_PARSE_MATERIALS: Failed to parse materials string '$materialsString'. Error: $e")
            emptyList()
            }
    }

    /**
     * Parses a raw progression string like "Level 1: A | Level 2 - B"
     * into a list of structured level objects.
     */
    fun parseProgression(progressionString: String?): List<ParsedProgressionLevel> {
        if (progressionString.isNullOrBlank()) return emptyList()

        return progressionString.split("|").mapNotNull { part ->
            val trimmedPart = part.trim()
            if (trimmedPart.isBlank()) return@mapNotNull null

            // Use a regex to find the title (e.g., "Level 1") and the description
            // This is robust against different delimiters (:, -, or just space)
            val matchResult = Regex("""(Level\s*\d+)\s*[:\-]?\s*(.+)""").find(trimmedPart)

            if (matchResult != null) {
                val title = matchResult.groupValues[1]
                val description = matchResult.groupValues[2]
                ParsedProgressionLevel(title, description)
            } else {
                // Fallback for parts that don't match the "Level X ..." pattern
                ParsedProgressionLevel("Info", trimmedPart)
            }
        }
    }

    /**
     * Parses a string like "Red:2.3|Orange:20|Yellow:25|Green:28.1|Blue:5|White:5.6|Purple:9"
     * into a list of structured SharpnessSegment objects.
     */
    fun parseSharpness(sharpnessString: String?): List<SharpnessSegment> {
        if (sharpnessString.isNullOrBlank()) return emptyList()

        // --- NEW: Add a try-catch block for safety ---
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
                    else -> Color.Gray // Fallback for unknown colors
                }
                SharpnessSegment(color, length)
            }
        } catch (e: Exception) {
            // If anything goes wrong during parsing, log the error and return an empty list.
            println("ERROR_PARSE_SHARPNESS: Failed to parse sharpness string '$sharpnessString'. Error: $e")
            emptyList()
        }
    }
}

