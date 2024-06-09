package kr.co.lion.modigm.ui.write.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.ChatRoomRepository
import kr.co.lion.modigm.repository.StudyRepository

class WriteViewModel : ViewModel() {
    // 스터디 Repository
    val studyRepository = StudyRepository()
    // 채팅 Repository
    val chatRoomRepository = ChatRoomRepository()

    // ----------------- Remote Data Source에 전송할 내용 -----------------
    // StudyData 객체 생성
    private val _studyData = MutableLiveData<StudyData>()
    val studyData: LiveData<StudyData> = _studyData

    // 데이터에서 가져올 부분
    private val _studyIdx = MutableLiveData<Int>() // 글 고유번호
    val studyIdx: LiveData<Int> = _studyIdx

    private val _studyDetailPlace = MutableLiveData<String>() // 오프라인 진행장소 상세 주소
    val studyDetailPlace: LiveData<String> = _studyDetailPlace

    private val _studyUIdList = MutableLiveData<MutableList<String>>() // 현재 참여자 목록
    val studyUIdList: LiveData<MutableList<String>> = _studyUIdList

    private val _chatIdx = MutableLiveData<Int>() // 연결된 채팅방 고유 번호
    val chatIdx: LiveData<Int> = _chatIdx

    private val _studyState = MutableLiveData<Boolean>() // 삭제 여부 (존재함, 삭제됨)
    val studyState: LiveData<Boolean> = _studyState

    private val _studyCanApply = MutableLiveData<Boolean>() // 모집상태(모집중, 모집완료)
    val studyCanApply: LiveData<Boolean> = _studyCanApply

    // 이 프래그먼트에 존재하는 부분
    // WriteField -> 활동타입 (스터디 : 1, 프로젝트 : 2, 공모전 : 3)
    private val _studyType = MutableLiveData<Int>()
    val studyType: LiveData<Int> = _studyType

    // WritePeriod -> 진행기간 (1개월 이하 : 1, 1개월 이상: 2, 3개월 이상 : 3, 6개월 이상 : 4)
    private val _studyPeriod = MutableLiveData<Int>()
    val studyPeriod: LiveData<Int> = _studyPeriod

    // WriteProceed -> 진행방식(1: 온라인, 2: 오프라인, 3: 온/오프 혼합)
    private val _studyOnOffline = MutableLiveData<Int>()
    val studyOnOffline: LiveData<Int> = _studyOnOffline

    // WriteProceed -> 진행장소
    private val _studyPlace = MutableLiveData<String>()
    val studyPlace: LiveData<String> = _studyPlace

    // WriteProceed -> 최대 인원수
    private val _studyMaxMember = MutableLiveData<Int>()
    val studyMaxMember: LiveData<Int> = _studyMaxMember

    // WriteSkill -> 신청방식 (1: 선착순, 2: 신청제)
    private val _studyApplyMethod = MutableLiveData<Int>()
    val studyApplyMethod: LiveData<Int> = _studyApplyMethod

    // WriteSkill -> 필요 기술스택 목록
    private val _studySkillList = MutableLiveData<MutableList<Int>?>()
    val studySkillList: LiveData<MutableList<Int>?> = _studySkillList

    // WriteIntro -> 썸네일 사진
    private val _studyPic = MutableLiveData<String>()
    val studyPic: LiveData<String> = _studyPic

    // WriteIntro -> 글 제목
    private val _studyTitle = MutableLiveData<String>()
    val studyTitle: LiveData<String> = _studyTitle

    // WriteIntro -> 글 내용
    private val _studyContent = MutableLiveData<String>()
    val studyContent: LiveData<String> = _studyContent

    // 글 작성자 Uid
    private val _studyWriteUid = MutableLiveData<String>()
    val studyWriteUid: LiveData<String> = _studyWriteUid

    // ----------------- 입력 상태 반환 -----------------

    // 탭 - 분야
    private val _fieldClicked = MutableLiveData<Boolean>()
    val fieldClicked: LiveData<Boolean> = _fieldClicked

    // 탭 - 기간
    private val _periodClicked = MutableLiveData<Boolean>()
    val periodClicked: LiveData<Boolean> = _periodClicked

    // 탭 - 진행방식
    private val _proceedClicked = MutableLiveData<Boolean>()
    val proceedClicked: LiveData<Boolean> = _proceedClicked

    // 탭 - 기술
    private val _skillClicked = MutableLiveData<Boolean>()
    val skillClicked: LiveData<Boolean> = _skillClicked

