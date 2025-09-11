package sk.inventory.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import kotlinx.coroutines.launch
import sk.inventory.ui.theme.InventoryTheme
import sk.inventory.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreen(navController: NavController, drawerState: DrawerState, scope: CoroutineScope) {
    InventoryTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") }, // Минимальный пустой title
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Открыть меню"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                val context = navController.context as Context
                                PreferencesManager.clear(context)
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Выход"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Главная страница",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}