package sk.inventory.models

data class CreateUserRequest(
    val username: String,
    val password: String,
    val roleName: String,  // Изменено на roleName вместо roleId (строка)
    val canEditWorkplace: Boolean = false,
    val mustChangePassword: Boolean = true
)