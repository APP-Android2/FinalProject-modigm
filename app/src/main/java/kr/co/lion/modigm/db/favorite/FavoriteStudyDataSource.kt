package kr.co.lion.modigm.db.favorite

import kr.co.lion.modigm.model.SqlStudyData

class FavoriteStudyDataSource {

    private val tag = "FavoriteStudyDataSource"

    private val dao = FavoriteStudyDao()

    suspend fun getMyFavoriteStudyDataList(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> {
        return dao.selectMyFavoriteStudyDataList(userIdx)
    }
    // 리소스를 해제하는 메서드 추가
    suspend fun close() {
        dao.close()
    }
}
