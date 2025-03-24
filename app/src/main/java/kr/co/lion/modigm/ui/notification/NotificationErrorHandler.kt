package kr.co.lion.modigm.ui.notification
import android.content.Context
import android.util.Log
import android.widget.Toast
import org.apache.http.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NotificationErrorHandler {
    fun handleException(context: Context, e: Exception, logTag: String) {
        when (e) {
            is IOException -> { // 네트워크 오류
                Log.e(logTag, "Network error: ${e.message}", e)
                showErrorMessage(context, "인터넷 연결을 확인해주세요.")
            }

            is HttpException -> { // 서버 오류
                Log.e(logTag, "Server error: ${e.message}", e)
                showErrorMessage(context, "서버 응답이 없습니다. 잠시 후 다시 시도해주세요.")
            }

            is SocketTimeoutException -> { // 서버 응답 지연
                Log.e(logTag, "SocketTimeoutException: ${e.message}", e)
                showErrorMessage(context, "서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.")
            }

            is UnknownHostException -> { // 서버 찾을 수 없음
                Log.e(logTag, "UnknownHostException: ${e.message}", e)
                showErrorMessage(context, "네트워크 연결이 원활하지 않습니다.")
            }

            else -> { // 기타 오류
                Log.e(logTag, "Unexpected error: ${e.message}", e)
                showErrorMessage(context, "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    private fun showErrorMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}