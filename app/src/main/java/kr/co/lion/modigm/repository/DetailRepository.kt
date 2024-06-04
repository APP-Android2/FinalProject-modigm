package kr.co.lion.modigm.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kr.co.lion.modigm.db.study.RemoteDetailDataSource
import kr.co.lion.modigm.model.UserData

class DetailRepository {

    private val remoteDetailDataSource = RemoteDetailDataSource()

    // 스터디 정보 가져오기
    suspend fun selectContentData(studyIdx:Int) = remoteDetailDataSource.selectContentData(studyIdx)

    // uid를 사용해서 사용자 정보 가져오기
    suspend fun loadUserDetailsByUid(uid: String): UserData? {
        return remoteDetailDataSource.loadUserDetailsByUid(uid)
    }

    suspend fun updateStudyCanApplyByStudyIdx(studyIdx: Int, canApply: Boolean) = remoteDetailDataSource.updateStudyCanApplyByStudyIdx(studyIdx, canApply)


}