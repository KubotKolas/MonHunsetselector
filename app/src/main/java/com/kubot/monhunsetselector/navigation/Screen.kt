package com.kubot.monhunsetselector.navigation

sealed class Screen(val route: String, val title: String) {
    object Loading : Screen("loading", "Loading")
    object Login : Screen("login", "Login")
    data object Browse :
        Screen("browse?selectionMode={selectionMode}&mainCat={mainCat}&subCat={subCat}", "Browse") {


        fun createRoute(): String {


            return "browse?selectionMode=false"
        }


        fun createRoute(mainCat: String, subCat: String? = null): String {
            return if (subCat != null) {

                "browse?selectionMode=true&mainCat=$mainCat&subCat=$subCat"
            } else {

                "browse?selectionMode=true&mainCat=$mainCat"
            }
        }
    }

    object MySets : Screen("my_sets", "My sets")
    object SetBuilder : Screen("set_builder/{setId}", "Edit set") {

        fun createRoute(setId: String?) =
            if (setId != null) "set_builder/$setId" else "set_builder/new"
    }


    object NewSetBuilder : Screen("set_builder/new", "New set")
}