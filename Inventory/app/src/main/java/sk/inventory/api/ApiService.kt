package sk.inventory.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import sk.inventory.models.ChangePasswordRequest
import sk.inventory.models.CreateUserRequest
import sk.inventory.models.LoginRequest
import sk.inventory.models.LoginResponse
import sk.inventory.models.WorkplaceCreateDto
import sk.inventory.models.WorkplaceResponse

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/change-initial-password")
    suspend fun changeInitialPassword(@Body request: ChangePasswordRequest): Response<LoginResponse>

    @POST("api/workplace")
    suspend fun createWorkplace(@Body dto: WorkplaceCreateDto): Response<WorkplaceResponse>

    @GET("api/workplace/{id}/qr")
    suspend fun getQrCode(@Path("id") id: Int): Response<ResponseBody>

    @DELETE("api/workplace/{id}")
    suspend fun deleteWorkplace(@Path("id") id: Int): Response<Unit>

    @DELETE("api/workplace/name/{name}")
    suspend fun deleteWorkplaceByName(@Path("name") name: String): Response<Unit>

    @GET("/api/workplace/getname/{name}")
    suspend fun getWorkplaceByName(@Path("name") name: String): Response<WorkplaceResponse>

    @PUT("/api/workplace/{id}")
    suspend fun updateWorkplace(@Path("id") id: Int, @Body dto: WorkplaceCreateDto): Response<Unit>

    @GET("/api/workplace")
    suspend fun getWorkplaces(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<Map<String, Any?>> // Возвращает JSON с total, page, data

    @POST("api/auth/register")
    suspend fun registerUser(@Body dto: CreateUserRequest): Response<Void>
}