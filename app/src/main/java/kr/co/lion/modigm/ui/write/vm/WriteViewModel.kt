package kr.co.lion.modigm.ui.write.vm

import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.SeekBarBindingAdapter.setProgress
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData

class WriteViewModel : ViewModel() {


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
    private val _studySkillList = MutableLiveData<List<String>>()
    val studySkillList: LiveData<List<String>> = _studySkillList

    // WriteIntro -> 썸네일 사진
    private val _studyPic = MutableLiveData<String>()
    val studyPic: LiveData<String> = _studyPic

    // WriteIntro -> 글 제목
    private val _studyTitle = MutableLiveData<String>()
    val studyTitle: LiveData<String> = _studyTitle

    // WriteIntro -> 글 내용
    private val _studyContent = MutableLiveData<String>()
    val studyContent: LiveData<String> = _studyContent

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
    // ----------------- 초기화 함수 -----------------
    fun initField(){ _studyType.value = 0 }
    fun initPeriod(){ _studyPeriod.value = 0 }
    fun initProceed(){
        _studyOnOffline.value = 0
        _studyPlace.value = ""
        _studyMaxMember.value = 0
    }
    fun initSkill(){
        _studyApplyMethod.value = 0
        _studySkillList.value = listOf()
    }
    fun initIntro(){
        _studyPic.value = ""
        _studyTitle.value = ""
        _studyContent.value = ""
    }
    // --------------------------------------------

    // ----------------- 입력 처리 함수 -----------------

    // 완료 버튼 활성화 함수
    fun buttonFinalStateActivation(): Boolean{
        return (fieldClicked.value == true
                && periodClicked.value == true
                && proceedClicked.value == true
                && skillClicked.value == true
                && introClicked.value == true)
    }

    // 활동타입 저장
    fun gettingStudyType(type: Int){
        _studyType.value = type
    }

    // 활동기간 저장
    fun gettingStudyPeriod(period: Int){
        _studyPeriod.value = period
    }
    //

    // 스터디 데이터를 저장한다
    fun saveStudyData(){
        val studyType = studyType.value
        val studyPeriod = studyPeriod.value
        val studyOnOffLine = studyOnOffline.value
        var studyData = StudyData()

    }

    fun userDidAnswer(tabName: String) {
        when (tabName) {
            "field" -> {
                _fieldClicked.value = true
            }

            "period" -> {
                _periodClicked.value = true
            }

            "proceed" -> {
                _proceedClicked.value = true
            }

            "skill" -> {
                _skillClicked.value = true
            }

            "intro" -> {
                _introClicked.value = true
            }
        }
        _buttonState.value = true
    }

    fun userDidNotAnswer(tabName: String) {
        when (tabName) {
            "field" -> {
                _fieldClicked.value = false
            }

            "period" -> {
                _periodClicked.value = false
            }

            "proceed" -> {
                _proceedClicked.value = false
            }

            "skill" -> {
                _skillClicked.value = false
            }

            "intro" -> {
                _introClicked.value = false
            }
        }
        _buttonState.value = false
    }

    fun activateButton() {
        _buttonState.value = true
    }

    fun deactivateButton() {
        _buttonState.value = false
    }

}