package sk.inventory.api

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sk.inventory.utils.PreferencesManager
import java.security.cert.X509Certificate
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val BASE_URL = "https://10.10.1.137:5163/"
    private var context: Context? = null

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, java.security.SecureRandom())
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        println("Retrofit Log: $message") // Логирование в консоль
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty() || !token.contains(".")) return true  // Базовая проверка валидности JWT

        // Логируем токен для диагностики (удалите в production)
        Log.d("RetrofitClient", "Token for parsing: $token")

        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("RetrofitClient", "Invalid JWT structure: ${parts.size} parts")
                return true
            }

            val payload = parts[1]  // Вторая часть JWT — payload
            Log.d("RetrofitClient", "Payload: $payload")

            // Правильный padding для Base64 URL-safe
            val normalizedPayload = when (payload.length % 4) {
                0 -> payload
                2 -> payload + "=="
                3 -> payload + "="
                else -> {
                    Log.e("RetrofitClient", "Invalid base64 length: ${payload.length}")
                    return true
                }
            }

            val decodedBytes = Base64.decode(normalizedPayload, Base64.URL_SAFE)
            Log.d("RetrofitClient", "Decoded payload length: ${decodedBytes.size}")

            val jsonString = String(decodedBytes)
            Log.d("RetrofitClient", "JSON payload: $jsonString")

            val json = JsonParser.parseString(jsonString).asJsonObject
            val expJson = json.getAsJsonPrimitive("exp")  // Используем exp вместо iat
            if (expJson == null) {
                Log.e("RetrofitClient", "No 'exp' field in token")
                return true
            }

            val exp = expJson.asLong * 1000  // exp в секундах * 1000 = ms
            val now = Date().time
            val expired = now > exp
            Log.d("RetrofitClient", "Token exp: $exp, now: $now, expired: $expired")

            expired
        } catch (e: Exception) {
            Log.e("RetrofitClient", "Token parse error: ${e.message}", e)
            true  // Если ошибка — считаем истёкшим
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = context?.let { PreferencesManager.getToken(it) }
        val request = if (token != null && !isTokenExpired(token)) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        // Проверка на истечение токена после запроса (только если токен был добавлен)
        if (response.code == 401 && token != null && isTokenExpired(token)) {
            context?.let {
                PreferencesManager.clear(it)  // Очистить токен и роль
                Log.w("RetrofitClient", "Token expired after 1 hour, performing logoff")
            }
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
                .create()
        ))
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    fun create(context: Context): ApiService {
        if (this.context == null) {
            initialize(context)
        }
        // Проверка истечения токена при создании клиента
        val token = PreferencesManager.getToken(context)
        if (token != null) {
            if (isTokenExpired(token)) {
                PreferencesManager.clear(context)
                Log.w("RetrofitClient", "Token expired on initialization, performing logoff")
            } else {
                Log.d("RetrofitClient", "Token is valid")
            }
        } else {
            Log.d("RetrofitClient", "No token found")
        }
        return apiService
    }
}