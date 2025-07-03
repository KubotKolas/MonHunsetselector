package com.kubot.monhunsetselector.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kubot.monhunsetselector.auth.AuthManager
import com.kubot.monhunsetselector.data.models.User
import com.kubot.monhunsetselector.data.models.UserSet
import com.kubot.monhunsetselector.navigation.Screen
import com.kubot.monhunsetselector.ui.viewmodel.MySetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySetsScreen(
    navController: NavController,
    user: User,
    authManager: AuthManager,
    onMenuClick: () -> Unit
) {

    val viewModel: MySetsViewModel = viewModel()
    val mySets by viewModel.mySets.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())


    LaunchedEffect(Unit) {
        viewModel.loadMySets()
    }


    rememberPullToRefreshState()



    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("My Sets") },
                navigationIcon = {

                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Navigation Drawer")
                    }
                },
                actions = {

                    IconButton(onClick = { authManager.logout() }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshMySets() },
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        )
        {


            LazyColumn(
                modifier = Modifier


                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Welcome, ${user.displayName}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (mySets.isEmpty() && !isRefreshing) {
                    item {
                        Text(
                            text = "You have no saved sets. Create one from the drawer menu!",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
                items(mySets) { set ->
                    SetCard(set = set, onClick = {
                        navController.navigate(Screen.SetBuilder.createRoute(set.id))
                    })
                }
            }


        }


    }
}

@Composable
fun SetCard(set: UserSet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(set.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = set.weaponName ?: "No weapon selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}