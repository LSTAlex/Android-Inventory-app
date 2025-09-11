package sk.inventory.models

data class LoginResponse(
    val token: String?,
    val role: String,
    val mustChangePassword: Boolean? = false,  // Новый флаг
    val message: String? = null,
    val success: Boolean? = true
)