package kr.co.lion.modigm.model

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp

data class StudyData(
    val studyIdx: Int = -1,                                             // 스터디 아이디
    val studyTitle: String = "",                                        // 제목
    val studyContent: String = "",                                      // 내용
    val studyType: String = "",                                         // 활동 타입 (스터디, 프로젝트, 공모전)
    val studyPeriod: String = "",                                       // 진행 기간
    val studyOnOffline: String = "",                                    // 진행 방식 (온라인, 오프라인, 온/오프 혼합)
    val studyDetailPlace: String = "",                                  // 오프라인 시 진행 장소 상세 주소
    val studyPlace: String = "",                                        // 오프라인 시 진행 장소
    val studyApplyMethod: String = "",                                  // 신청 방식 (신청제) 선착순은 보류
    val studyCanApply: String = "",                                     // 모집 상태 (모집 중, 모집 완료)
    val studyPic: String = "",                                          // 썸네일 사진
    val studyMaxMember: Int = 0,                                        // 최대 인원수
    val studyState: Boolean = true,                                     // 삭제 여부 (존재함, 삭제됨)
    val studyChatLink: String = "",                                     // 오픈 채팅 링크
    val studyCreateDate: LocalDateTime = LocalDateTime.now(),           // 생성 날짜
    val userIdx: Int = -1,                                              // 사용자 번호
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
        map["studyChatLink"] = this.studyChatLink
        map["studyCreateDate"] = this.studyCreateDate
        map["userIdx"] = this.userIdx
        return map
    }


    companion object{
        fun fromMap(map: Map<String, Any>): StudyData {
            return StudyData(
                studyIdx = map["studyIdx"] as Int,
                studyTitle = map["studyTitle"] as String,
                studyContent = map["studyContent"] as String,
                studyType = map["studyType"] as String,
                studyPeriod = map["studyPeriod"] as String,
                studyOnOffline = map["studyOnOffline"] as String,
                studyDetailPlace = map["studyDetailPlace"] as String,
                studyPlace = map["studyPlace"] as String,
                studyApplyMethod = map["studyApplyMethod"] as String,
                studyCanApply = map["studyCanApply"] as String,
                studyPic = map["studyPic"] as String,
                studyMaxMember = map["studyMaxMember"] as Int,
                studyState = map["studyState"] as Boolean,
                studyChatLink = map["studyChatLink"] as String,
                studyCreateDate = map["studyCreateDate"] as LocalDateTime,
                userIdx = map["userIdx"] as Int
            )
        }

        private fun getLocalDate(timestamp: Timestamp): LocalDateTime {
            return LocalDateTime.ofEpochSecond(
                timestamp.time / 1000, // 밀리초를 초 단위로 변환
                (timestamp.time % 1000 * 1000000).toInt(), // 나머지 밀리초를 나노초로 변환
                org.threeten.bp.ZoneOffset.UTC // 필요에 따라 ZoneOffset 설정
            )
        }

        fun getStudyData(resultSet: ResultSet): StudyData {
            return StudyData(
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
                resultSet.getString("studyChatLink"),
                getLocalDate(resultSet.getTimestamp("studyCreateDate")),
                resultSet.getInt("userIdx"),
            )
        }

        // StudyData 객체를 PreparedStatement에 매핑하는 함수
        fun setPreparedStatement(statement: PreparedStatement, studyData: StudyData) {
            // StudyData 객체의 데이터를 Map 형태로 변환
            val params = studyData.toMap().values.toList()

            // 각 데이터를 PreparedStatement에 할당
            params.forEachIndexed { index, value ->
                when (value) {
                    is String -> statement.setString(index + 1, value)
                    is Int -> statement.setInt(index + 1, value)
                    is Boolean -> statement.setBoolean(index + 1, value)
                    is LocalDateTime -> {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val formattedDateTime = value.format(formatter)
                        statement.setTimestamp(index + 1, Timestamp.valueOf(formattedDateTime))
                    }
                    else -> throw IllegalArgumentException("Unsupported type: ${value::class.simpleName}")
                }
            }
        }
    }
}