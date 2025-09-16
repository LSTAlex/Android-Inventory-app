package sk.inventory.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var currentRole by mutableStateOf<String?>(null)
        private set

    fun setRole(role: String?) {
        currentRole = role
    }
}
