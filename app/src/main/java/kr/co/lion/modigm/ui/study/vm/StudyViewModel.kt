package kr.co.lion.modigm.ui.study.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository

class StudyViewModel : ViewModel() {

    private val studyRepository = StudyRepository()

    // 전체 스터디 목록 중 모집중인 스터디 리스트
    private val _studyStateTrueDataList = MutableLiveData<List<StudyData>>()
    val studyStateTrueDataList: LiveData<List<StudyData>> = _studyStateTrueDataList




    // 내 스터디 리스트
    private val _studyMyDataList = MutableLiveData<List<StudyData>>()
    val studyMyDataList: LiveData<List<StudyData>> = _studyMyDataList


    // 뷰모델 초기화 시
    init {
        viewModelScope.launch {
            getStudyStateTrueData()
            getStudyMyData()
        }
    }




    // 전체 스터디 목록 중 모집중인 스터디만 가져온다. (홈화면 전체 스터디 접근 시)
    fun getStudyStateTrueData() = viewModelScope.launch {
        try {
            val response = studyRepository.getStudyStateTrueData()

            _studyStateTrueDataList.value = response
            Log.d("테스트 vm1","${_studyStateTrueDataList.value.toString()}")
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error vmGetDeliveryDataByUserIdx : ${e.message}")
        }

    }

    // 내 스터디 목록을 가져온다. (홈화면 내 스터디 접근 시)
    fun getStudyMyData() = viewModelScope.launch {
        try {
            val response = studyRepository.getStudyMyData()

            _studyMyDataList.value = response
            Log.d("테스트 vm2","${_studyMyDataList.value.toString()}")
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error vmGetDeliveryDataByUserIdx : ${e.message}")
        }

    }
}