    // 탭 - 소개
    private val _introClicked = MutableLiveData<Boolean>()
    val introClicked: LiveData<Boolean> = _introClicked


    // ----------------- 버튼 상태 반환 -----------------

    // 버튼 활성화 / 비활성화 상태
    private val _buttonState = MutableLiveData<Boolean>()
    val buttonState: LiveData<Boolean> = _buttonState

    // 값 초기화
    init {
        viewModelScope.launch {
            // 버튼 상태 비활성화
            _buttonState.value = false

            // 클릭 상태 설정
            _fieldClicked.value = false
            _periodClicked.value = false
            _proceedClicked.value = false
            _skillClicked.value = false
            _introClicked.value = false
        }
    }

    fun userDidAnswer(tabName: String) {
        when (tabName) {
            "field" -> { _fieldClicked.value = true }

            "period" -> { _periodClicked.value = true }

            "proceed" -> { _proceedClicked.value = true }

            "skill" -> { _skillClicked.value = true }

            "intro" -> { _introClicked.value = true }
        }
        _buttonState.value = true
    }

    fun userDidNotAnswer(tabName: String) {
        when (tabName) {
            "field" -> { _fieldClicked.value = false }

            "period" -> { _periodClicked.value = false }

            "proceed" -> { _proceedClicked.value = false }

            "skill" -> { _skillClicked.value = false }

            "intro" -> { _introClicked.value = false }
        }
        _buttonState.value = false
    }

    fun activateButton() {
        _buttonState.value = true
    }

    fun deactivateButton() {
        _buttonState.value = false
    }

    // ----------------- 초기화 함수 -----------------
    fun initField() {
        _studyType.value = 0
    }

    fun initPeriod() {
        _studyPeriod.value = 0
    }

    fun initProceed() {
        _studyOnOffline.value = 0
        _studyPlace.value = ""
        _studyMaxMember.value = 0
    }

    fun initSkill() {
        _studyApplyMethod.value = 0
        _studySkillList.value = mutableListOf()
    }

    fun initIntro() {
//        _studyPic.value = ""
        _studyTitle.value = ""
        _studyContent.value = ""
    }
    // --------------------------------------------

    // ----------------- 입력 처리 함수 -----------------

    // studyField에서 입력받은 데이터 저장
    fun gettingStudyField(type: Int) {
        _studyType.value = type
    }

    // studyPeriod에서 입력받은 데이터 저장
    fun gettingStudyPeriod(period: Int) {
        _studyPeriod.value = period
    }

    // studyProceed에서 입력받은 데이터 저장
    fun gettingStudyProceed(onOffline: Int, place: String, max: Int){
        _studyOnOffline.value = onOffline
        _studyPlace.value = place
        _studyMaxMember.value = max
    }

    // studySkill에서 입력받은 데이터 저장
    fun gettingStudySkill(method: Int, skillList: MutableList<Int>){
        _studyApplyMethod.value = method
        _studySkillList.value = skillList
    }
    // studyIntro에서 입력받은 데이터 저장
    fun gettingStudyIntro(picture: String, title: String, content: String){
        _studyPic.value = picture
        _studyTitle.value = title
        _studyContent.value = content
    }

    // 완료 버튼 활성화 함수
    fun buttonFinalStateActivation(): Boolean {
        return (fieldClicked.value == true
                && periodClicked.value == true
                && proceedClicked.value == true
                && skillClicked.value == true
                && introClicked.value == true)
    }

    // --------------------------------------------


    // ---------------- 입력된 리스트에서 해당 데이터를 찾아서 제거 ------------------

    // 필요한 기술 스택(_studySKillList)에서  X 버튼 클릭 시 해당 데이터 제거
    fun removeStudySkill(skill: Int){
        // 필요한 기술 스택 리스트를 불러온다
        val skillList = studySkillList.value

        // skill에 해당하는 내용이 있는지 확인한다
        // 비어있는 리스트가 아니면
        if (skillList != null){
            // skill에 해당하는 내용을 가지고 있다면
            if (skillList.contains(skill)){
                // 해당 내용을 제거한다
                skillList.removeIf { it == skill }
                // 비어있는 리스트가 되었다면!? -> 사용자 입력 false 처리
                if (skillList == null){
                    _skillClicked.value = false
                } else {
                    // 제거한 내용을 다시 필요한 기술 스택 리스트에 저장해준다
                    _studySkillList.value = skillList
                }
            }
            // skill에 해당하는 내용을 가지고 있지 않다면
            else {
                Log.d("TedMoon", "${skill}에 해당하는 내용이 필요한 기술 스택 리스트에 없습니다!")
            }
        } else {
            Log.d("TedMoon", "필요한 기술 스택 리스트에 아무것도 없습니다!")
        }
    }

