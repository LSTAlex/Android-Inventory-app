package sk.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import sk.inventory.ui.theme.DrawerMenu
import sk.inventory.ui.screens.LoginScreen
import sk.inventory.ui.screens.MainScreen
import sk.inventory.ui.screens.CreateWorkspaceScreen
import sk.inventory.ui.screens.DeleteWorkspaceScreen
import sk.inventory.ui.screens.EditWorkspaceScreen // Новый экран
import sk.inventory.utils.PreferencesManager
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.platform.LocalContext
import sk.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.ui.screens.ChangePasswordScreen
import sk.inventory.ui.screens.FindWorkspaceScreen
import sk.inventory.ui.screens.WorkplacesScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.initialize(this)
        setContent {
            InventoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    // Проверка токена при запуске
                    LaunchedEffect(Unit) {
                        RetrofitClient.create(context) // Это вызовет проверку истечения токена
                        startDestination = if (PreferencesManager.getToken(context) != null) "main" else "login"
                    }

                    val currentStartDestination = startDestination ?: "login"

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerMenu(drawerState = drawerState, scope = scope) { selectedItem ->
                                scope.launch {
                                    drawerState.close()
                                    when (selectedItem) {
                                        "Создать рабочее место" -> navController.navigate("create_workspace")
                                        "Удалить рабочее место" -> navController.navigate("deleteWorkspace")
                                        "Изменить рабочее место" -> navController.navigate("editWorkspace") // Новая навигация
                                        "Найти" -> navController.navigate("find_workspace")
                                        "Рабочие места" -> navController.navigate("workplaces")

                                    }
                                }
                            }
                        }
                    ) {
                        Scaffold { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = currentStartDestination,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable("login") { LoginScreen(navController) }
                                composable("main") { MainScreen(navController, drawerState, scope) }
                                composable("create_workspace") { CreateWorkspaceScreen(navController) }
                                composable("deleteWorkspace") { DeleteWorkspaceScreen(navController) }
                                composable("editWorkspace") { EditWorkspaceScreen(navController) }
                                composable("find_workspace") { FindWorkspaceScreen(navController) }
                                composable("workplaces") { WorkplacesScreen(navController) }
                                composable("change_password") { ChangePasswordScreen(navController) }
                            }
                        }
                    }
                }
            }
        }
    }
}