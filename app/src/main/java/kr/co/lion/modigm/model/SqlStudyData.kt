package kr.co.lion.modigm.model

import java.sql.ResultSet

data class SqlStudyData(
    var studyIdx: Int = -1,                     // 스터디 아이디
    var studyTitle: String = "",                // 제목
    var studyContent: String = "",              // 내용
    var studyType: String = "",                 // 활동 타입 (스터디, 프로젝트, 공모전)
    var studyPeriod: String = "",               // 진행 기간
    var studyOnOffline: String = "",            // 진행 방식 (온라인, 오프라인, 온/오프 혼합)
    var studyDetailPlace: String = "",          // 오프라인 시 진행 장소 상세 주소
    var studyPlace: String = "",                // 오프라인 시 진행 장소
    var studyApplyMethod: String = "",          // 신청 방식 (선착순, 신청제)
    var studyCanApply: String = "",             // 모집 상태 (모집 중, 모집 완료)
    var studyPic: String = "",                  // 썸네일 사진
    var studyMaxMember: Int = 0,                // 최대 인원수
    var studyState: Boolean = true,             // 삭제 여부 (존재함, 삭제됨)
    var userIdx: Int = -1,                      // 사용자 번호
){
    fun getColumns(): Array<String>{
        val columns = arrayOf(
            "studyTitle",
            "studyContent",
            "studyType",
            "studyPeriod",
            "studyOnOffline",
            "studyDetailPlace",
            "studyPlace",
            "studyApplyMethod",
            "studyCanApply",
            "studyPic",
            "studyMaxMember",
            "studyState",
            "userIdx",
        )
        return columns
    }

    fun getValues(): Array<Any>{
        val values = arrayOf<Any>(
            this.studyTitle,
            this.studyContent,
            this.studyType,
            this.studyPeriod,
            this.studyOnOffline,
            this.studyDetailPlace,
            this.studyPlace,
            this.studyApplyMethod,
            this.studyCanApply,
            this.studyPic,
            this.studyMaxMember,
            this.studyState,
            this.userIdx,
        )
        return values
    }

    companion object{
        fun getStudyData(resultSet: ResultSet): SqlStudyData{
            return SqlStudyData(
                resultSet.getInt("studyIdx"),
                resultSet.getString("studyTitle"),
                resultSet.getString("studyContent"),
                resultSet.getString("studyType"),
                resultSet.getString("studyPeriod"),
                resultSet.getString("studyOnOffline"),
                resultSet.getString("studyDetailPlace"),
                resultSet.getString("studyPlace"),
                resultSet.getString("studyApplyMethod"),
                resultSet.getString("studyCanApply"),
                resultSet.getString("studyPic"),
                resultSet.getInt("studyMaxMember"),
                resultSet.getBoolean("studyState"),
                resultSet.getInt("userIdx"),
            )
        }
    }

}