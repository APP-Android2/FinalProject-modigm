package kr.co.lion.modigm.ui.write.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.ChatRoomRepository
import kr.co.lion.modigm.repository.StudyRepository


class WriteViewModel : ViewModel() {
    // 스터디 Repository
    val studyRepository = StudyRepository()

    // 채팅 Repository
    val chatRoomRepository = ChatRoomRepository()

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
    val studyOnOffline = MutableLiveData<Int>(0)
    val studyPlace = MutableLiveData<String>("")
    val studyDetailPlace = MutableLiveData<String>("")
    val studyMaxMember = MutableLiveData<Int>(0)

    // 분야 데이터 저장
    val selectedFieldTag = MutableLiveData<Int>()

    // 기간 데이터 저장
    val selectedPeriodTag = MutableLiveData<Int>()

    // 신청 방식 데이터 저장
    val selectedApplyTag = MutableLiveData<Int>()

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

    // 파이어스토어에 데이터 저장
    suspend fun saveDataToFirestore(): Int? {
        return try {
            val sequence = studyRepository.getStudySequence() + 1
            val writeUidValue = writeUid.value ?: ""
            val chatSequence = chatRoomRepository.getChatRoomSequence()

            val studyData = StudyData(
                studyIdx = sequence,
                studyTitle = studyTitle.value ?: "",
                studyContent = studyContent.value ?: "",
                studyType = selectedFieldTag.value ?: 0,
                studyPeriod = selectedPeriodTag.value ?: 0,
                studyOnOffline = studyOnOffline.value ?: 0,
                studyPlace = studyPlace.value ?: "",
                studyDetailPlace = studyDetailPlace.value ?: "",
                studyApplyMethod = selectedApplyTag.value ?:0,
                studySkillList = studySkillList.value ?: emptyList(),
                studyCanApply = true,
                studyPic = studyPicUri.value ?: "",
                studyMaxMember = studyMaxMember.value ?: 0,
                studyUidList = listOf(writeUidValue),
                chatIdx = chatSequence,
                studyState = true,
                studyWriteUid = writeUidValue
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("Study").add(studyData).await()
            // 시퀀스 업데이트
            studyRepository.updateStudySequence(sequence)
            chatRoomRepository.updateChatRoomSequence(chatSequence)
            sequence
        } catch (e: Exception) {
            Log.e("WriteViewModel", "Error saving data: ${e.message}")
            null
        }
    }



}