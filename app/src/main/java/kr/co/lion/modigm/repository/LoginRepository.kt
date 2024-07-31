package kr.co.lion.modigm.repository

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseUser
import kr.co.lion.modigm.db.login.LoginDataSource

class LoginRepository {

    private val loginDataSource = LoginDataSource()

    /**
     * 이메일과 비밀번호로 로그인
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return Result<Boolean> 로그인 성공 여부를 반환
     */
    suspend fun emailLogin(email: String, password: String): Result<Int> {
        return loginDataSource.loginWithEmailPassword(email, password)
    }

    /**
     * 깃허브로 로그인
     * @param context 액티비티 컨텍스트
     * @return Result<Boolean> 로그인 성공 여부를 반환
     */
    suspend fun githubLogin(context: Activity): Result<Int> {
        return loginDataSource.signInWithGithub(context)
    }

    /**
     * 카카오로 로그인
     * @param context 컨텍스트
     * @return Result<Boolean> 로그인 성공 여부를 반환
     */
    suspend fun kakaoLogin(context: Context): Result<Int> {
        return loginDataSource.loginWithKakao(context)
    }

    /**
     * 자동 로그인
     * @return Result<Boolean> 로그인 성공 여부를 반환
     */
    suspend fun autoLogin(userIdx: Int):Result<Int> {
        return loginDataSource.autoLogin(userIdx)
    }

    /**
     * DAO 코루틴 취소
     */
    suspend fun daoCoroutineCancel() {
        loginDataSource.daoCoroutineCancel()
    }
}
