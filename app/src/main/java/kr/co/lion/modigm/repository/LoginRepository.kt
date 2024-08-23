package kr.co.lion.modigm.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kr.co.lion.modigm.db.login.RemoteLoginDataSource
import kr.co.lion.modigm.model.SqlUserData

class LoginRepository {
    private val tag by lazy { LoginRepository::class.simpleName }

    private val loginDataSource by lazy { RemoteLoginDataSource() }

    /**
     * 이메일과 비밀번호로 로그인
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun emailLogin(email: String, password: String): Result<Int> {
        return runCatching {
            loginDataSource.emailLogin(email, password).getOrThrow()
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
            loginDataSource.githubLogin(context).getOrThrow()
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
            loginDataSource.kakaoLogin(context).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "카카오 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 자동 로그인
     * @param userIdx 사용자의 인덱스
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

    /**
     * 전화번호로 사용자 데이터 조회
     * @param userPhone 사용자의 전화번호
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun getUserDataByUserPhone(userPhone: String): Result<SqlUserData> {
        return runCatching {
            loginDataSource.getUserDataByUserPhone(userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화번호로 유저 이름과 이메일 조회 중 오류 발생: ${e.message}", e)
            Result.failure<SqlUserData>(e)
        }
    }

    /**
     * 이메일로 사용자 데이터 조회
     * @param userEmail 사용자의 이메일
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun getUserDataByUserEmail(userEmail: String): Result<SqlUserData> {
        return runCatching {
            loginDataSource.getUserDataByUserEmail(userEmail).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일로 유저 이름과 전화번호 조회 중 오류 발생: ${e.message}", e)
            Result.failure<SqlUserData>(e)
        }
    }

    /**
     * 전화 인증 코드 발송
     * @param activity 액티비티 컨텍스트
     * @param userPhone 사용자의 전화번호
     * @return Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> 인증 코드 발송 결과를 반환
     */
    suspend fun sendPhoneAuthCode(activity: Activity, userPhone: String): Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> {
        return runCatching {
            loginDataSource.sendPhoneAuthCode(activity, userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화 인증 코드 발송 중 오류 발생: ${e.message}", e)
            Result.failure<Triple<String, Any, Any>>(e)
        }
    }

    /**
     * 인증번호 확인 (이메일 찾기)
     * @param verificationId 인증 ID
     * @param authCode 사용자가 입력한 인증 코드
     * @return Result<String> 이메일을 반환
     */
    suspend fun getEmailByAuthCode(verificationId: String, authCode: String): Result<String> {
        return runCatching {
            loginDataSource.getEmailByAuthCode(verificationId, authCode).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "인증 코드로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    /**
     * 인증 코드로 로그인
     * @param verificationId 인증 ID
     * @param authCode 사용자가 입력한 인증 코드
     * @return Result<Boolean> 로그인 성공 여부를 반환
     */
    suspend fun signInByAuthCode(verificationId: String, authCode: String): Result<Boolean> {
        return runCatching {
            loginDataSource.signInByAuthCode(verificationId, authCode).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "인증 코드로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 비밀번호 변경
     * @param newPassword 새로운 비밀번호
     * @return Result<Boolean> 비밀번호 변경 성공 여부를 반환
     */
    suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return runCatching {
            loginDataSource.updatePassword(newPassword).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "비밀번호 변경 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 유저 비밀번호 재인증
     */
    suspend fun checkPassword(userPassword: String): Result<String> {
        return runCatching {
            loginDataSource.checkPassword(userPassword).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "비밀번호 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    /**
     * 카카오 재인증
     */
    suspend fun reAuthenticateWithKakao(context: Activity): Result<String> {
        return runCatching {
            loginDataSource.reAuthenticateWithKakao(context).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "카카오 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    /**
     * 깃허브 재인증
     */
    suspend fun reAuthenticateWithGithub(context: Activity): Result<String> {
        return runCatching {
            loginDataSource.reAuthenticateWithGithub(context).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "깃허브 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    /**
     * 전화번호 변경
     */
    suspend fun updatePhone(userIdx: Int, currentUserPhone:String, newUserPhone:String, verificationId: String, authCode: String): Result<Boolean> {
        return runCatching {
            loginDataSource.updatePhone(userIdx, currentUserPhone, newUserPhone, verificationId, authCode).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화번호 변경 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 로그아웃
     */
    fun authLogout(): Result<Boolean> {
        return runCatching {
            loginDataSource.authLogout().getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "로그아웃 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }
}
