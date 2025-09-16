package sk.inventory.ui.theme

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.models.ChangePasswordRequest
import sk.inventory.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    val context = LocalContext.current
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val username = PreferencesManager.getTempUser(context) ?: "admin"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Смените пароль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Первый вход для пользователя '$username'. Введите текущий пароль и новый.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            TextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Текущий пароль") },
                visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { currentVisible = !currentVisible }) {
                        Icon(
                            imageVector = if (currentVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Новый пароль (минимум 6 символов)") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердите пароль") },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (currentPassword != "admin123") {
                        errorMessage = "Текущий пароль должен быть 'admin123'"
                        return@Button
                    }
                    if (newPassword.length < 6) {
                        errorMessage = "Пароль должен быть минимум 6 символов"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Пароли не совпадают"
                        return@Button
                    }
                    coroutineScope.launch {
                        try {
                            val apiService = RetrofitClient.create(context)
                            val request = ChangePasswordRequest(
                                currentPassword = currentPassword,  // Добавьте в модель, если нужно
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                            val response = apiService.changeInitialPassword(request)
                            if (response.isSuccessful) {
                                val result = response.body()
                                result?.let {
                                    PreferencesManager.saveToken(context, it.token ?: "")
                                    PreferencesManager.saveRole(context, it.role)
                                    PreferencesManager.clearTempUser(context)
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    Toast.makeText(context, "Пароль успешно изменён", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                errorMessage = "Ошибка смены пароля: ${response.errorBody()?.string()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    }
                },
                enabled = currentPassword == "admin123" && newPassword.length >= 6 && newPassword == confirmPassword,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сменить пароль")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}