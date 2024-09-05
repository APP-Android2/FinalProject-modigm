package kr.co.lion.modigm.model

import java.sql.ResultSet

data class FavoriteData(
    val favoriteIdx : Int = -1,
    val studyIdx : Int = -1,
    val userIdx : Int = -1,
){
    companion object {
        /**
         * ResultSet에서 FavoriteData 객체를 생성하는 메서드
         * @param resultSet 조회된 ResultSet
         * @return FavoriteData 객체
         */
        fun getFavoriteData(resultSet: ResultSet): FavoriteData {
            return FavoriteData(
                favoriteIdx = resultSet.getInt("favoriteIdx"),
                userIdx = resultSet.getInt("userIdx"),
                studyIdx = resultSet.getInt("studyIdx")
            )
        }
    }
}
