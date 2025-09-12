package sk.inventory.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import sk.inventory.api.RetrofitClient
import sk.inventory.models.WorkplaceResponse
import sk.inventory.models.WorkplacesPagingSource
import sk.inventory.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkplacesScreen(navController: NavController) {
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

    // Создание Pager для загрузки данных с явным управлением размером
    val lazyPagingItems = Pager(
        config = androidx.paging.PagingConfig(
            pageSize = 10,
            initialLoadSize = 10 // Установим initialLoadSize равным pageSize
        )
    ) {
        WorkplacesPagingSource(context)
    }.flow.collectAsLazyPagingItems()

    val dateFormat = remember { SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault()) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рабочие места") },
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
            // Фиксированная шапка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .widthIn(min = 1200.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        "ID",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Имя",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Описание",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Локация",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "ПК",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Монитор",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Телефон",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Кем создано",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Создано",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Данные с вертикальной прокруткой
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(lazyPagingItems) { wp ->
                    wp?.let {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .widthIn(min = 1200.dp)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    it.WorkplaceID.toString(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.Name,
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.Description,
                                    modifier = Modifier.weight(3f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.Location,
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.PC,
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.Monitor,
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.Telephone,
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.CreatedBy ?: "-",
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    it.CreatedAt?.let { dateFormat.format(it) } ?: "-",
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Индикатор загрузки
                item {
                    when (lazyPagingItems.loadState.refresh) {
                        is androidx.paging.LoadState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                        is androidx.paging.LoadState.Error -> {
                            Text(
                                text = "Ошибка загрузки: ${(lazyPagingItems.loadState.refresh as androidx.paging.LoadState.Error).error.message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                        else -> {}
                    }
                    when (lazyPagingItems.loadState.append) {
                        is androidx.paging.LoadState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                        is androidx.paging.LoadState.Error -> {
                            Text(
                                text = "Ошибка подгрузки: ${(lazyPagingItems.loadState.append as androidx.paging.LoadState.Error).error.message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}