package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.like.RemoteLikeDataSource
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.StudyData

class LikeRepository {
    private val remoteLikeDataSource = RemoteLikeDataSource()

    suspend fun getLikedStudies(uid: String): List<StudyData> {
        val studyIdxs = remoteLikeDataSource.fetchLikedStudies(uid)
        return remoteLikeDataSource.fetchStudyDetails(studyIdxs)
    }
}