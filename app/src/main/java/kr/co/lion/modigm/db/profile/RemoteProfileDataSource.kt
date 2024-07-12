package kr.co.lion.modigm.db.profile

import android.util.Log
import kr.co.lion.modigm.model.SqlUserData

class RemoteProfileDataSource {
    private val dao = RemoteProfileDao()

    // 모집 중인 전체 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun loadUserDataByUserIdx(userIdx: Int): SqlUserData? {
        var user: SqlUserData? = null

        try {
            user = dao.loadUserDataByUserIdx(userIdx)
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadUserDataByUserIdx(): $error")
        }

        return user
    }

    // 사용자 정보를 수정
    suspend fun updateUserData(user: SqlUserData) {
        try {
            dao.updateUserData(user)
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "updateUserData(): $error")
        }
    }

    // 사용자 정보를 수정
    suspend fun updateUserListData(userIdx: Int, linkList: List<String>) {
        try {
            dao.updateUserListData(userIdx, linkList)
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "updateUserData(): $error")
        }
    }
}