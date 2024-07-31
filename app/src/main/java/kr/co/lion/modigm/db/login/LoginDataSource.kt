package kr.co.lion.modigm.db.login

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.PreferenceUtil
import kr.co.lion.modigm.ui.login.LoginError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginDataSource {

    private val tag = "LoginDataSource"
    private val dao = LoginDao()
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance("asia-northeast3")

    /**
     * 이메일과 비밀번호로 로그인
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun loginWithEmailPassword(email: String, password: String): Result<Int> {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw LoginError.FirebaseEmailLoginError
            getUserIdxByUserUid(uid)
        }.onFailure { e ->
            Result.failure<Int>(e)
        }
    }

    /**
     * 깃허브로 로그인
     * @param context 액티비티 컨텍스트
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun signInWithGithub(context: Activity): Result<Int> {
        return runCatching {
            val provider = OAuthProvider.newBuilder("github.com")
            val result = auth.startActivityForSignInWithProvider(context, provider.build()).await()
            val uid = result.user?.uid ?: throw LoginError.GithubOAuthError
            getUserIdxByUserUid(uid)
        }.onFailure { e ->
            Result.failure<Int>(e)
        }
    }

    /**
     * 카카오로 로그인
     * @param context 컨텍스트
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun loginWithKakao(context: Context): Result<Int> {
        return runCatching {
            val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                loginWithKakaoTalk(context)
            } else {
                loginWithKakaoAccount(context)
            }
            handleKakaoResponse(token)
        }.onFailure { e ->
            Result.failure<Int>(e)
        }
    }

    /**
     * 카카오 로그인 응답 처리
     * @param token OAuthToken?
     * @return Int 로그인 성공 여부를 반환
     */
    private suspend fun handleKakaoResponse(token: OAuthToken?): Int {
        return if (token != null) {
            runCatching {
                val customToken = getKakaoCustomToken(token.accessToken)
                val uid = signInWithCustomToken(customToken)
                getUserIdxByUserUid(uid)
            }.getOrElse {
                throw LoginError.KakaoAuthError
            }
        } else {
            throw LoginError.KakaoAuthError
        }
    }

    /**
     * 자동 로그인
     * @return Result<Int> 로그인 성공 여부를 반환
     */
    suspend fun autoLogin(userIdx: Int): Result<Int> {
        return runCatching {
            val authUserUid = auth.currentUser?.uid ?: throw LoginError.FirebaseInvalidUser
            val prefsUserUid = getUserUidByUserIdx(userIdx)
            if(authUserUid == prefsUserUid){
                userIdx
            } else {
                throw LoginError.FirebaseInvalidUser
            }
        }.onFailure { e ->
            Result.failure<Int>(e)
        }
    }

    /**
     * 사용자 등록 여부 확인
     * @param uid 사용자의 UID
     * @return Boolean 사용자가 이미 등록되어 있는지 여부
     */
    private suspend fun isUserAlreadyRegistered(uid: String): Boolean {
        return dao.isUserAlreadyRegistered(uid).fold(
            onSuccess = { result -> result },
            onFailure = { e ->
                Log.e(tag, "이미 존재하는 유저인지 조회 중 오류 발생", e)
                throw LoginError.DatabaseUnknownError
            }
        )
    }

    /**
     * 카카오 커스텀 토큰 획득
     * @param accessToken 카카오 액세스 토큰
     * @return String 커스텀 토큰
     */
    private suspend fun getKakaoCustomToken(accessToken: String): String {
        val data = hashMapOf("token" to accessToken)
        return runCatching {
            val result = functions.getHttpsCallable("getKakaoCustomAuth").call(data).await()
            val customToken = result.data as Map<*, *>
            customToken["custom_token"] as String
        }.getOrElse {
            throw LoginError.KakaoAuthError
        }
    }

    /**
     * 커스텀 토큰으로 Firebase 로그인
     * @param customToken 커스텀 토큰
     * @return String 사용자 UID
     */
    private suspend fun signInWithCustomToken(customToken: String): String {
        return runCatching {
            val authResult = auth.signInWithCustomToken(customToken).await()
            authResult.user?.uid ?: throw LoginError.FirebaseEmailLoginError
        }.getOrElse {
            throw it
        }
    }

    /**
     * 카카오톡으로 로그인
     * @param context 컨텍스트
     * @return OAuthToken 카카오 OAuth 토큰
     */
    private suspend fun loginWithKakaoTalk(context: Context): OAuthToken = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                cont.resumeWithException(LoginError.KakaoAuthError)
            } else if (token != null) {
                cont.resume(token)
            } else {
                cont.resumeWithException(LoginError.KakaoAuthError)
            }
        }
    }

    /**
     * 카카오 계정으로 로그인
     * @param context 컨텍스트
     * @return OAuthToken 카카오 OAuth 토큰
     */
    private suspend fun loginWithKakaoAccount(context: Context): OAuthToken = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                cont.resumeWithException(LoginError.KakaoAuthError)
            } else if (token != null) {
                cont.resume(token)
            } else {
                cont.resumeWithException(LoginError.KakaoAuthError)
            }
        }
    }

    /**
     * userIdx를 통해 사용자 데이터 조회
     * @param userIdx 사용자 인덱스
     * @return SqlUserData 사용자 데이터
     */
    private suspend fun getUserDataByUserIdx(userIdx: Int): SqlUserData {
        return dao.selectUserDataByUserIdx(userIdx).fold(
            onSuccess = { result -> result },
            onFailure = { e ->
                Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
                throw LoginError.DatabaseUnknownError
            }
        )
    }

    /**
     * userUid를 통해 사용자 인덱스 조회
     * @param userUid 사용자 UID
     * @return Int 사용자 인덱스
     */
    private suspend fun getUserIdxByUserUid(userUid: String): Int {
        return dao.selectUserIdxByUserUid(userUid).fold(
            onSuccess = { result -> result },
            onFailure = { e ->
                Log.e(tag, "userUid로 userIdx 조회 중 오류 발생", e)
                throw LoginError.DatabaseUnknownError
            }
        )
    }

    /**
     * userUid를 통해 사용자 데이터 조회
     * @param userUid 사용자 UID
     * @return SqlUserData 사용자 데이터
     */
    private suspend fun getUserDataByUserUid(userUid: String): SqlUserData {
        return dao.selectUserDataByUserUid(userUid).fold(
            onSuccess = { result -> result },
            onFailure = { e ->
                Log.e(tag,"userUid로 유저 데이터 조회 중 오류 발생", e)
                throw LoginError.DatabaseUnknownError
            }
        )
    }

    /**
     * userIdx를 통해 사용자 UID 조회
     * @param userIdx 사용자 인덱스
     * @return Int 사용자 UID
     */
    private suspend fun getUserUidByUserIdx(userIdx: Int): String {
        return dao.selectUserUidByUserIdx(userIdx).fold(
            onSuccess = { result -> result },
            onFailure = { e ->
                Log.e(tag, "userIdx로 userUid 조회 중 오류 발생", e)
                throw LoginError.DatabaseUnknownError
            }
        )
    }

    /**
     * DAO 코루틴 취소
     */
    suspend fun daoCoroutineCancel() {
        dao.daoCoroutineCancel()
    }
}
