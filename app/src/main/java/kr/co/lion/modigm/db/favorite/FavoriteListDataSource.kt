package kr.co.lion.modigm.db.favorite

import kr.co.lion.modigm.model.SqlStudyData

class FavoriteListDataSource {

    private val dao = FavoriteListDao()

    suspend fun fetchFavoriteStudies(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> {
        return dao.selectFavoriteStudies(userIdx)
    }

    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean {
        return dao.toggleFavorite(userIdx, studyIdx)
    }
}
