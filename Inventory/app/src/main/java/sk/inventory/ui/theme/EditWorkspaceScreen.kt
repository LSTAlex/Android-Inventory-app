package sk.inventory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import sk.inventory.models.WorkplaceResponse
import sk.inventory.models.WorkplaceCreateDto
import sk.inventory.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkspaceScreen(navController: NavController) {
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
    var workplace by remember { mutableStateOf<WorkplaceResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editPc by remember { mutableStateOf("") }
    var editMonitor by remember { mutableStateOf("") }
    var editTelephone by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Изменить рабочее место") },
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
                            val response = apiService.getWorkplaceByName(name)
                            if (response.isSuccessful) {
                                errorMessage = null
                                workplace = response.body()
                                workplace?.let {
                                    editName = it.Name ?: ""
                                    editDescription = it.Description ?: ""
                                    editLocation = it.Location ?: ""
                                    editPc = it.PC ?: ""
                                    editMonitor = it.Monitor ?: ""
                                    editTelephone = it.Telephone ?: ""
                                } ?: run {
                                    editName = ""
                                    editDescription = ""
                                    editLocation = ""
                                    editPc = ""
                                    editMonitor = ""
                                    editTelephone = ""
                                }
                            } else {
                                errorMessage = "Компьютера с таким именем нет"
                                workplace = null
                                editName = ""
                                editDescription = ""
                                editLocation = ""
                                editPc = ""
                                editMonitor = ""
                                editTelephone = ""
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                            workplace = null
                            editName = ""
                            editDescription = ""
                            editLocation = ""
                            editPc = ""
                            editMonitor = ""
                            editTelephone = ""
                        }
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Поиск")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            workplace?.let { wp ->
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Локация") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editPc,
                        onValueChange = { editPc = it },
                        label = { Text("ПК") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editMonitor,
                        onValueChange = { editMonitor = it },
                        label = { Text("Монитор") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editTelephone,
                        onValueChange = { editTelephone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val apiService = RetrofitClient.create(context)
                                    val dto = WorkplaceCreateDto(
                                        editName,
                                        editDescription,
                                        editLocation,
                                        editPc,
                                        editMonitor,
                                        editTelephone
                                    )
                                    val response = apiService.updateWorkplace(wp.WorkplaceID, dto)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Сохранено успешно", Toast.LENGTH_SHORT).show()
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Ошибка сохранения: ${response.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Ошибка: ${e.message}"
                                }
                            }
                        },
                        enabled = editName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}