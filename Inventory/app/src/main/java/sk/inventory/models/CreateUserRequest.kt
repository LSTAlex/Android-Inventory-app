package sk.inventory.models

data class CreateUserRequest(
    val username: String,
    val password: String,
    val roleName: String,
    val mustChangePassword: Boolean = true
)