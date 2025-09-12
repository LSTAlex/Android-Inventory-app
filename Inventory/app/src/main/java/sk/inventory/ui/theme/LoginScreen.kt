package sk.inventory.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.models.LoginRequest
import sk.inventory.utils.PreferencesManager
import sk.inventory.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = username, onValueChange = { username = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val apiService = RetrofitClient.create(context)
                        val response = apiService.login(LoginRequest(username, password))
                        if (response.isSuccessful) {
                            val body = response.body()
                            body?.let {
                                PreferencesManager.saveToken(context, it.token ?: "")
                                PreferencesManager.saveRole(context, it.role)
                                viewModel.setRole(it.role)  // <- обновляем роль для MainActivity
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Неверные credentials", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти")
        }
    }
}


private suspend fun performLogin(context: Context, username: String, password: String, navController: NavController) {
    try {
        val apiService = RetrofitClient.create(context)
        val response = apiService.login(LoginRequest(username, password))
        if (response.isSuccessful) {
            val loginResponse = response.body()
            loginResponse?.let {
                if (it.mustChangePassword == true) {
                    // Сохраните username для смены пароля
                    PreferencesManager.saveTempUser(context, username)
                    navController.navigate("change_password") {
                        popUpTo("login") { inclusive = true }
                    }
                    Toast.makeText(context, "Первый вход: смените пароль", Toast.LENGTH_LONG).show()
                } else {
                    PreferencesManager.saveToken(context, it.token ?: "")
                    Log.d("LoginScreen", "Saving role: ${it.role}")
                    PreferencesManager.saveRole(context, it.role)
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        } else {
            Toast.makeText(context, "Неверные credentials", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}