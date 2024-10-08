package kr.co.lion.modigm.db.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class RemoteProfileDataSource {
    private val dao = RemoteProfileDao()

    // userIdx를 통해 사용자 정보를 가져오는 메서드
    suspend fun loadUserDataByUserIdx(userIdx: Int): UserData? {
        var user: UserData? = null

        try {
            user = dao.loadUserDataByUserIdx(userIdx)
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadUserDataByUserIdx(): $error")
        }

        return user
    }

    // userIdx를 통해 등록된 링크 목록을 가져오는 메서드
    suspend fun loadUserLinkDataByUserIdx(userIdx: Int): List<String> {
        try {
            val linkList = dao.loadUserLinkDataByUserIdx(userIdx)
            return linkList
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadUserDataByUserIdx(): $error")
            return emptyList()
        }
    }

    // 사용자 정보를 수정
    suspend fun updateUserData(user: UserData) {
        try {
            dao.updateUserData(user)
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "updateUserData(): $error")
        }
    }

    // 사용자 링크 정보를 수정
    suspend fun updateUserLinkData(userIdx: Int, linkList: List<String>) {
        try {
            dao.updateUserLinkData(userIdx, linkList)
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "updateUserListData(): $error")
        }
    }

    // 프로필 사진을 Amazon S3에 업로드
    suspend fun uploadProfilePic(uri: Uri, context: Context): String {
        try {
            val filePath = dao.uploadProfilePic(uri, context)
            return filePath
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "uploadProfilePic(): $error")
            return ""
        }
    }

    // 사용자가 진행한 스터디 목록 (3개만)
    suspend fun loadSmallHostStudyList(userIdx: Int): List<StudyData> {
        try {
            val studyList = dao.loadSmallHostStudyList(userIdx)
            return studyList
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadHostStudyList(): $error")
            return emptyList()
        }
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    suspend fun loadSmallPartStudyList(userIdx: Int): List<StudyData> {
        try {
            val studyList = dao.loadSmallPartStudyList(userIdx)
            return studyList
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadPartStudyList(): $error")
            return emptyList()
        }
    }

    // 사용자가 진행한 스터디 목록 (전체)
    suspend fun loadHostStudyList(userIdx: Int): List<StudyData> {
        try {
            val studyList = dao.loadHostStudyList(userIdx)
            return studyList
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadHostStudyList(): $error")
            return emptyList()
        }
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (전체)
    suspend fun loadPartStudyList(userIdx: Int): List<StudyData> {
        try {
            val studyList = dao.loadPartStudyList(userIdx)
            return studyList
        } catch (error: Exception) {
            Log.e("RemoteProfileDataSource", "loadPartStudyList(): $error")
            return emptyList()
        }
    }
}