package kr.co.lion.modigm.model

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
    fun toMap(): Map<String, Any>{
        val map = mutableMapOf<String, Any>()
        map["studyTitle"] = this.studyTitle
        map["studyContent"] = this.studyContent
        map["studyType"] = this.studyType
        map["studyPeriod"] = this.studyPeriod
        map["studyOnOffline"] = this.studyOnOffline
        map["studyDetailPlace"] = this.studyDetailPlace
        map["studyPlace"] = this.studyPlace
        map["studyApplyMethod"] = this.studyApplyMethod
        map["studyCanApply"] = this.studyCanApply
        map["studyPic"] = this.studyPic
        map["studyMaxMember"] = this.studyMaxMember
        map["studyState"] = this.studyState
        map["userIdx"] = this.userIdx
        return map
    }

    companion object{
        fun fromMap(map: Map<String, Any>): SqlStudyData{
            val studyData = SqlStudyData()
            studyData.studyTitle = map["studyTitle"] as String
            studyData.studyContent = map["studyContent"] as String
            studyData.studyType = map["studyType"] as String
            studyData.studyPeriod = map["studyPeriod"] as String
            studyData.studyOnOffline = map["studyOnOffline"] as String
            studyData.studyDetailPlace = map["studyDetailPlace"] as String
            studyData.studyPlace = map["studyPlace"] as String
            studyData.studyApplyMethod = map["studyApplyMethod"] as String
            studyData.studyCanApply = map["studyCanApply"] as String
            studyData.studyPic = map["studyPic"] as String
            studyData.studyMaxMember = map["studyMaxMember"] as Int
            studyData.studyState = map["studyState"] as Boolean
            studyData.userIdx = map["userIdx"] as Int
            return studyData
        }
    }

}