    // ------------------------------------------------------------------
    // ----------------- ViewModel에 필요한 항목들 불러오기 -------------------

    // 글 고유번호(studyIdx)
    fun gettingStudyIdx() = viewModelScope.launch {
        try {
            // 스터디 시퀀스 값을 가져온다
            val studySequence = studyRepository.getStudySequence()
            // 스터디 시퀀스 값을 업데이트 한다
            studyRepository.updateStudySequence(studySequence + 1)
            // 저장할 값을 담아준다
            _studyIdx.value = studySequence + 1
        } catch (e: Exception){
            Log.e("Firebase Error", "Error dbUpdateStudySequence : ${e.message}")
        }
    }
    // 모집상태(studyCanApply)  -> default = true
    fun gettingStudyCanApply(){
        _studyCanApply.value = true
    }
    // 현재 참여자 목록(studyUidList) -> List<String> / List[0] = 진행자
    fun gettingStudyUidList() = viewModelScope.launch {

        // 현재 사용자 uid 받아오기
        val uid = gettingCurrentUid()
        // 리스트 만들기
        val uidList = mutableListOf(uid)

        // 리스트에 답아준다
        _studyUIdList.value = uidList
    }
    // 연결된 채팅방 고유번호(chatIdx)
    fun gettingChatIdx() = viewModelScope.launch {
        try {
            // 채팅방 시퀀스 값을 가져온다
            val chatRoomSequence = chatRoomRepository.getChatRoomSequence()
            // 채팅방 시퀀스 값을 업데이트해서 서버로 올려준다
            chatRoomRepository.updateChatRoomSequence(chatRoomSequence + 1)
            // 저장할 값을 담아준다
            _chatIdx.value = chatRoomSequence + 1
        } catch (e: Exception){
            Log.e("Firebase Error", "Error dbUpdateStudySequence : ${e.message}")
        }
    }
    // 글 삭제 여부(studyState) -> default = true(유효)
    fun gettingStudyState(){
        _studyState.value = true
    }
    // 글 작성자(studyWriteUid)
    fun gettingCurrentUid(): String {
        // auth 접근
        val auth = FirebaseAuth.getInstance()
        // 현재 사용자 uid 받아오기
        val uid = auth.currentUser?.uid.toString()
        // 값을 담아준다
        _studyWriteUid.value = uid
        return uid
    }

    // --------------------------------------------

    // ----------------- 스터디 데이터 저장 -------------------

    // WriteFieldFragment 입력받은 데이터 불러오기(

    // StudyData 생성
    suspend fun saveStudyData() {
        // studyIdx 불러오기
        val studySequence = studyRepository.getStudySequence()
        studyRepository.updateStudySequence(studySequence + 1)

        // chatIdx 불러오기
        val chatSequence = chatRoomRepository.getChatRoomSequence()
        chatRoomRepository.updateChatRoomSequence(chatSequence + 1)


        val studyIdx = studySequence + 1
        val studyTitle = _studyTitle.value
        val studyContent = _studyContent.value
        val studyType = _studyType.value
        val studyPeriod = _studyPeriod.value
        val studyOnOffLine = _studyOnOffline.value
        val studyPlace = _studyPlace.value
        val studyDetailPlace = _studyDetailPlace.value
        val studyApplyMethod = _studyApplyMethod.value
        val studySkillList = _studySkillList.value
        val studyCanApply = _studyCanApply.value
        val studyPicture = _studyPic.value
        val studyMaxMember = _studyMaxMember.value
        val studyUidList = _studyUIdList.value
        val chatIdx = chatSequence + 1
        val studyState = _studyState.value
        val studyWriteUid = _studyWriteUid.value


        return
    }

    // studyIdx 반환
    fun returnStudyIdx(): Int{
        return studyIdx.value?: -1
    }


    // ----------------- Repository에 데이터 전송 -----------------

    // StudyData를 Repository에 전송
    suspend fun uploadStudyData(){
        // StudyData 불러오기
        val studyData = saveStudyData()
        // repository에 전송
//        val uploadData = studyRepository.uploadStudyData(studyData)
//        Log.d("uploadData", "${uploadData}")
    }

    // ---------------------------------------------------------
}