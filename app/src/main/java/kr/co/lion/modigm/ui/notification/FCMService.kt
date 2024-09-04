package kr.co.lion.modigm.ui.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.content.Context
import java.io.InputStream
import com.google.auth.oauth2.GoogleCredentials
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import java.util.Properties

object FCMService {
    private const val TAG = "FCMService"
    private const val FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/modigm-4afde/messages:send"
    private const val SERVER_KEY = "AIzaSyBhtMRcNXcMdCGIbJqzCFqS8Q-dpr-ga74" // Replace this with your actual server key

    private val client = OkHttpClient()

    suspend fun sendNotificationToToken(context: Context, token: String, title: String, body: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context) // 올바른 Access Token을 가져옵니다.
                if (accessToken == null) {
                    Log.e(TAG, "Failed to obtain access token.")
                    return@withContext false
                }

                val jsonBody = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", body)
                        })
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(FCM_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken") // Access Token 사용
                    .addHeader("Content-Type", "application/json; UTF-8")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    Log.d(TAG, "Notification sent successfully: ${response.body?.string()}")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending push notification", e)
                return@withContext false
            }
        }
    }

    fun getAccessToken(context: Context): String? {
        return try {
            val jsonContent = """
            {
              "type": "${BuildConfig.SERVICE_ACCOUNT_TYPE}",
              "project_id": "${BuildConfig.PROJECT_ID}",
              "private_key_id": "${BuildConfig.PRIVATE_KEY_ID}",
              "private_key": "${BuildConfig.PRIVATE_KEY}",
              "client_email": "${BuildConfig.CLIENT_EMAIL}",
              "client_id": "${BuildConfig.CLIENT_ID}",
              "auth_uri": "${BuildConfig.AUTH_URI}",
              "token_uri": "${BuildConfig.TOKEN_URI}",
              "auth_provider_x509_cert_url": "${BuildConfig.AUTH_PROVIDER_X509_CERT_URL}",
              "client_x509_cert_url": "${BuildConfig.CLIENT_X509_CERT_URL}"
            }
        """.trimIndent()

            val inputStream = jsonContent.byteInputStream()
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired() // 토큰이 만료된 경우 새로고침

            credentials.accessToken.tokenValue // 액세스 토큰 반환
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining access token", e)
            null
        }
    }

}