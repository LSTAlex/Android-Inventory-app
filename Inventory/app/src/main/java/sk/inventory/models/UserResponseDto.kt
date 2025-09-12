package sk.inventory.models

import com.google.gson.annotations.SerializedName

data class UserResponseDto(
    @SerializedName("userID") val userID: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("roleName") val roleName: String?
)