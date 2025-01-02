package kr.co.lion.modigm.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kr.co.lion.modigm.db.login.RemoteLoginDataSource
import kr.co.lion.modigm.model.UserData

class LoginRepository {
    private val logTag by lazy { LoginRepository::class.simpleName }

    private val loginDataSource by lazy { RemoteLoginDataSource() }

    suspend fun emailLogin(email: String, password: String): Result<Int> {
        return runCatching {
            loginDataSource.emailLogin(email, password).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "이메일 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun githubLogin(activity: Activity): Result<Int> {
        return runCatching {
            loginDataSource.githubLogin(activity).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "깃허브 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun kakaoLogin(context: Context): Result<Int> {
        return runCatching {
            loginDataSource.kakaoLogin(context).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "카카오 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun autoLogin(userIdx: Int): Result<Int> {
        return runCatching {
            loginDataSource.autoLogin(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "자동 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun getUserDataByUserPhone(userPhone: String): Result<UserData> {
        return runCatching {
            loginDataSource.getUserDataByUserPhone(userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "전화번호로 유저 이름과 이메일 조회 중 오류 발생: ${e.message}", e)
            Result.failure<UserData>(e)
        }
    }

    suspend fun getUserDataByUserEmail(userEmail: String): Result<UserData> {
        return runCatching {
            loginDataSource.getUserDataByUserEmail(userEmail).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "이메일로 유저 이름과 전화번호 조회 중 오류 발생: ${e.message}", e)
            Result.failure<UserData>(e)
        }
    }

    suspend fun sendPhoneAuthCode(activity: Activity, userPhone: String): Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> {
        return runCatching {
            loginDataSource.sendPhoneAuthCode(activity, userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "전화 인증 코드 발송 중 오류 발생: ${e.message}", e)
            Result.failure<Triple<String, Any, Any>>(e)
        }
    }

    suspend fun getEmailByAuthCode(verificationId: String, authCode: String): Result<String> {
        return runCatching {
            loginDataSource.getEmailByAuthCode(verificationId, authCode).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "인증 코드로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    suspend fun signInByAuthCode(verificationId: String, authCode: String): Result<Boolean> {
        return runCatching {
            loginDataSource.signInByAuthCode(verificationId, authCode).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "인증 코드로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return runCatching {
            loginDataSource.updatePassword(newPassword).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "비밀번호 변경 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    suspend fun checkPassword(userPassword: String): Result<String> {
        return runCatching {
            loginDataSource.checkPassword(userPassword).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "비밀번호 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    suspend fun reAuthenticateWithKakao(context: Activity): Result<String> {
        return runCatching {
            loginDataSource.reAuthenticateWithKakao(context).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "카카오 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    suspend fun reAuthenticateWithGithub(context: Activity): Result<String> {
        return runCatching {
            loginDataSource.reAuthenticateWithGithub(context).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "깃허브 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    suspend fun updatePhone(userIdx: Int, currentUserPhone:String, newUserPhone:String, verificationId: String, authCode: String): Result<Boolean> {
        return runCatching {
            loginDataSource.updatePhone(userIdx, currentUserPhone, newUserPhone, verificationId, authCode).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "전화번호 변경 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    fun authLogout(): Result<Boolean> {
        return runCatching {
            loginDataSource.authLogout().getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "로그아웃 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    // FCM 토큰 등록 메서드
    suspend fun registerFcmToken(userIdx: Int, fcmToken: String): Result<Boolean> {
        return loginDataSource.registerFcmToken(userIdx, fcmToken)
    }

    // 사용자 FCM 토큰 가져오는 메서드
    suspend fun getUserFcmToken(userIdx: Int): String? {
        return loginDataSource.getUserFcmToken(userIdx)
    }
}
