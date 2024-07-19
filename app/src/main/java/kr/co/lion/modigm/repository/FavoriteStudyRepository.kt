package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.favorite.FavoriteStudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class FavoriteStudyRepository {

    private val favoriteStudyDataSource = FavoriteStudyDataSource()

    suspend fun getMyFavoriteStudyDataList(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> {
        return favoriteStudyDataSource.getMyFavoriteStudyDataList(userIdx)
    }

    // 리소스를 해제하는 메서드 추가
    suspend fun close() {
        favoriteStudyDataSource.close()
    }
}
