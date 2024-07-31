package kr.co.lion.modigm.db.join

import kr.co.lion.modigm.model.SqlUserData

class RemoteJoinUserDataSource {

    private val dao = RemoteJoinUserDao()

    //사용자 정보 저장
    suspend fun insetUserData(userInfoData: SqlUserData): Result<Int>
        = dao.insertUserData(userInfoData.toMap())

    suspend fun closeConn(){
        dao.closeConn()
    }

}