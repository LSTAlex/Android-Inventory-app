package sk.inventory.models

import com.google.gson.annotations.SerializedName
import java.util.Date


data class WorkplaceCreateDto(
    val Name: String,
    val Description: String,
    val Location: String,
    val PC: String,
    val Monitor: String,
    val Telephone: String
)

data class WorkplaceResponse(
    @SerializedName("workplaceID") val WorkplaceID: Int,
    @SerializedName("name") val Name: String,
    @SerializedName("description") val Description: String,
    @SerializedName("location") val Location: String,
    @SerializedName("pc") val PC: String,
    @SerializedName("monitor") val Monitor: String,
    @SerializedName("telephone") val Telephone: String,
    @SerializedName("createdAt") val CreatedAt: Date?,
    @SerializedName("createdBy") val CreatedBy: String?,
    @SerializedName("qrcode") val QrCode: String?
)