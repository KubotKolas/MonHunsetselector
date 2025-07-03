package com.kubot.monhunsetselector.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kubot.monhunsetselector.ui.components.DetailsDialog
import com.kubot.monhunsetselector.ui.components.EquipmentCard
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

    val viewModel: BrowseViewModel = viewModel()


    LaunchedEffect(Unit) {
        viewModel.initializeFromNavArgs(initialMainCategory, initialSubCategory)
    }


    val mainCategory by viewModel.selectedMainCategory.collectAsState()
    val subCategory by viewModel.selectedSubCategory.collectAsState()


    val availableSubCategories = subCategories[mainCategory] ?: emptyList()


    var selectedEquipment by remember { mutableStateOf<Any?>(null) }

    selectedEquipment?.let { equipment ->

        DetailsDialog(
            item = equipment,
            isSelectionMode = isSelectionMode,
            onDismissRequest = { selectedEquipment = null },
            onItemSelected = if (isSelectionMode) {
                { selectedItem -> onEquipmentSelected(selectedItem) }
            } else {
                null
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
    { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {

                AppExposedDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Category",
                    items = mainCategories,
                    selectedItem = mainCategory,
                    onItemSelected = { viewModel.onMainCategorySelected(it) }
                )


                AppExposedDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Type",
                    items = availableSubCategories,
                    selectedItem = subCategory,
                    onItemSelected = { viewModel.onSubCategorySelected(it) },

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


            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 1000.dp)
            ) {
                items(equipmentList) { equipment ->

                    EquipmentCard(
                        equipment = equipment,
                        onClick = { selectedEquipment = equipment }
                    )


                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppExposedDropdownMenu(
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

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

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