package com.kubot.monhunsetselector.navigation

sealed class Screen(val route: String, val title: String) {
    object Loading : Screen("loading", "Loading")
    object Login : Screen("login", "Login")
    data object Browse : Screen("browse?selectionMode={selectionMode}&mainCat={mainCat}&subCat={subCat}", "Browse") {

        // This helper creates the route for browsing freely from the drawer.
        fun createRoute(): String {
            // We only need to provide the selectionMode argument.
            // mainCat and subCat will be null.
            return "browse?selectionMode=false"
        }

        // This overloaded helper creates the route for selecting an item from the builder.
        fun createRoute(mainCat: String, subCat: String? = null): String {
            return if (subCat != null) {
                // Provide all arguments.
                "browse?selectionMode=true&mainCat=$mainCat&subCat=$subCat"
            } else {
                // Provide selectionMode and mainCat. subCat will be null.
                "browse?selectionMode=true&mainCat=$mainCat"
            }
        }
    }
    object MySets : Screen("my_sets", "My sets")
    object SetBuilder : Screen("set_builder/{setId}", "Edit set") {
        // Function to create the route with an argument
        fun createRoute(setId: String?) = if (setId != null) "set_builder/$setId" else "set_builder/new"
    }

    // A simpler route for creating a new set
    object NewSetBuilder : Screen("set_builder/new", "New set")
}