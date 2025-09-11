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

    // Ключи для SharedPreferences
    private const val KEY_TOKEN_ISSUED_AT = "token_issued_at"

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
        if (token.isNullOrEmpty()) return true
        return try {
            val payload = token.split(".")[1]  // Вторая часть JWT — payload
            val normalizedPayload = payload.padEnd((payload.length / 4 * 4) + 4, '=')  // Padding для Base64
            val decodedBytes = Base64.decode(normalizedPayload, Base64.URL_SAFE or Base64.NO_WRAP)
            val json = JsonParser.parseString(String(decodedBytes)).asJsonObject
            val iat = json.getAsJsonPrimitive("iat").asLong * 1000  // Issued at в ms
            val now = Date().time
            now > (iat + 60 * 60 * 1000)  // 1 час = 3600000 ms
        } catch (e: Exception) {
            Log.e("RetrofitClient", "Token parse error", e)
            true  // Если ошибка — считаем истёкшим
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = context?.let { PreferencesManager.getToken(it) }
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        // Проверка на истечение токена
        if (token != null && isTokenExpired(token)) {
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
        if (token != null && isTokenExpired(token)) {
            PreferencesManager.clear(context)
            Log.w("RetrofitClient", "Token expired on initialization, performing logoff")
        }
        return apiService
    }
}