package com.kubot.monhunsetselector.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kubot.monhunsetselector.ui.components.DetailsDialog
import com.kubot.monhunsetselector.ui.components.EquipmentCard
import com.kubot.monhunsetselector.ui.components.EquipmentDetailsDialog
import com.kubot.monhunsetselector.ui.viewmodel.BrowseViewModel
import com.kubot.monhunsetselector.ui.viewmodel.mainCategories
import com.kubot.monhunsetselector.ui.viewmodel.subCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    onEquipmentSelected: (Any) -> Unit,
    initialMainCategory: String?,
    initialSubCategory: String?,
    isSelectionMode: Boolean
) {
    println("DEBUG_SELECTION_MODE: BrowseScreen composed with isSelectionMode = $isSelectionMode")
    // Instantiate the ViewModel
    val viewModel: BrowseViewModel = viewModel()


    LaunchedEffect(Unit) {
        viewModel.initializeFromNavArgs(initialMainCategory, initialSubCategory)
    }

    // Collect states from the ViewModel
    val mainCategory by viewModel.selectedMainCategory.collectAsState()
    val subCategory by viewModel.selectedSubCategory.collectAsState()
//    val equipmentList by viewModel.equipmentList.collectAsState()

    // Get the list of available sub-categories based on the main selection
    val availableSubCategories = subCategories[mainCategory] ?: emptyList()
//    val isSelectionMode = navController.previousBackStackEntry?.destination?.route?.startsWith("set_builder") == true

    var selectedEquipment by remember { mutableStateOf<Any?>(null) }

    selectedEquipment?.let { equipment ->
//        EquipmentDetailsDialog(
        DetailsDialog(
            item = equipment,
            isSelectionMode = isSelectionMode,
            onDismissRequest = { selectedEquipment = null }, // This lambda hides the dialog
            onItemSelected = if (isSelectionMode) {
                { selectedItem -> onEquipmentSelected(selectedItem) }
            } else {
                null // Pass null when not in selection mode
            }
        )
    }


    val searchQuery by viewModel.searchQuery.collectAsState()
    val equipmentList by viewModel.filteredEquipmentList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Equipment") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Navigation Drawer")
                    }
                }
            )
        }
    )
    {
        innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            // Dropdowns Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                // Main Category Dropdown
                AppExposedDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Category",
                    items = mainCategories,
                    selectedItem = mainCategory,
                    onItemSelected = { viewModel.onMainCategorySelected(it) }
                )

                // Sub Category Dropdown
                AppExposedDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Type",
                    items = availableSubCategories,
                    selectedItem = subCategory,
                    onItemSelected = { viewModel.onSubCategorySelected(it) },
                    // Disable the dropdown if there are no sub-categories (like for Skills)
                    enabled = availableSubCategories.isNotEmpty()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results List
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp), // Creates as many columns as fit, each at least 300dp wide
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 1000.dp) // Constrain height to avoid scrolling issues
            ) {
                items(equipmentList) { equipment ->
                    // Use your EquipmentCard to display the item
                    EquipmentCard(
                        equipment = equipment,
                        onClick = {selectedEquipment = equipment}
                    )

                    // For the placeholder:
                    // Text(equipment.toString())
                }
            }
        }
    }
}

/**
 * A reusable composable for an exposed dropdown menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppExposedDropdownMenu( // <-- RENAMED HERE
    modifier: Modifier = Modifier,
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            enabled = enabled
        )
        // This now correctly calls the BUILT-IN Material 3 composable
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // This check prevents a crash if the list is empty and selectedItem is not blank
            if (items.isNotEmpty()) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}