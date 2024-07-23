package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.join.RemoteJoinUserDataSource
import kr.co.lion.modigm.model.SqlUserData

class JoinUserRepository {

    private val _joinUserDataSource = RemoteJoinUserDataSource()
    // 회원가입
    suspend fun insetUserData(userInfoData: SqlUserData): Boolean = _joinUserDataSource.insetUserData(userInfoData)

    // 리소스를 해제하는 메서드 추가
    suspend fun closeConn() {
        _joinUserDataSource.closeConn()
    }
}