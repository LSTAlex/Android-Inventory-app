package sk.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import sk.inventory.ui.screens.*
import sk.inventory.ui.theme.DrawerMenu
import sk.inventory.ui.theme.InventoryTheme
import sk.inventory.utils.PreferencesManager
import sk.inventory.api.RetrofitClient
import sk.inventory.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(this)

        setContent {
            InventoryTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Устанавливаем начальную роль из Preferences
                LaunchedEffect(Unit) {
                    val role = PreferencesManager.getRole(this@MainActivity)
                    viewModel.setRole(role)
                }

                val currentRole by viewModel::currentRole

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerMenu(
                            drawerState = drawerState,
                            scope = scope,
                            currentRole = currentRole
                        ) { selectedItem ->
                            scope.launch {
                                drawerState.close()
                                when (selectedItem) {
                                    "Создать рабочее место" -> navController.navigate("create_workspace")
                                    "Удалить рабочее место" -> navController.navigate("deleteWorkspace")
                                    "Изменить рабочее место" -> navController.navigate("editWorkspace")
                                    "Найти" -> navController.navigate("find_workspace")
                                    "Рабочие места" -> navController.navigate("workplaces")
                                    "Создать пользователя" -> navController.navigate("create_user")
                                }
                            }
                        }
                    }
                ) {
                    Scaffold { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = if (PreferencesManager.getToken(this@MainActivity) != null) "main" else "login",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("login") { LoginScreen(navController, viewModel) }
                            composable("main") { MainScreen(navController, drawerState, scope) }
                            composable("create_workspace") { CreateWorkspaceScreen(navController) }
                            composable("deleteWorkspace") { DeleteWorkspaceScreen(navController) }
                            composable("editWorkspace") { EditWorkspaceScreen(navController) }
                            composable("find_workspace") { FindWorkspaceScreen(navController) }
                            composable("workplaces") { WorkplacesScreen(navController) }
                            composable("create_user") { CreateUserScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}
