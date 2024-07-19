package kr.co.lion.modigm.db.join

import android.util.Log
import kr.co.lion.modigm.model.SqlUserData

class JoinUserDataSource {

    private val dao = JoinUserDao()

    //사용자 정보 저장
    suspend fun insetUserData(userInfoData: SqlUserData): Boolean{
        return try {
            dao.insertUserData(userInfoData.toMap())
        }catch (error: Exception){
            Log.e("Modigm_Error","insetUserData() error : $error")
            false
        }
    }

    suspend fun closeConn(){
        dao.closeConn()
    }

}