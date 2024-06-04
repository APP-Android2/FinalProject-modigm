package kr.co.lion.modigm.repository

import android.content.Context
import android.widget.ImageView
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.UserData

class StudyRepository {
    private val remoteStudyDataSource = RemoteStudyDataSource()

    // 스터디 시퀀스 값을 가져온다.
    suspend fun getStudySequence() = remoteStudyDataSource.getStudySequence()

    // 스터디 시퀀스 값을 업데이트한다.
    suspend fun updateStudySequence(studySequence: Int) = remoteStudyDataSource.updateStudySequence(studySequence)

    // 전체 스터디 목록을 가져온다.
    suspend fun getStudyAllData() = remoteStudyDataSource.getStudyAllData()

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다.
    suspend fun getStudyStateTrueData() = remoteStudyDataSource.getStudyStateTrueData()

    // 내 스터디 목록을 가져온다. (홈화면 내 스터디 접근 시)
    suspend fun getStudyMyData() = remoteStudyDataSource.getStudyMyData()


    // 스터디 정보 가져오기
    suspend fun selectContentData(studyIdx:Int) = remoteStudyDataSource.selectContentData(studyIdx)

    // uid를 사용해서 사용자 정보 가져오기
    suspend fun loadUserDetailsByUid(uid: String): UserData? {
        return remoteStudyDataSource.loadUserDetailsByUid(uid)
    }

    suspend fun updateStudyCanApplyByStudyIdx(studyIdx: Int, canApply: Boolean) = remoteStudyDataSource.updateStudyCanApplyByStudyIdx(studyIdx, canApply)

    suspend fun loadStudyThumbnail(context: Context, imageFileName: String, imageView: ImageView) =
        remoteStudyDataSource.loadStudyThumbnail(context, imageFileName, imageView)

    // 사용자가 참여한 스터디 목록을 가져온다. (프로필 화면)
    suspend fun loadStudyPartDataByUid(uid: String) = remoteStudyDataSource.loadStudyPartData(uid)

    // 사용자가 진행한 스터디 목록을 가져온다. (프로필 화면)
    suspend fun loadStudyHostDataByUid(uid: String) = remoteStudyDataSource.loadStudyHostData(uid)

}