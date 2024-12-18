package kr.co.lion.modigm.db.login

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
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
import kr.co.lion.modigm.model.UserData
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteLoginDataSource {

    private val logTag by lazy { RemoteLoginDataSource::class.simpleName }
    private val dao by lazy { RemoteLoginDao() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val functions by lazy { FirebaseFunctions.getInstance("asia-northeast3") }

    suspend fun kakaoLogin(context: Context): Result<Int> {
        return runCatching {
            val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                loginWithKakaoTalk(context)
            } else {
                loginWithKakaoAccount(context)
            }
            handleKakaoResponse(token).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "카카오로 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    private suspend fun loginWithKakaoTalk(context: Context): OAuthToken =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(logTag, "카카오톡 로그인 실패", error)
                    cont.resumeWithException(IllegalStateException("카카오톡 로그인 실패: ${error.message}"))
                } else if (token != null) {
                    Log.d(logTag, "카카오톡 로그인 성공")
                    cont.resume(token)
                } else {
                    Log.e(logTag, "카카오톡 로그인 실패: 알 수 없는 오류")
                    cont.resumeWithException(IllegalStateException("카카오톡 로그인 실패: 알 수 없는 오류"))
                }
            }
        }

    private suspend fun loginWithKakaoAccount(context: Context): OAuthToken =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) {
                    Log.e(logTag, "카카오 계정 로그인 실패", error)
                    cont.resumeWithException(IllegalStateException("카카오 계정 로그인 실패: ${error.message}"))
                } else if (token != null) {
                    Log.d(logTag, "카카오 계정 로그인 성공")
                    cont.resume(token)
                } else {
                    Log.e(logTag, "카카오 계정 로그인 실패: 알 수 없는 오류")
                    cont.resumeWithException(IllegalStateException("카카오 계정 로그인 실패: 알 수 없는 오류"))
                }
            }
        }

    private suspend fun handleKakaoResponse(token: OAuthToken?): Result<Int> {
        return runCatching {
            if (token != null) {
                val customToken = getKakaoCustomToken(token.accessToken).getOrThrow()
                val result = auth.signInWithCustomToken(customToken).await()
                val user = result.user ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
                val uid = user.uid
                getUserIdxByUserUid(uid).fold(
                    onSuccess = { userIdx ->
                        Log.d(logTag, "카카오 로그인 성공. uid: $uid")
                        userIdx
                    },
                    onFailure = { e ->
                        Log.d(logTag, "데이터베이스에 없는 사용자", e)
                        if (e.message == "해당 유저를 찾을 수 없습니다.") 0 else throw e
                    }
                )
            } else {
                throw IllegalStateException("유효하지 않은 카카오 토큰입니다.")
            }
        }.onFailure { e ->
            Log.e(logTag, "카카오 로그인 응답 처리 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    private suspend fun getKakaoCustomToken(accessToken: String): Result<String> {
        val data = hashMapOf("token" to accessToken)
        return runCatching {
            val result = functions.getHttpsCallable("getKakaoCustomAuth").call(data).await()
            val customToken = result.data as Map<*, *>
            Log.d(logTag, "카카오 커스텀 토큰 획득 성공.")
            customToken["custom_token"] as String
        }.onFailure { e ->
            Log.e(logTag, "카카오 커스텀 토큰 획득 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    suspend fun githubLogin(activity: Activity): Result<Int> {
        return runCatching {
            val provider = OAuthProvider.newBuilder("github.com")
            val result = auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            val user = result.user ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            val uid = user.uid
            Log.d(logTag, "Github 로그인 성공. uid: $uid")
            val isNewUser = result.additionalUserInfo?.isNewUser
            Log.d(logTag, "isNewUser: $isNewUser")
            if (isNewUser != null && isNewUser == true) 0 else getUserIdxByUserUid(uid).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "깃허브로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun emailLogin(email: String, password: String): Result<Int> {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            Log.d(logTag, "Firebase 로그인 성공. uid: $uid")
            getUserIdxByUserUid(uid).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "이메일과 비밀번호로 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun autoLogin(userIdx: Int): Result<Int> {
        return runCatching {
            val authUserUid = auth.currentUser?.uid ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            val prefsUserUid = getUserUidByUserIdx(userIdx).getOrThrow()
            if (authUserUid == prefsUserUid) {
                Log.d(logTag, "자동 로그인 성공")
                userIdx
            } else {
                throw IllegalStateException("사용자 ID가 일치하지 않습니다.")
            }
        }.onFailure { e ->
            Log.e(logTag, "자동 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun getUserDataByUserIdx(userIdx: Int): Result<UserData> {
        return runCatching {
            dao.selectUserDataByUserIdx(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<UserData>(e)
        }
    }

    private suspend fun getUserIdxByUserUid(userUid: String): Result<Int> {
        return runCatching {
            dao.selectUserIdxByUserUid(userUid).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "userUid로 userIdx 조회 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    suspend fun getUserDataByUserUid(userUid: String): Result<UserData> {
        return runCatching {
            dao.selectUserDataByUserUid(userUid).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "userUid로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<UserData>(e)
        }
    }

    private suspend fun getUserUidByUserIdx(userIdx: Int): Result<String> {
        return runCatching {
            dao.selectUserUidByUserIdx(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "userIdx로 userUid 조회 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    suspend fun getUserDataByUserPhone(userPhone: String): Result<UserData> {
        return runCatching {
            dao.selectUserDataByUserPhone(userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "전화번호로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<UserData>(e)
        }
    }

    suspend fun getUserDataByUserEmail(userEmail: String): Result<UserData> {
        return runCatching {
            dao.selectUserDataByUserEmail(userEmail).getOrThrow()
        }.onFailure { e ->
            Log.e(logTag, "이메일로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<UserData>(e)
        }
    }

    suspend fun sendPhoneAuthCode(activity: Activity, userPhone: String): Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> {
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
                            Log.d(logTag, "전화번호 인증 완료. credential: $credential")
                            if (cont.isActive) {
                                cont.resume(Triple("", credential, null))
                            }
                        }

                        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                            Log.e(logTag, "전화번호 인증코드 발송 중 오류 발생", e)
                            if (cont.isActive) {
                                cont.resumeWithException(e)
                            }
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            Log.d(logTag, "인증 코드 발송. verificationId: $verificationId")
                            if (cont.isActive) {
                                cont.resume(Triple(verificationId, null, token))
                            }
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }.onFailure { e ->
            Log.e(logTag, "전화번호 인증코드 발송 중 오류 발생", e)
            Result.failure<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>>(e)
        }
    }

    suspend fun getEmailByAuthCode(verificationId: String, authCode: String): Result<String> {
        return runCatching {
            val phoneCredential = PhoneAuthProvider.getCredential(verificationId, authCode)
            val result = auth.signInWithCredential(phoneCredential).await()
            val email = result.user?.email ?: throw IllegalStateException("유효한 이메일이 아닙니다.")
            auth.signOut()
            email
        }.onFailure { e ->
            Log.e(logTag, "인증번호 확인 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    suspend fun signInByAuthCode(verificationId: String, authCode: String): Result<Boolean> {
        return runCatching {
            val credential = PhoneAuthProvider.getCredential(verificationId, authCode)
            auth.signInWithCredential(credential).await()
            true
        }.onFailure { e ->
            Log.e(logTag, "인증번호 확인 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("유효한 사용자가 아닙니다.")

            // 비밀번호 변경
            user.updatePassword(newPassword).await()
            Log.d(logTag, "비밀번호 변경 성공")

            true
        }.onFailure { e ->
            Log.e(logTag, "비밀번호 변경 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    suspend fun checkPassword(password: String): Result<String> {
        return runCatching {
            // 현재 로그인한 사용자 가져오기
            val currentUser = auth.currentUser ?: throw IllegalStateException("로그인한 사용자가 없습니다.")
            // 비밀번호 재인증
            val credential = currentUser.email?.let { EmailAuthProvider.getCredential(it, password) } ?: throw IllegalStateException("로그인한 사용자가 없습니다.")
            currentUser.reauthenticate(credential).await()
            Log.d(logTag, "재인증 성공")
            // 현재 로그인한 사용자의 전화번호 가져오기
            currentUser.phoneNumber ?: throw IllegalStateException("로그인한 사용자의 전화번호가 없습니다.")
        }.onFailure { e ->
            Log.e(logTag, "재인증 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    // 카카오 재인증
    suspend fun reAuthenticateWithKakao(context: Activity): Result<String> {
        return runCatching {
            // 카카오 재인증 토큰 획득
            val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                loginWithKakaoTalk(context)
            } else {
                loginWithKakaoAccount(context)
            }

            // 획득한 토큰으로 재인증
            handleKakaoReAuthentication(token).getOrThrow()
            val user = auth.currentUser ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            user.phoneNumber ?: throw IllegalStateException("로그인한 사용자의 전화번호가 없습니다.")
        }.onFailure { e ->
            Log.e(logTag, "카카오 재인증 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    private suspend fun handleKakaoReAuthentication(token: OAuthToken?): Result<Boolean> {
        return runCatching {
            if (token != null) {
                // 카카오 커스텀 토큰 획득
                val customToken = getKakaoCustomToken(token.accessToken).getOrThrow()
                // 카카오 재인증
                val authResult = auth.signInWithCustomToken(customToken).await()
                // 로그인한 사용자 가져오기
                authResult.user ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
                Log.d(logTag, "카카오 재인증 성공.")
                true
            } else {
                throw IllegalStateException("유효하지 않은 카카오 토큰입니다.")
            }
        }.onFailure { e ->
            Log.e(logTag, "카카오 재인증 처리 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    suspend fun reAuthenticateWithGithub(context: Activity): Result<String> {
        return runCatching {
            // 깃허브 제공자 생성
            val provider = OAuthProvider.newBuilder("github.com")
            // 깃허브 재인증
            auth.currentUser?.startActivityForReauthenticateWithProvider(context, provider.build())?.await()
            Log.d(logTag, "깃허브 재인증 성공.")
            // 현재 로그인한 사용자의 전화번호 가져오기
            val user = auth.currentUser ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            user.phoneNumber ?: throw IllegalStateException("로그인한 사용자의 전화번호가 없습니다.")
        }.onFailure { e ->
            Log.e(logTag, "깃허브 재인증 중 오류 발생: ${e.message}", e)
            Result.failure<String>(e)
        }
    }

    suspend fun updatePhone(
        userIdx: Int,
        currentUserPhone: String,
        newUserPhone: String,
        verificationId: String,
        authCode: String
    ): Result<Boolean> {
        return runCatching {
            // 로그인한 사용자 가져오기
            val user = auth.currentUser ?: throw IllegalStateException("로그인한 사용자가 없습니다.")

            // 인증번호 확인
            val phoneCredential = PhoneAuthProvider.getCredential(verificationId, authCode)

            // 전화번호 변경
            user.updatePhoneNumber(phoneCredential).await()
            Log.d(logTag, "전화번호 변경 성공")

            // 전화번호 변경 성공 시 데이터베이스 전화번호 업데이트
            dao.updatePhoneByUserIdx(userIdx, newUserPhone).getOrThrow()
            true
        }.onFailure { e ->
            // 전화번호 변경 실패 시 원래 전화번호로 복구
            dao.updatePhoneByUserIdx(userIdx, currentUserPhone)
            Log.e(logTag, "전화번호 변경 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }


    fun authLogout(): Result<Boolean> {
        return runCatching {
            // 로그아웃
            auth.signOut()
            true
        }.onFailure { e ->
            Log.e(logTag, "로그아웃 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    // FCM 토큰을 서버에 등록하는 메서드
    suspend fun registerFcmToken(userIdx: Int, fcmToken: String): Boolean {
        return dao.insertUserFcmToken(userIdx, fcmToken)
    }

    // 사용자 FCM 토큰을 가져오는 메서드
    suspend fun getUserFcmToken(userIdx: Int): String? {
        return dao.getUserFcmToken(userIdx)
    }
}
