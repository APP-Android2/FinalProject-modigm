package kr.co.lion.modigm.model

data class StudyData(
    val studyIdx: Int = -1,                     // 글 고유 번호
    val studyTitle: String = "",                // 글 제목
    val studyContent: String = "",              // 글 내용
    val studyType: Int = 0,                     // 활동 타입 (스터디, 프로젝트, 공모전)
    val studyPeriod: Int = 0,                   // 진행 기간
    val studyOnOffline: Int = 0,                // 진행 방식 (온라인, 오프라인, 온/오프 혼합)
    val studyPlace: String = "",                // 오프라인 시 진행 장소
    val studyDetailPlace: String = "",          // 오프라인 시 진행 장소 상세 주소
    val studyApplyMethod: Int = 0,              // 신청 방식 (선착순, 신청제)
    val studySkillList: List<Int> = listOf(),   // 필요 기술 스택 목록
    val studyCanApply: Boolean = true,          // 모집 상태 (모집 중, 모집 완료)
    val studyPic: String = "",                  // 썸네일 사진
    val studyMaxMember: Int = 0,                // 최대 인원수
    val studyUidList: List<String> = listOf(),  // 현재 참여 목록
    val chatIdx: Int = -1,                      // 연결된 채팅방 고유번호
    val studyState: Boolean = true,             // 삭제 여부 (존재함, 삭제됨)
    val studyWriteUid: String = "",             // 글 작성자 uid
)