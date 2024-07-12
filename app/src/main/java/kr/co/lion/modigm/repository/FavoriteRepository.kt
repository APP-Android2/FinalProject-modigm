package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.favorite.RemoteFavoriteDataSource
import kr.co.lion.modigm.model.StudyData

class FavoriteRepository {
    private val remoteFavoriteDataSource = RemoteFavoriteDataSource()

    suspend fun getFavoriteStudies(uid: String): List<StudyData> {
        val studyIdxs = remoteFavoriteDataSource.fetchFavoriteStudies(uid)
        return remoteFavoriteDataSource.fetchStudyDetails(studyIdxs)
    }
}