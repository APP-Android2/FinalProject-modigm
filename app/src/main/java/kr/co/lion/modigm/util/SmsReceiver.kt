package kr.co.lion.modigm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsReceiver: BroadcastReceiver() {

    companion object {
        private const val PATTERN = "\\d{6}"
    }

    interface SmsReceiverListener {
        fun onMessageReceived(message: String)
    }

    private var smsListener: SmsReceiverListener? = null

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
                        Log.d("test1234", "onReceive1 : $authCode")
                        if(smsListener != null && authCode != null){
                            Log.d("test1234", "onReceive2 : $authCode")
                            smsListener!!.onMessageReceived(authCode)
                        }
                    }
                }

            }
        }
    }

    fun setListener(listener: SmsReceiverListener) {
        this.smsListener = listener
    }

    fun doFilter() = IntentFilter().apply {
        addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
    }

    private fun getAuthCode(message: String): String? = PATTERN.toRegex().find(message)?.value
}