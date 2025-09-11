package sk.inventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteWorkspaceScreen(navController: NavController) {
    val context = LocalContext.current
    val role = PreferencesManager.getRole(context)
    if (role != "SAdmin" && role != "Admin") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Доступ запрещён. Только для SAdmin и Admin.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
        return
    }

    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Удалить рабочее место") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя рабочего места") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val apiService = RetrofitClient.create(context)
                            val response = apiService.deleteWorkplaceByName(name)
                            if (response.isSuccessful) {
                                message = "Рабочее место '$name' успешно удалено."
                            } else {
                                message = "Рабочее место '$name' не найдено."
                            }
                        } catch (e: Exception) {
                            message = "Ошибка: ${e.message}"
                        }
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Удалить")
            }

            message?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = if (it.contains("успешно")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }
}