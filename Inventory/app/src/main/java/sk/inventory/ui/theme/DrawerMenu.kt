package sk.inventory.ui.theme

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerMenu(
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentRole: String?,
    onMenuItemClick: (String) -> Unit
) {
    ModalDrawerSheet {
        Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)

        val context = LocalContext.current
        Log.d("DrawerMenu", "Rendering menu with role: $currentRole")

        val menuItems = buildList {
            // Вкладка "Найти" доступна всем ролям
            add(MenuItem("Найти", Icons.Default.Search))

            // Вкладки для Admin и SAdmin
            if (currentRole in listOf("Admin", "SAdmin")) {
                add(MenuItem("Создать рабочее место", Icons.Default.Add))
                add(MenuItem("Удалить рабочее место", Icons.Default.Delete))
                add(MenuItem("Изменить рабочее место", Icons.Default.Edit))
                add(MenuItem("Рабочие места", Icons.Default.List))
            }

            // Вкладка "Создать пользователя" только для SAdmin
            if (currentRole == "SAdmin") {
                add(MenuItem("Создать пользователя", Icons.Default.PersonAdd))
            }
        }

        Log.d("DrawerMenu", "Generated menu items: ${menuItems.map { it.label }}")

        menuItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false,
                onClick = {
                    scope.launch {
                        onMenuItemClick(item.label)
                        if (item.label !in listOf("Найти", "Создать рабочее место", "Удалить рабочее место", "Рабочие места", "Создать пользователя")) {
                            Toast.makeText(context, "Функция в разработке", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

data class MenuItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)