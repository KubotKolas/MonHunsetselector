package com.kubot.monhunsetselector.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.repository.GameDataRepository
import com.kubot.monhunsetselector.navigation.Screen
import com.kubot.monhunsetselector.ui.components.SkillCard
import com.kubot.monhunsetselector.ui.components.SkillDetailsDialog
import com.kubot.monhunsetselector.ui.components.SlotDisplayCard
import com.kubot.monhunsetselector.ui.components.StatRow
import com.kubot.monhunsetselector.ui.viewmodel.CumulativeStats
import com.kubot.monhunsetselector.ui.viewmodel.SetBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBuilderScreen(
    navController: NavController,
    setId: String?,
    onMenuClick: () -> Unit
) {
    val viewModel: SetBuilderViewModel = viewModel()
    val set by viewModel.currentSet.collectAsState()
    val stats by viewModel.cumulativeStats.collectAsState()

    var selectedSkillName by remember { mutableStateOf<String?>(null) }

    selectedSkillName?.let { skillName ->
        SkillDetailsDialog(
            skillName = skillName,

            gameDataRepository = GameDataRepository(),
            onDismissRequest = { selectedSkillName = null }
        )
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {

        savedStateHandle?.getStateFlow<Any?>("selected_equipment", null)?.collect { equipment ->

            if (equipment != null) {
                if (equipment is ArmorPiece) {
                    println("DEBUG_NAV: Equipment selected. Name: '${equipment.name}', Type: '${equipment.type}'")
                }

                viewModel.onEquipmentSelected(equipment)




                savedStateHandle.remove<Any>("selected_equipment")
            }
        }
    }


    LaunchedEffect(setId) {
        viewModel.loadSet(setId)
    }


    LaunchedEffect(viewModel.saveEvent) {
        viewModel.saveEvent.collect {
            navController.popBackStack()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Set") },
            text = { Text("Are you sure you want to permanently delete this set?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSet()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (setId == null) "Create Set" else "Edit Set") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Navigation Drawer")
                    }
                },
                actions = {
                    if (setId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Set")
                        }
                    }
                    Button(onClick = { viewModel.saveSet() }) { Text("Save") }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            item()
            {
                OutlinedTextField(
                    value = set.name,
                    onValueChange = { viewModel.onSetNameChanged(it) },
                    label = { Text("Set Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            println("DEBUG_UI_STATE: Current set state is: $set")










            SlotDisplayCard(
                label = "Weapon",
                onClick = { navController.navigate(Screen.Browse.createRoute("Weapons")) }
            ) {
                Text(
                    text = set.weaponName ?: "Tap to select",
                    style = MaterialTheme.typography.bodyLarge,

                    color = if (set.weaponName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SlotDisplayCard(
                label = "Head",
                onClick = { navController.navigate(Screen.Browse.createRoute("Armor", "Helms")) }
            ) {
                Text(
                    text = set.headName ?: "Tap to select",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (set.headName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SlotDisplayCard(
                label = "Chest",
                onClick = { navController.navigate(Screen.Browse.createRoute("Armor", "Chest")) }
            ) {
                Text(
                    text = set.chestName ?: "Tap to select",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (set.chestName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SlotDisplayCard(
                label = "Arms",
                onClick = { navController.navigate(Screen.Browse.createRoute("Armor", "Arms")) }
            ) {
                Text(
                    text = set.armsName ?: "Tap to select",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (set.armsName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SlotDisplayCard(
                label = "Waist",
                onClick = { navController.navigate(Screen.Browse.createRoute("Armor", "Waist")) }
            ) {
                Text(
                    text = set.waistName ?: "Tap to select",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (set.waistName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SlotDisplayCard(
                label = "Legs",
                onClick = { navController.navigate(Screen.Browse.createRoute("Armor", "Legs")) }
            ) {
                Text(
                    text = set.legsName ?: "Tap to select",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (set.legsName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }




            CumulativeStatsSection(
                stats = stats,
                onSkillClick = { skillName -> selectedSkillName = skillName }
            )


        }
    }
}


fun LazyListScope.CumulativeStatsSection(
    stats: CumulativeStats,
    onSkillClick: (String) -> Unit
) {


    item {
        Text(
            "Cumulative Stats",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
    if (stats.numericStats.isNotEmpty()) {
        items(stats.numericStats.entries.toList()) { (name, value) ->

            StatRow(statName = name, statValue = value.toString())
        }
    } else {
        item { Text("No stats to display.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }


    item {
        Text(
            "Skills",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
    if (stats.skills.isNotEmpty()) {
        items(stats.skills.entries.toList()) { (name, level) ->
            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                SkillCard(
                    skillName = name,
                    skillLevel = level,
                    onClick = { onSkillClick(name) }
                )
            }
        }
    } else {
        item {
            Text(
                "No skills from equipped items.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }


    if (stats.slots.isNotEmpty()) {
        item {
            Text(
                "Decoration Slots",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(stats.slots) { slotString ->

            Text(slotString, modifier = Modifier
                .padding(start = 8.dp)
                .padding(bottom = 8.dp))
        }
    }


    if (stats.materials.isNotEmpty()) {
        item {
            Text(
                "Materials",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(stats.materials.entries.toList()) { (name, quantity) ->
            StatRow(statName = name, statValue = "x$quantity")
        }
    }
}