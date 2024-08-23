package kr.co.lion.modigm.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class PreferenceUtil(context: Context) {

    // 암호화된 SharedPreferences 초기화
    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "encrypted_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // 정수 값을 가져오는 함수 (ex. userIdx, studyIdx 등...)
    fun getInt(key: String, defValue: Int = 0): Int =
        prefs.getInt(key, defValue)

    // 정수 값을 저장하는 함수 (ex. userIdx, studyIdx 등...)
    fun setInt(key: String, value: Int) =
        prefs.edit().putInt(key, value).apply()

    // 문자열 값을 가져오는 함수 (ex. userUid, userName 등...)
    fun getString(key: String, defValue: String = ""): String =
        prefs.getString(key, defValue) ?: defValue

    // 문자열 값을 저장하는 함수 (ex. userUid, userName 등...)
    fun setString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()

    // Boolean 값을 저장하는 함수 (ex. autoLogin 등...)
    fun setBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    // Boolean 값을 가져오는 함수 (ex. autoLogin 등...)
    fun getBoolean(key: String, defValue: Boolean = false): Boolean =
        prefs.getBoolean(key, defValue)

    // 특정 키에 해당하는 값을 삭제하는 함수 (ex. 해당 키 값을 가진 자료'만' 삭제해야 할 경우 등...)
    fun clearOnePrefs(key: String) =
        prefs.edit().remove(key).apply()

    // 모든 SharedPreferences 값을 삭제하는 함수 (ex. 로그아웃, 에러 발생 시 처리 등...)
    fun clearAllPrefs() =
        prefs.edit().clear().apply()

    // 모든 SharedPreferences 값을 로그에 출력하는 함수 (개발 확인용)
    fun logAllPreferences() {
        prefs.all.forEach { (key, value) ->
            Log.d("SharedPreferencesLog", "$key: $value")
        }
    }

    fun getAllPrefs(): String {
        val allPrefs = prefs.all
        val stringBuilder = StringBuilder()
        for ((key, value) in allPrefs) {
            stringBuilder.append("$key: $value")
        }
        return stringBuilder.toString()
    }
}
