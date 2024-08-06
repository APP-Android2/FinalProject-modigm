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
import kr.co.lion.modigm.model.SqlUserData
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteLoginDataSource {

    private val tag by lazy { RemoteLoginDataSource::class.simpleName }
    private val dao by lazy { RemoteLoginDao() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val functions by lazy { FirebaseFunctions.getInstance("asia-northeast3") }

    /**
     * 카카오로 로그인
     * @param context 컨텍스트
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun kakaoLogin(context: Context): Result<Int> {
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

    /**
     * 카카오톡으로 로그인
     * @param context 컨텍스트
     * @return OAuthToken 카카오 OAuth 토큰
     */
    private suspend fun loginWithKakaoTalk(context: Context): OAuthToken =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(tag, "카카오톡 로그인 실패", error)
                    cont.resumeWithException(IllegalStateException("카카오톡 로그인 실패: ${error.message}"))
                } else if (token != null) {
                    Log.d(tag, "카카오톡 로그인 성공")
                    cont.resume(token)
                } else {
                    Log.e(tag, "카카오톡 로그인 실패: 알 수 없는 오류")
                    cont.resumeWithException(IllegalStateException("카카오톡 로그인 실패: 알 수 없는 오류"))
                }
            }
        }

    /**
     * 카카오 계정으로 로그인
     * @param context 컨텍스트
     * @return OAuthToken 카카오 OAuth 토큰
     */
    private suspend fun loginWithKakaoAccount(context: Context): OAuthToken =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) {
                    Log.e(tag, "카카오 계정 로그인 실패", error)
                    cont.resumeWithException(IllegalStateException("카카오 계정 로그인 실패: ${error.message}"))
                } else if (token != null) {
                    Log.d(tag, "카카오 계정 로그인 성공")
                    cont.resume(token)
                } else {
                    Log.e(tag, "카카오 계정 로그인 실패: 알 수 없는 오류")
                    cont.resumeWithException(IllegalStateException("카카오 계정 로그인 실패: 알 수 없는 오류"))
                }
            }
        }

    /**
     * 카카오 로그인 응답 처리
     * @param token 카카오 OAuth 토큰
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    private suspend fun handleKakaoResponse(token: OAuthToken?): Result<Int> {
        return runCatching {
            if (token != null) {
                val customToken = getKakaoCustomToken(token.accessToken).getOrThrow()
                val result = auth.signInWithCustomToken(customToken).await()
                val user = result.user ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
                val uid = user.uid
                getUserIdxByUserUid(uid).fold(
                    onSuccess = { userIdx ->
                        Log.d(tag, "카카오 로그인 성공. uid: $uid")
                        userIdx
                    },
                    onFailure = { e ->
                        Log.d(tag, "데이터베이스에 없는 사용자", e)
                        if (e.message == "해당 유저를 찾을 수 없습니다.") 0 else throw e
                    }
                )
            } else {
                throw IllegalStateException("유효하지 않은 카카오 토큰입니다.")
            }
        }.onFailure { e ->
            Log.e(tag, "카카오 로그인 응답 처리 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 카카오 커스텀 토큰 획득
     * @param accessToken 카카오 OAuth 액세스 토큰
     * @return Result<String> 카카오 커스텀 토큰을 반환
     */
    private suspend fun getKakaoCustomToken(accessToken: String): Result<String> {
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

    /**
     * 깃허브로 로그인
     * @param context 액티비티 컨텍스트
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun githubLogin(context: Activity): Result<Int> {
        return runCatching {
            val provider = OAuthProvider.newBuilder("github.com")
            val result = auth.startActivityForSignInWithProvider(context, provider.build()).await()
            val user = result.user ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            val uid = user.uid
            Log.d(tag, "Github 로그인 성공. uid: $uid")
            val isNewUser = result.additionalUserInfo?.isNewUser
            Log.d(tag, "isNewUser: $isNewUser")
            if (isNewUser != null && isNewUser == true) 0 else getUserIdxByUserUid(uid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "깃허브로 로그인 중 오류 발생: ${e.message}", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 이메일과 비밀번호로 로그인
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun emailLogin(email: String, password: String): Result<Int> {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            Log.d(tag, "Firebase 로그인 성공. uid: $uid")
            getUserIdxByUserUid(uid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일과 비밀번호로 로그인 중 오류 발생", e)
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
            val authUserUid = auth.currentUser?.uid ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
            val prefsUserUid = getUserUidByUserIdx(userIdx).getOrThrow()
            if (authUserUid == prefsUserUid) {
                Log.d(tag, "자동 로그인 성공")
                userIdx
            } else {
                throw IllegalStateException("사용자 ID가 일치하지 않습니다.")
            }
        }.onFailure { e ->
            Log.e(tag, "자동 로그인 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 사용자 인덱스로 사용자 데이터 조회
     * @param userIdx 사용자 인덱스
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun getUserDataByUserIdx(userIdx: Int): Result<SqlUserData> {
        return runCatching {
            dao.selectUserDataByUserIdx(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    /**
     * 사용자 UID로 사용자 인덱스 조회
     * @param userUid 사용자 UID
     * @return Result<Int> 조회된 사용자 인덱스를 반환
     */
    private suspend fun getUserIdxByUserUid(userUid: String): Result<Int> {
        return runCatching {
            dao.selectUserIdxByUserUid(userUid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userUid로 userIdx 조회 중 오류 발생", e)
            Result.failure<Int>(e)
        }
    }

    /**
     * 사용자 UID로 사용자 데이터 조회
     * @param userUid 사용자 UID
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun getUserDataByUserUid(userUid: String): Result<SqlUserData> {
        return runCatching {
            dao.selectUserDataByUserUid(userUid).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    /**
     * 사용자 인덱스로 사용자 UID 조회
     * @param userIdx 사용자 인덱스
     * @return Result<String> 조회된 사용자 UID를 반환
     */
    private suspend fun getUserUidByUserIdx(userIdx: Int): Result<String> {
        return runCatching {
            dao.selectUserUidByUserIdx(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "userIdx로 userUid 조회 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    /**
     * 전화번호로 사용자 데이터 조회
     * @param userPhone 사용자 전화번호
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun getUserDataByUserPhone(userPhone: String): Result<SqlUserData> {
        return runCatching {
            dao.selectUserDataByUserPhone(userPhone).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전화번호로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    /**
     * 이메일로 사용자 데이터 조회
     * @param userEmail 사용자 이메일
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun getUserDataByUserEmail(userEmail: String): Result<SqlUserData> {
        return runCatching {
            dao.selectUserDataByUserEmail(userEmail).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "이메일로 유저 데이터 조회 중 오류 발생", e)
            Result.failure<SqlUserData>(e)
        }
    }

    /**
     * 전화 인증 코드 발송
     * @param activity 액티비티 컨텍스트
     * @param userPhone 사용자 전화번호
     * @return Result<Triple<String, PhoneAuthCredential?, PhoneAuthProvider.ForceResendingToken?>> 인증 코드 발송 결과를 반환
     */
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

    /**
     * 인증번호 확인 (이메일 찾기)
     * @param verificationId 인증 ID
     * @param authCode 사용자 입력 인증 코드
     * @return Result<String> 이메일을 반환
     */
    suspend fun getEmailByInputCode(verificationId: String, authCode: String): Result<String> {
        return runCatching {
            val phoneCredential = PhoneAuthProvider.getCredential(verificationId, authCode)
            val result = auth.signInWithCredential(phoneCredential).await()
            val email = result.user?.email ?: throw IllegalStateException("유효한 이메일이 아닙니다.")
            auth.signOut()
            email
        }.onFailure { e ->
            Log.e(tag, "인증번호 확인 중 오류 발생", e)
            Result.failure<String>(e)
        }
    }

    /**
     * 인증번호 확인 (비밀번호 찾기)
     * @param verificationId 인증 ID
     * @param authCode 사용자 입력 인증 코드
     * @return Result<Boolean> 인증 성공 여부를 반환
     */
    suspend fun signInByAuthCode(verificationId: String, authCode: String): Result<Boolean> {
        return runCatching {
            val credential = PhoneAuthProvider.getCredential(verificationId, authCode)
            auth.signInWithCredential(credential).await()
            true
        }.onFailure { e ->
            Log.e(tag, "인증번호 확인 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 비밀번호 변경
     * @param newPassword 새로운 비밀번호
     * @return Result<Boolean> 비밀번호 변경 성공 여부를 반환
     */
    fun updatePassword(newPassword: String): Result<Boolean> {
        return runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("유효한 사용자가 아닙니다.")
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

    fun authLogout(): Result<Boolean> {
        return runCatching {
            // 로그아웃
            auth.signOut()
            true
        }.onFailure { e ->
            Log.e(tag, "로그아웃 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }
}
