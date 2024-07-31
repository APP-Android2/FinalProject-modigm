package kr.co.lion.modigm.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kr.co.lion.modigm.db.login.LoginDataSource
import kr.co.lion.modigm.model.SqlUserData

class LoginRepository {
    private val tag by lazy { "LoginRepository" }

    private val loginDataSource by lazy { LoginDataSource() }

    /**
     * 이메일과 비밀번호로 로그인
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun emailLogin(email: String, password: String): Result<Int> {
        return runCatching {
            loginDataSource.loginWithEmailPassword(email, password).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 깃허브로 로그인
     * @param context 액티비티 컨텍스트
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun githubLogin(context: Activity): Result<Int> {
        return runCatching {
            loginDataSource.signInWithGithub(context).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "깃허브 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 카카오로 로그인
     * @param context 컨텍스트
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun kakaoLogin(context: Context): Result<Int> {
        return runCatching {
            loginDataSource.loginWithKakao(context).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "카카오 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 자동 로그인
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun autoLogin(userIdx: Int): Result<Int> {
        return runCatching {
            loginDataSource.autoLogin(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "자동 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun getUserDataByUserPhone(userPhone: String): Result<SqlUserData> {
        return runCatching {
            loginDataSource.getUserDataByUserPhone(userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화번호로 유저 이름과 이메일 조회 중 오류 발생: ${e.message}", e)
            Result.failure<SqlUserData>(e)
        }
    }

    suspend fun getUserDataByUserEmail(userEmail: String): Result<SqlUserData> {
        return runCatching {
            loginDataSource.getUserDataByUserEmail(userEmail).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일로 유저 이름과 전화번호 조회 중 오류 발생: ${e.message}", e)
        }
    }

    suspend fun sendPhoneAuthCode(activity: Activity, userPhone: String): Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> {
        return runCatching {
            loginDataSource.sendPhoneAuthCode(activity, userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화 인증 코드 발송 중 오류 발생: ${e.message}", e)
            Result.failure<Triple<String, Any, Any>>(e)
        }
    }

    // 인증번호 확인 (이메일 찾기)
    suspend fun getEmailByInputCode(verificationId: String, inputCode: String): Result<String> {
        return runCatching {
            loginDataSource.getEmailByInputCode(verificationId, inputCode).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "인증 코드로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    suspend fun signInByInputCode(verificationId: String, inputCode: String): Result<Boolean> {
        return runCatching {
            loginDataSource.signInByInputCode(verificationId, inputCode).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "인증 코드로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    fun updatePassword(newPassword: String): Result<Boolean> {
        return runCatching {
            loginDataSource.updatePassword(newPassword).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "비밀번호 변경 중 오류 발생: ${e.message}", e)
            Result.failure<Unit>(e)
        }
    }

    /**
     * DAO 코루틴 취소
     */
    suspend fun closeDataSource() {
        loginDataSource.closeDataSource()
    }
}
