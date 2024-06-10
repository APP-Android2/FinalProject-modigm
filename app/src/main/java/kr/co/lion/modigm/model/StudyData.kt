package kr.co.lion.modigm.model

data class StudyData(
    var studyIdx: Int = -1,                     // 글 고유 번호
    var studyTitle: String = "",                // 글 제목
    var studyContent: String = "",              // 글 내용
    var studyType: Int = 0,                     // 활동 타입 (스터디, 프로젝트, 공모전)
    var studyPeriod: Int = 0,                   // 진행 기간
    var studyOnOffline: Int = 0,                // 진행 방식 (온라인, 오프라인, 온/오프 혼합)
    var studyPlace: String = "",                // 오프라인 시 진행 장소
    var studyDetailPlace: String = "",          // 오프라인 시 진행 장소 상세 주소
    var studyApplyMethod: Int = 0,              // 신청 방식 (선착순, 신청제)
    var studySkillList: List<Int> = listOf(),   // 필요 기술 스택 목록
    var studyCanApply: Boolean = true,          // 모집 상태 (모집 중, 모집 완료)
    var studyPic: String = "",                  // 썸네일 사진
    var studyMaxMember: Int = 0,                // 최대 인원수
    var studyUidList: List<String> = listOf(),  // 현재 참여 목록
    var chatIdx: Int = -1,                      // 연결된 채팅방 고유번호
    var studyState: Boolean = true,             // 삭제 여부 (존재함, 삭제됨)
    var studyWriteUid: String = "",             // 글 작성자 uid
    var studyApplyList: List<String> = listOf(),// 스터디 신청자 리스트
    var studyLikeState: Boolean = false         // 스터디 좋아요 상태(좋아요, 좋아요 취소)
)