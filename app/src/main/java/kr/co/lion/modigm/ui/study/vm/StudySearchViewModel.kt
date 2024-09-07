package kr.co.lion.modigm.ui.study.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class StudySearchViewModel : ViewModel() {
    // --------------------------------- MySQL 적용 ---------------------------------
    // --------------------------------- 초기화 시작 --------------------------------

    // 태그
    private val logTag by lazy { StudySearchViewModel::class.simpleName }

    // 스터디 레포지토리
    private val studyRepository by lazy { StudyRepository() }



    // --------------------------------- 초기화 끝 --------------------------------
    // --------------------------------- 라이브데이터 시작 --------------------------------

    // 전체 스터디 목록 중 모집중인 스터디 리스트
    private val _searchStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val searchStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _searchStudyData

    // 로딩 상태를 나타내는 LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 스터디 검색 오류
    private val _searchStudyError = MutableLiveData<Throwable?>()
    val searchStudyError: LiveData<Throwable?> = _searchStudyError

    // --------------------------------- 라이브데이터 끝 --------------------------------

    // 현재 사용자의 인덱스를 가져오는 함수
    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }

    /**
     * 검색 스터디 목록 가져오기
     */
    fun getSearchStudyData() {
        viewModelScope.launch {
            _isLoading.postValue(true) // 로딩 시작
            val result = studyRepository.getAllStudyData()
            result.onSuccess {
                _searchStudyData.postValue(it)
            }.onFailure {
                Log.e(logTag, "Error getAllStudyData", it)
                _searchStudyError.postValue(it)
            }
            _isLoading.postValue(false) // 로딩 종료
        }
    }

    /**
     * 데이터 초기화 메서드
     */
    fun clearData() {
        _searchStudyData.postValue(emptyList())
        _searchStudyError.postValue(null)
        _isLoading.postValue(false)
    }

    // ------------------MySQL 적용 끝-----------------------
}