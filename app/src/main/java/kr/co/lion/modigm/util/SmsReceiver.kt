package kr.co.lion.modigm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Telephony
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsReceiver: BroadcastReceiver() {

    companion object {
        private const val PATTERN = "^modigm-4afde\\.firebaseapp\\.com"
    }

    interface SmsReceiverListener {
        fun onMessageReceived(message: String)
    }

    private var smsListener: SmsReceiverListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
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
                        if(smsListener != null && sms.isNotEmpty()){
                            val check = PATTERN.toRegex().find(sms)?.destructured?.component1()
                            if(!check.isNullOrEmpty()){
                                Log.d("test1234", "$check")
                                smsListener!!.onMessageReceived(check)
                            }
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
}