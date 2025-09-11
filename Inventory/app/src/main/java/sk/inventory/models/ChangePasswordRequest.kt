package sk.inventory.models

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)