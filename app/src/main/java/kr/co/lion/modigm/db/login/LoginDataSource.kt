package kr.co.lion.modigm.db.login

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.ui.login.LoginError
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginDataSource {

    private val tag by lazy { "LoginDataSource" }
    private val dao by lazy { LoginDao() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val functions by lazy { FirebaseFunctions.getInstance("asia-northeast3") }

    suspend fun loginWithEmailPassword(email: String, password: String): Result<Int> {
        Log.d(tag, "loginWithEmailPassword 호출됨. email: $email")
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw LoginError.FirebaseEmailLoginError
            Log.d(tag, "Firebase 로그인 성공. uid: $uid")
            getUserIdxByUserUid(uid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일과 비밀번호로 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun signInWithGithub(context: Activity): Result<Int> {
        Log.d(tag, "signInWithGithub 호출됨.")
        return runCatching {
            val provider = OAuthProvider.newBuilder("github.com")
            val result = auth.startActivityForSignInWithProvider(context, provider.build()).await()
            val uid = result.user?.uid ?: throw LoginError.GithubOAuthError
            Log.d(tag, "Github 로그인 성공. uid: $uid")
            getUserIdxByUserUid(uid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "깃허브로 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun loginWithKakao(context: Context): Result<Int> {
        Log.d(tag, "loginWithKakao 호출됨.")
        return runCatching {
            val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                loginWithKakaoTalk(context)
            } else {
                loginWithKakaoAccount(context)
            }
            handleKakaoResponse(token).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "카카오로 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    private suspend fun handleKakaoResponse(token: OAuthToken?): Result<Int> {
        Log.d(tag, "handleKakaoResponse 호출됨. token: $token")
        return runCatching {
            if (token != null) {
                val customToken = getKakaoCustomToken(token.accessToken).getOrThrow()
                val uid = signInWithCustomToken(customToken).getOrThrow()
                getUserIdxByUserUid(uid).getOrThrow()
            } else {
                throw LoginError.KakaoAuthError
            }
        }.onFailure { e ->
            Log.e(tag, "카카오 로그인 응답 처리 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun autoLogin(userIdx: Int): Result<Int> {
        Log.d(tag, "autoLogin 호출됨. userIdx: $userIdx")
        return runCatching {
            val authUserUid = auth.currentUser?.uid ?: throw LoginError.FirebaseInvalidUser
            val prefsUserUid = getUserUidByUserIdx(userIdx).getOrThrow()
            if (authUserUid == prefsUserUid) {
                Log.d(tag, "자동 로그인 성공. userIdx: $userIdx")
                userIdx
            } else {
                throw LoginError.FirebaseInvalidUser
            }
        }.onFailure { e ->
            Log.e(tag, "자동 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    private suspend fun getKakaoCustomToken(accessToken: String): Result<String> {
        Log.d(tag, "getKakaoCustomToken 호출됨. accessToken: $accessToken")
        val data = hashMapOf("token" to accessToken)
        return runCatching {
            val result = functions.getHttpsCallable("getKakaoCustomAuth").call(data).await()
            val customToken = result.data as Map<*, *>
            Log.d(tag, "카카오 커스텀 토큰 획득 성공.")
            customToken["custom_token"] as String
        }.onFailure { e ->
            Log.e(tag, "카카오 커스텀 토큰 획득 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    private suspend fun signInWithCustomToken(customToken: String): Result<String> {
        Log.d(tag, "signInWithCustomToken 호출됨. customToken: $customToken")
        return runCatching {
            val authResult = auth.signInWithCustomToken(customToken).await()
            val uid = authResult.user?.uid ?: throw LoginError.FirebaseEmailLoginError
            Log.d(tag, "Firebase 커스텀 토큰 로그인 성공. uid: $uid")
            uid
        }.onFailure { e ->
            Log.e(tag, "커스텀 토큰으로 Firebase 로그인 중 오류 발생", e)
        }
    }

    private suspend fun loginWithKakaoTalk(context: Context): OAuthToken =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(tag, "카카오톡 로그인 실패", error)
                    cont.resumeWithException(LoginError.KakaoAuthError)
                } else if (token != null) {
                    Log.d(tag, "카카오톡 로그인 성공")
                    cont.resume(token)
                } else {
                    Log.e(tag, "카카오톡 로그인 실패: 알 수 없는 오류")
                    cont.resumeWithException(LoginError.KakaoAuthError)
                }
            }
        }

    private suspend fun loginWithKakaoAccount(context: Context): OAuthToken =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) {
                    Log.e(tag, "카카오 계정 로그인 실패", error)
                    cont.resumeWithException(LoginError.KakaoAuthError)
                } else if (token != null) {
                    Log.d(tag, "카카오 계정 로그인 성공")
                    cont.resume(token)
                } else {
                    Log.e(tag, "카카오 계정 로그인 실패: 알 수 없는 오류")
                    cont.resumeWithException(LoginError.KakaoAuthError)
                }
            }
        }

    private suspend fun getUserDataByUserIdx(userIdx: Int): Result<SqlUserData> {
        Log.d(tag, "getUserDataByUserIdx 호출됨. userIdx: $userIdx")
        return runCatching {
            dao.selectUserDataByUserIdx(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    private suspend fun getUserIdxByUserUid(userUid: String): Result<Int> {
        Log.d(tag, "getUserIdxByUserUid 호출됨. userUid: $userUid")
        return runCatching {
            dao.selectUserIdxByUserUid(userUid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userUid로 userIdx 조회 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    private suspend fun getUserDataByUserUid(userUid: String): Result<SqlUserData> {
        Log.d(tag, "getUserDataByUserUid 호출됨. userUid: $userUid")
        return runCatching {
            dao.selectUserDataByUserUid(userUid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    private suspend fun getUserUidByUserIdx(userIdx: Int): Result<String> {
        Log.d(tag, "getUserUidByUserIdx 호출됨. userIdx: $userIdx")
        return runCatching {
            dao.selectUserUidByUserIdx(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userIdx로 userUid 조회 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    suspend fun getUserDataByUserPhone(userPhone: String): Result<SqlUserData> {
        Log.d(tag, "checkUserByPhoneFindNameAndEmail 호출됨. userPhone: $userPhone")
        return runCatching {
            dao.selectUserDataByUserPhone(userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화번호로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    suspend fun getUserDataByUserEmail(userEmail: String): Result<SqlUserData> {
        Log.d(tag, "getUserDataByUserEmail 호출됨. userEmail: $userEmail")
        return runCatching {
            dao.selectUserDataByUserEmail(userEmail).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    suspend fun sendPhoneAuthCode(activity: Activity, userPhone: String): Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> {
        Log.d(tag, "sendPhoneAuthCode 호출됨. userPhone: $userPhone")
        return runCatching {
            suspendCancellableCoroutine { cont ->
                val setNumber = userPhone.replaceRange(0, 1, "+82 ")
                auth.setLanguageCode("kr")

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(setNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            Log.d(tag, "전화번호 인증 완료. credential: $credential")
                            if (cont.isActive) {
                                cont.resume(Triple("", credential, null))
                            }
                        }

                        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                            Log.e(tag, "전화번호 인증코드 발송 중 오류 발생", e)
                            if (cont.isActive) {
                                cont.resumeWithException(e)
                            }
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            Log.d(tag, "인증 코드 발송. verificationId: $verificationId")
                            if (cont.isActive) {
                                cont.resume(Triple(verificationId, null, token))
                            }
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }.onFailure { e ->
            Log.e(tag, "전화번호 인증코드 발송 중 오류 발생", e)
            Result.failure<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>>(e)
        }
    }



    // 인증번호 확인 (이메일 찾기)
    suspend fun getEmailByInputCode(verificationId: String, inputCode: String): Result<String> {
        Log.d(tag, "signInWithCredential 호출됨. verificationId: $verificationId, inputCode: $inputCode")
        return runCatching {
            val phoneCredential = PhoneAuthProvider.getCredential(verificationId, inputCode)
            val result = auth.signInWithCredential(phoneCredential).await()
            val email = result.user?.email ?: throw LoginError.FirebaseEmailLoginError
            auth.signOut()
            email
        }.onFailure { e ->
            Log.e(tag, "인증번호 확인 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    // 인증번호 확인 (비밀번호 찾기)
    suspend fun signInByInputCode(verificationId: String, inputCode: String): Result<Boolean> {
        return runCatching {
            Log.d(tag, "signInByInputCode 호출됨. verificationId: $verificationId, inputCode: $inputCode")
            val credential = PhoneAuthProvider.getCredential(verificationId, inputCode)
            auth.signInWithCredential(credential).await()
            true
        }.onFailure { e ->
            Log.e(tag, "인증번호 확인 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    // 비밀번호 변경
    fun updatePassword(newPassword: String): Result<Boolean> {
        return runCatching {
            Log.d(tag, "updatePassword 호출됨. newPassword: $newPassword")
            val user = auth.currentUser ?: throw LoginError.FirebaseInvalidUser
            Log.d(tag, "현재 사용자 로그인 상태. user: $user")
            user.updatePassword(newPassword).addOnSuccessListener {
                Log.d(tag, "비밀번호 변경 성공")
                auth.signOut()
                Log.d(tag, "로그아웃 성공")
            }
            true
        }.onFailure { e ->
            Log.e(tag, "비밀번호 변경 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }


    suspend fun closeDataSource() {
        Log.d(tag, "closeDataSource 호출됨.")
        HikariCPDataSource.closeDataSource()
    }
}
