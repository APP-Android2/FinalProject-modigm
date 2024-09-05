package kr.co.lion.modigm.ui.write.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.WriteStudyRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs


class WriteViewModel : ViewModel() {

    // 스터디 Repository
    private val writeStudyRepository = WriteStudyRepository()

    // 탭의 현재 선택 상태를 저장할 변수
    private val _selectedTabPosition = MutableLiveData(0)
    val selectedTabPosition: LiveData<Int> get() = _selectedTabPosition

    // 프로그래스바 상태를 저장할 변수
    private val _progressBarState = MutableLiveData(20)
    val progressBarState: LiveData<Int> get() = _progressBarState

    // 탭 선택 상태 업데이트
    fun updateSelectedTab(position: Int) {
        _selectedTabPosition.value = position
        _progressBarState.value = (position + 1) * 20 // 탭 위치에 따라 프로그래스바 업데이트
    }

    private val _isItemSelected = MutableLiveData<Boolean>()
    val isItemSelected: LiveData<Boolean> get() = _isItemSelected

    // MutableMap으로 데이터를 관리
    private val _writeDataMap = MutableLiveData<MutableMap<String, Any>>(mutableMapOf())
    val writeDataMap: LiveData<MutableMap<String, Any>> get() = _writeDataMap

    // 데이터를 업데이트하는 함수
    fun updateData(key: String, value: Any) {
        val currentMap = _writeDataMap.value ?: mutableMapOf()
        currentMap[key] = value
        _writeDataMap.value = currentMap // 변경된 값을 다시 할당하여 UI에 반영
    }

    // 데이터를 특정 키로 가져오는 함수
    fun getUpdateData(key: String): Any? {
        return _writeDataMap.value?.get(key)
    }

    private val _currentTabNum = MutableLiveData<Int>()
    val currentTabNum: LiveData<Int> get() = _currentTabNum


    fun updateCurrentTabNum(tabNum: Int) {
        _currentTabNum.value = tabNum

    }












    // 각 탭의 유효성 검사 상태를 저장하는 LiveData

    // 분야
    private val isFieldValid = MutableLiveData<Boolean>(false)
    // 기간
    private val isPeriodValid = MutableLiveData<Boolean>(false)
    // 진행 방식
    private val isProceedValid = MutableLiveData<Boolean>(false)
    // 기술
    private val isSkillValid = MutableLiveData<Boolean>(false)
    // 소개
    private val isIntroValid = MutableLiveData<Boolean>(false)

    var currentTab = 0 // 현재 탭의 위치를 저장

    // 진행방식, 장소, 최대 정원
    val studyOnOffline = MutableLiveData<String>("")
    val studyPlace = MutableLiveData<String>("")
    val studyDetailPlace = MutableLiveData<String>("")
    val studyMaxMember = MutableLiveData<Int>(0)

    // 분야 데이터 저장
    val selectedFieldTag = MutableLiveData<String>()

    // 기간 데이터 저장
    val selectedPeriodTag = MutableLiveData<String>()

    // 신청 방식 데이터 저장
    val selectedApplyTag = MutableLiveData<String>()

    // 이미지 URI 저장
    val studyPicUri = MutableLiveData<String>("")

    // 데이터 복원용
    val studyPic = MutableLiveData<String>("")

    // 기타 데이터
    val studyTitle = MutableLiveData<String>("")
    val studyContent = MutableLiveData<String>("")

    val studySkillList = MutableLiveData<List<Int>>(emptyList())


    // 각 탭의 유효성 검사 메서드
    // 분야
    fun validateField(isValid: Boolean) {
        isFieldValid.value = isValid
        updateIsItemSelected()
    }

    //기간
    fun validatePeriod(isValid: Boolean) {
        isPeriodValid.value = isValid
        updateIsItemSelected()
    }

    // 진행방식
    fun validateProceed(isValid: Boolean) {
        isProceedValid.value = isValid
        updateIsItemSelected()
    }

    // 기술
    fun validateSkill(isValid: Boolean) {
        isSkillValid.value = isValid
        updateIsItemSelected()
    }

    // 소개
    fun validateIntro(isValid: Boolean) {
        isIntroValid.value = isValid
        updateIsItemSelected()
    }

    // 각 탭의 유효성 검사 상태를 바탕으로 isItemSelected 업데이트
    private fun updateIsItemSelected() {
        _isItemSelected.value = when (currentTab) {
            0 -> isFieldValid.value ?: false
            1 -> isPeriodValid.value ?: false
            2 -> isProceedValid.value ?: false
            3 -> isSkillValid.value ?: false
            4 -> isIntroValid.value ?: false
            else -> false
        }
    }

    // 각 탭의 유효성 검사
    fun validateCurrentTab(): Boolean {
        return when (currentTab) {
            0 -> isFieldValid.value ?: false
            1 -> isPeriodValid.value ?: false
            2 -> isProceedValid.value ?: false
            3 -> isSkillValid.value ?: false
            4 -> isIntroValid.value ?: false
            else -> false
        }
    }

    // 진행방식, 장소, 최대 정원의 유효성 검사
    fun validateProceedInput() {
        val onOfflineValue = studyOnOffline.value ?: ""
        val isValid = when (onOfflineValue) {
            "온라인" -> (studyMaxMember.value ?: 0) in 1..30 // "온라인"인 경우 장소 유효성 검사 생략
            else -> {
                (studyMaxMember.value ?: 0) in 1..30 && studyPlace.value?.isNotEmpty() == true
            }
        }
        validateProceed(isValid)
    }

    // DB에 데이터 저장
    suspend fun saveDataToDB(): Int? {
        val userIdx = prefs.getInt("currentUserIdx", 0)

        return try {
            // 스터디 테이블에 저장
            val studyData = StudyData(
                studyTitle = studyTitle.value ?: "",
                studyContent = studyContent.value ?: "",
                studyType = selectedFieldTag.value ?: "",
                studyPeriod = selectedPeriodTag.value ?: "",
                studyOnOffline = studyOnOffline.value ?: "",
                studyPlace = studyPlace.value ?: "",
                studyDetailPlace = studyDetailPlace.value ?: "",
                studyApplyMethod = selectedApplyTag.value ?: "",
                studyCanApply = "모집중",
                studyPic = studyPicUri.value ?: "",
                studyMaxMember = studyMaxMember.value ?: 0,
                studyState = true,
                userIdx = userIdx,
            )
            val studyIdx = studySkillList.value?.let {
                writeStudyRepository.uploadStudyData(
                    userIdx,
                    studyData,
                    studySkillList.value?:listOf(),
                    studyPicUri.value
                )
            }
            studyIdx
        } catch (e: Exception) {
            Log.e("WriteViewModel", "Error saving data: ${e.message}")
            null
        }
    }
}