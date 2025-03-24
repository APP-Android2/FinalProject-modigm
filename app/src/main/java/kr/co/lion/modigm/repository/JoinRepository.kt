package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.join.RemoteJoinUserDataSource
import kr.co.lion.modigm.model.UserData
import javax.inject.Inject


class JoinRepository @Inject constructor(
    private val _joinUserDataSource: RemoteJoinUserDataSource
) {

    // 회원가입
    suspend fun insetUserData(userInfoData: UserData): Result<Int>
        = _joinUserDataSource.insetUserData(userInfoData)

    // 해당 전화 번호의 계정이 있는지 확인 (중복 확인)
    suspend fun checkUserByPhone(phoneNumber: String): Result<Map<String, String>?>
        = _joinUserDataSource.checkUserByPhone(phoneNumber)

}