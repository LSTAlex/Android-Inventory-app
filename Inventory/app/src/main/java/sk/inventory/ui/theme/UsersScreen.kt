package sk.inventory.ui.theme

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.models.UserResponseDto
import sk.inventory.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(navController: NavController) {
    val context = LocalContext.current
    val role = PreferencesManager.getRole(context)
    if (role != "SAdmin") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Доступ запрещён. Только для SAdmin.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
        return
    }

    var users by remember { mutableStateOf<List<UserResponseDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf<UserResponseDto?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val apiService = RetrofitClient.create(context)
                val response = apiService.getUsers()
                Log.d("UsersScreen", "Response code: ${response.code()}")
                Log.d("UsersScreen", "Response body: ${response.body()}")
                if (response.isSuccessful) {
                    users = response.body()?.filter { it.userID != 0 } ?: emptyList()
                    Log.d("UsersScreen", "Users loaded: $users")
                    errorMessage = null
                } else {
                    errorMessage = "Ошибка загрузки пользователей: ${response.code()} - ${response.errorBody()?.string()}"
                    Log.d("UsersScreen", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.message}"
                Log.e("UsersScreen", "Exception: ${e.message}", e)
            } finally {
                isLoading = false
                Log.d("UsersScreen", "Loading finished, users: $users")
            }
        }
    }

    // Диалоговое окно подтверждения удаления
    showDialog?.let { userToDelete ->
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text("Подтверждение удаления") },
            text = { Text("Вы уверены, что хотите удалить пользователя ${userToDelete.username ?: "ID ${userToDelete.userID}"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val apiService = RetrofitClient.create(context)
                                val response = apiService.deleteUser(userToDelete.userID)
                                if (response.isSuccessful) {
                                    users = users.filter { it.userID != userToDelete.userID }
                                    errorMessage = "Пользователь ${userToDelete.username ?: "ID ${userToDelete.userID}"} удалён"
                                } else {
                                    errorMessage = when (response.code()) {
                                        404 -> "Пользователь не найден"
                                        400 -> response.errorBody()?.string() ?: "Ошибка удаления"
                                        else -> "Ошибка удаления: ${response.code()}"
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка: ${e.message}"
                            }
                            showDialog = null
                        }
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление пользователями") },
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
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            } else if (users.isEmpty()) {
                Text(
                    text = "Нет пользователей",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // Шапка таблицы
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            "ID",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Имя пользователя",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Роль",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Действия",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Список пользователей
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(users, key = { it.userID }) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    user.userID.toString(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    user.username ?: "-",
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    user.roleName ?: "-",
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                if (user.username?.toLowerCase() == "admin") {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    IconButton(
                                        onClick = { showDialog = user },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Удалить пользователя"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Отображение ошибок или сообщений
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    it,
                    color = if (it.contains("удалён")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}