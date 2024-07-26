package kr.co.lion.modigm.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.UserData

class PreferenceUtil(context: Context) {

    private val prefs: SharedPreferences
    private val gson = Gson()

    init {
        // 마스터 키 생성 또는 가져오기
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // EncryptedSharedPreferences 생성
        prefs = EncryptedSharedPreferences.create(
            "encrypted_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue) ?: defValue
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    // 유저 정보를 SharedPreferences에 저장
    fun setUserData(key: String, user: SqlUserData) {
        Log.d("currentUserData",user.toString())
        val userJson = gson.toJson(user)
        prefs.edit().putString(key, userJson).apply()
    }

    // SharedPreferences에서 유저 정보를 가져옴
    fun getUserData(key: String): UserData? {
        val userJson = prefs.getString(key, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserData::class.java)
        } else {
            null
        }
    }

    // 자동 로그인 설정 저장
    fun setAutoLogin(autoLogin: Boolean) {
        prefs.edit().putBoolean("autoLogin", autoLogin).apply()
    }

    // 자동 로그인 설정 불러오기
    fun getAutoLogin(): Boolean {
        return prefs.getBoolean("autoLogin", false)
    }

    fun clearUserData(key: String) {
        prefs.edit().remove(key).apply()
    }

    // 모든 SharedPreferences 데이터를 로그로 출력
    fun logAllPreferences() {
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            Log.d("SharedPreferencesLog", "$key: $value")
        }
    }
}
