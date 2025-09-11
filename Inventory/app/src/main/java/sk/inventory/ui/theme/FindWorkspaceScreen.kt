package sk.inventory.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sk.inventory.api.RetrofitClient
import sk.inventory.models.WorkplaceResponse
import sk.inventory.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindWorkspaceScreen(navController: NavController) {
    val context = LocalContext.current
    val role = PreferencesManager.getRole(context)
    if (role !in listOf("SAdmin", "Admin", "User")) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Доступ запрещён. Только для SAdmin, Admin и User.")
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
    var qrCodeBytes by remember { mutableStateOf<ByteArray?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Найти рабочее место") },
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
        // Основная Column с прокруткой для всего контента
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                                Log.d("FindWorkspaceScreen", "Raw response: ${response.raw()}")
                                Log.d("FindWorkspaceScreen", "Deserialized workplace: $workplace")
                                if (workplace == null) {
                                    errorMessage = "Ошибка десериализации данных"
                                    qrCodeBytes = null
                                } else {
                                    val qrCode = workplace!!.QrCode
                                    qrCodeBytes = if (qrCode != null) {
                                        try {
                                            val decodedBytes = Base64.decode(qrCode, Base64.DEFAULT)
                                            Log.d("FindWorkspaceScreen", "Decoded QR code length: ${decodedBytes.size}")
                                            decodedBytes
                                        } catch (e: IllegalArgumentException) {
                                            errorMessage = "Ошибка декодирования QR-кода: ${e.message}"
                                            Log.e("FindWorkspaceScreen", "Base64 decode error", e)
                                            null
                                        }
                                    } else {
                                        errorMessage = "QR-код отсутствует в ответе"
                                        null
                                    }
                                }
                            } else {
                                errorMessage = "Компьютера с таким именем нет"
                                workplace = null
                                qrCodeBytes = null
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                            Log.e("FindWorkspaceScreen", "Network error", e)
                            workplace = null
                            qrCodeBytes = null
                        }
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Найти")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Отображение данных рабочего места
            workplace?.let { wp ->
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("ID: ")
                                }
                                append(wp.WorkplaceID.toString())
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Имя: ")
                                }
                                append(wp.Name)
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Описание: ")
                                }
                                append(wp.Description)
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Локация: ")
                                }
                                append(wp.Location)
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("ПК: ")
                                }
                                append(wp.PC)
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Монитор: ")
                                }
                                append(wp.Monitor)
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Телефон: ")
                                }
                                append(wp.Telephone)
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Создано: ")
                                }
                                append(wp.CreatedAt?.let { dateFormat.format(it) } ?: "-")
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                    append("Создано кем: ")
                                }
                                append(wp.CreatedBy ?: "-")
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Карточка QR-кода отображается только после запроса
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "QR-код",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (qrCodeBytes != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val bitmap = BitmapFactory.decodeByteArray(qrCodeBytes!!, 0, qrCodeBytes!!.size)
                                Log.d("FindWorkspaceScreen", "Bitmap decode result: ${bitmap != null}, Size: ${bitmap?.width}x${bitmap?.height}")
                                if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "QR-код рабочего места",
                                        modifier = Modifier.size(150.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = {
                                                val saveBitmap = BitmapFactory.decodeByteArray(qrCodeBytes!!, 0, qrCodeBytes!!.size)
                                                if (saveBitmap != null) {
                                                    MediaStore.Images.Media.insertImage(
                                                        context.contentResolver,
                                                        saveBitmap,
                                                        "QR Code",
                                                        "QR code for workplace"
                                                    )
                                                    Toast.makeText(context, "QR-код сохранён в галерею", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = "Сохранить")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Сохранить")
                                        }
                                        Button(
                                            onClick = {
                                                val shareBitmap = BitmapFactory.decodeByteArray(qrCodeBytes!!, 0, qrCodeBytes!!.size)
                                                if (shareBitmap != null) {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "image/*"
                                                        putExtra(
                                                            Intent.EXTRA_STREAM,
                                                            Uri.parse(
                                                                MediaStore.Images.Media.insertImage(
                                                                    context.contentResolver,
                                                                    shareBitmap,
                                                                    "QR Code",
                                                                    null
                                                                )
                                                            )
                                                        )
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Поделиться QR-кодом"))
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Поделиться")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Поделиться")
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Bitmap пустой или повреждён", color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("QR-код не доступен: $errorMessage", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}