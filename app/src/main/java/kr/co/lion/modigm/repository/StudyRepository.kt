package kr.co.lion.modigm.repository

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class StudyRepository {
    private val remoteStudyDataSource = RemoteStudyDataSource()

    // 전체 스터디 목록을 가져온다.
    suspend fun getStudyAllData() = remoteStudyDataSource.getStudyAllData()

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다.
    suspend fun getStudyStateTrueData() = remoteStudyDataSource.getStudyStateTrueData()

    // 내 스터디 목록을 가져온다. (홈화면 내 스터디 접근 시)
    suspend fun getStudyMyData(currentUserUid:String) = remoteStudyDataSource.getStudyMyData(currentUserUid)

    suspend fun loadStudyThumbnail(context: Context, imageFileName: String, imageView: ImageView) =
        remoteStudyDataSource.loadStudyThumbnail(context, imageFileName, imageView)

    // 사용자가 참여한 스터디 목록을 가져온다. (프로필 화면)
    suspend fun loadStudyPartDataByUid(uid: String) = remoteStudyDataSource.loadStudyPartData(uid)

    // 사용자가 진행한 스터디 목록을 가져온다. (프로필 화면)
    suspend fun loadStudyHostDataByUid(uid: String) = remoteStudyDataSource.loadStudyHostData(uid)

    // 스터디 정보 가져오기
    suspend fun selectContentData(studyIdx:Int) = remoteStudyDataSource.selectContentData(studyIdx)

    // uid를 사용해서 사용자 정보 가져오기
    suspend fun loadUserDetailsByUid(uid: String): UserData? {
        return remoteStudyDataSource.loadUserDetailsByUid(uid)
    }

    suspend fun updateStudyCanApplyByStudyIdx(studyIdx: Int, canApply: Boolean) = remoteStudyDataSource.updateStudyCanApplyByStudyIdx(studyIdx, canApply)

    // 스터디 데이터 업데이트
    suspend fun updateStudyDataByStudyIdx(studyIdx: Int, updatedStudyData: Map<String, Any>) =
        remoteStudyDataSource.updateStudyDataByStudyIdx(studyIdx, updatedStudyData)


    // 스터디 커버
    suspend fun loadStudyPicUrl(studyPic: String): Result<Uri> {
        return try {
            val uri = remoteStudyDataSource.loadStudyPicUrl(studyPic)
            if (uri != null) Result.success(uri) else Result.failure(Exception("Image not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 유저 프로필
    suspend fun loadUserPicUrl(userProfilePic: String): Result<Uri> {
        return try {
            val uri = remoteStudyDataSource.loadUserPicUrl(userProfilePic)
            if (uri != null) Result.success(uri) else Result.failure(Exception("Image not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudyUidListByStudyIdx(studyIdx: Int): List<String>? {
//        return remoteStudyDataSource.selectContentData(studyIdx)?.studyUidList
        return null
    }

    suspend fun getUserDetailsByUid(uid: String): UserData? {
        return remoteStudyDataSource.loadUserDetailsByUid(uid)
    }

    suspend fun updateStudyUserList(userUid: String, studyIdx: Int): Boolean {
        return remoteStudyDataSource.updateStudyUserList(userUid, studyIdx)
    }

    suspend fun addLike(uid: String, studyIdx: Int) {
        remoteStudyDataSource.addLike(uid, studyIdx)
    }

    suspend fun removeLike(uid: String, studyIdx: Int) {
        remoteStudyDataSource.removeLike(uid, studyIdx)
    }


    // 특정 studyIdx에 대한 스터디 정보를 가져오고 studyState를 업데이트한다.
    suspend fun updateStudyStateByStudyIdx(studyIdx: Int, newState: Boolean) {
        remoteStudyDataSource.updateStudyStateByStudyIdx(studyIdx, newState)
    }


    // 스터디 정보 업로드
    suspend fun uploadStudyData(studyData: StudyData) = remoteStudyDataSource.uploadStudyData(studyData)


    suspend fun applyToStudy(studyIdx: Int, uid: String) {
        remoteStudyDataSource.addToApplyList(studyIdx, uid)
    }

    suspend fun joinStudy(studyIdx: Int, uid: String) {
        remoteStudyDataSource.addToStudyUidList(studyIdx, uid)
    }

    fun fetchStudyApplyMembers(studyIdx: Int, callback: (List<UserData>) -> Unit) {
        remoteStudyDataSource.getStudyApplyList(studyIdx) { userIds ->
            if (userIds.isNotEmpty()) {
                remoteStudyDataSource.getUsersByIds(userIds) { users ->
                    callback(users)
                }
            } else {
                callback(emptyList())
            }
        }
    }

    fun removeUserFromStudyApplyList(studyIdx: Int, userUid: String, callback: (Boolean) -> Unit) {
        remoteStudyDataSource.removeUserFromStudyApplyList(studyIdx, userUid, callback)
    }

    fun addUserToStudyUidList(studyIdx: Int, userUid: String, callback: (Boolean) -> Unit) {
        remoteStudyDataSource.addUserToStudyUidList(studyIdx, userUid, callback)
    }
}