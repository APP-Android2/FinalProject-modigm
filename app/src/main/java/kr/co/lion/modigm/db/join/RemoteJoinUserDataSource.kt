package kr.co.lion.modigm.db.join

import kr.co.lion.modigm.model.SqlUserData

class RemoteJoinUserDataSource {

    private val dao = RemoteJoinUserDao()

    //사용자 정보 저장
    suspend fun insetUserData(userInfoData: SqlUserData): Result<Int>
        = dao.insertUserData(userInfoData.toMap())

    // 해당 전화 번호의 계정이 있는지 확인 (중복 확인)
    suspend fun checkUserByPhone(phone: String): Result<Map<String, String>?>
        = dao.checkUserByPhone(phone)

}