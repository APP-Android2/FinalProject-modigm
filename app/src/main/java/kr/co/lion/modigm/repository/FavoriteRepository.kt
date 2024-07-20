package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.favorite.FavoriteListDataSource
import kr.co.lion.modigm.model.SqlStudyData

class FavoriteRepository {

    private val dataSource = FavoriteListDataSource()

    suspend fun getFavoriteStudies(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> {
        return dataSource.fetchFavoriteStudies(userIdx)
    }

    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean {
        return dataSource.toggleFavorite(userIdx, studyIdx)
    }
}
