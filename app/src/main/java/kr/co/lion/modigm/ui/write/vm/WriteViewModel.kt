package kr.co.lion.modigm.ui.write.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.repository.WriteStudyRepository
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.PreferenceUtil


class WriteViewModel : ViewModel() {
    // 스터디 Repository
    val writeStudyRepository = WriteStudyRepository()

//    private val prefs: PreferenceUtil = ModigmApplication.prefs

    private val _isItemSelected = MutableLiveData<Boolean>()
    val isItemSelected: LiveData<Boolean> get() = _isItemSelected


    // 각 탭의 유효성 검사 상태를 저장하는 LiveData

    // 분야
    val isFieldValid = MutableLiveData<Boolean>(false)
    // 기간
    val isPeriodValid = MutableLiveData<Boolean>(false)
    // 진행 방식
    val isProceedValid = MutableLiveData<Boolean>(false)
    // 기술
    val isSkillValid = MutableLiveData<Boolean>(false)
    // 소개
    val isIntroValid = MutableLiveData<Boolean>(false)

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

    // 작성자 uid
    val writeUid = MutableLiveData<String>("")

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
        val onOfflineValue = studyOnOffline.value ?: 0
        val isValid = (onOfflineValue != 0) &&
                ((studyMaxMember.value ?: 0) in 1..30) &&
                (onOfflineValue == 1 || studyPlace.value?.isNotEmpty() == true) // 온라인이 아닌 경우 장소 유효성 검사
        validateProceed(isValid)
    }

    // DB에 데이터 저장
    suspend fun saveDataToDB(): Int? {
        val userIdx = prefs.getString("userIdx", "0")
        return try {
            // 스터디 테이블에 저장
            val studyData = SqlStudyData(
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
                userIdx = userIdx.toInt(),
            )
            val studyIdx = studySkillList.value?.let {
                writeStudyRepository.uploadStudyData(
                    userIdx.toInt(),
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