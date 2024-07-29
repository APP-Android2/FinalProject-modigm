package kr.co.lion.modigm.db.join

import android.util.Log
import kr.co.lion.modigm.model.SqlUserData

class RemoteJoinUserDataSource {

    private val dao = RemoteJoinUserDao()

    //사용자 정보 저장
    suspend fun insetUserData(userInfoData: SqlUserData): Int{
        return try {
            dao.insertUserData(userInfoData.toMap()) ?: 0
        }catch (error: Exception){
            Log.e("Modigm_Error","insetUserData() error : $error")
            0
        }
    }

    suspend fun closeConn(){
        dao.closeConn()
    }

}