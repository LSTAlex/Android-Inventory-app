package sk.inventory.ui.theme

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.models.WorkplaceCreateDto
import sk.inventory.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkspaceScreen(navController: NavController) {
    val context = LocalContext.current
    val role = PreferencesManager.getRole(context)
    if (role != "SAdmin" && role != "Admin") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Доступ запрещён.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
        return
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var pc by remember { mutableStateOf("") }
    var monitor by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var qrCodeBytes by remember { mutableStateOf<ByteArray?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isFormValid = name.isNotBlank() && location.isNotBlank() && pc.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать рабочее место") },
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя (обязательное поле)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Локация (обязательное поле)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = pc,
                onValueChange = { pc = it },
                label = { Text("ПК (обязательное поле)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = monitor,
                onValueChange = { monitor = it },
                label = { Text("Монитор") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = telephone,
                onValueChange = { telephone = it },
                label = { Text("Телефон") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val dto = WorkplaceCreateDto(
                            Name = name,
                            Description = description,
                            Location = location,
                            PC = pc,
                            Monitor = monitor,
                            Telephone = telephone
                        )
                        try {
                            val apiService = RetrofitClient.create(context)
                            val response = apiService.createWorkplace(dto)
                            if (response.isSuccessful) {
                                val workplace = response.body()
                                workplace?.let {
                                    val qrResponse = apiService.getQrCode(it.WorkplaceID)
                                    if (qrResponse.isSuccessful) {
                                        qrCodeBytes = qrResponse.body()?.bytes()
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Ошибка загрузки QR-кода: ${qrResponse.message()}"
                                    }
                                }
                            } else {
                                when (response.code()) {
                                    409 -> {
                                        errorMessage = "Рабочее место с именем '$name' уже существует."
                                    }
                                    else -> {
                                        errorMessage = "Ошибка создания: ${response.errorBody()?.string()}"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            }

            qrCodeBytes?.let { bytes ->
                Spacer(modifier = Modifier.height(16.dp))
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR-код",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally) // Центрирование QR-кода
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "QR Code", null)
                            val uri = Uri.parse(path)

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Поделиться QR-кодом"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Поделиться")
                    }
                    Button(
                        onClick = {
                            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "QR Code", "QR code for workplace")
                            Toast.makeText(context, "QR-код сохранён в галерею", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}