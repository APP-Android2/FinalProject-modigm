package kr.co.lion.modigm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmsReceiver: BroadcastReceiver() {

    companion object {
        private const val PATTERN = "\\d{6}"

        private val _smsCode = MutableStateFlow("")
        val smsCode = _smsCode.asStateFlow()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            intent.extras?.let { bundle ->
                val status = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    bundle.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java) as Status
                }else{
                    bundle.get(SmsRetriever.EXTRA_STATUS) as Status
                }
                when(status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // 받아온 문자 내용
                        val sms = bundle.getString(SmsRetriever.EXTRA_SMS_MESSAGE, "")
                        val authCode = getAuthCode(sms)
                        CoroutineScope(Dispatchers.Main).launch {
                            authCode?.let {
                                _smsCode.emit(it)
                            }
                        }
                    }
                    else -> {}
                }

            }
        }
    }

    fun doFilter() = IntentFilter().apply {
        addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
    }

    private fun getAuthCode(message: String): String? = PATTERN.toRegex().find(message)?.value
}