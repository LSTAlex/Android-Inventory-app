package sk.inventory.ui.theme

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerMenu(drawerState: DrawerState, scope: CoroutineScope, onMenuItemClick: (String) -> Unit) {
    ModalDrawerSheet {
        Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)

        val context = LocalContext.current

        val menuItems = listOf(
            MenuItem("Создать рабочее место", Icons.Default.Add),
            MenuItem("Удалить рабочее место", Icons.Default.Delete),
            MenuItem("Изменить рабочее место", Icons.Default.Edit),
            MenuItem("Найти", Icons.Default.Search),
            MenuItem("Рабочие места", Icons.Default.List)
        )

        menuItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false,
                onClick = {
                    scope.launch {
                        drawerState.close()
                        onMenuItemClick(item.label)
                        if (item.label != "Создать рабочее место" && item.label != "Удалить рабочее место") {
                            Toast.makeText(context, "Функция в разработке", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

data class MenuItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)