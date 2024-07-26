package kr.co.lion.modigm.ui.study.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyListRepository
import kr.co.lion.modigm.util.ModigmApplication

class StudyViewModel : ViewModel() {

    // --------------------------------- MySQL 적용 ---------------------------------
    // --------------------------------- 초기화 시작 --------------------------------

    private val studyListRepository by lazy {
        StudyListRepository()
    }

    private val prefs by lazy {
        ModigmApplication.prefs
    }

    // --------------------------------- 초기화 끝 --------------------------------
    // --------------------------------- 라이브데이터 시작 --------------------------------
    // 전체 스터디 목록 중 모집중인 스터디 리스트
    private val _allStudyStateTrueDataList = MutableLiveData<List<Triple<SqlStudyData, Int, Boolean>>>()
    val allStudyStateTrueDataList: LiveData<List<Triple<SqlStudyData, Int, Boolean>>> = _allStudyStateTrueDataList

    // 전체 스터디 목록 중 모집중인 스터디 리스트 로딩
    private val _setNullStudyAllLoading = MutableLiveData<Boolean?>(null)
    val setNullStudyAllLoading: LiveData<Boolean?> = _setNullStudyAllLoading

    // 내 스터디 리스트
    private val _myStudyDataList = MutableLiveData<List<Triple<SqlStudyData, Int, Boolean>>>()
    val myStudyDataList: LiveData<List<Triple<SqlStudyData, Int, Boolean>>> = _myStudyDataList

    // 내 스터디 로딩
    private val _studyMyDataLoading = MutableLiveData<Boolean?>(null)
    val studyMyDataLoading: LiveData<Boolean?> = _studyMyDataLoading

    private val _isFavorite = MutableLiveData<Pair<Int, Boolean>>()
    val isFavorite: LiveData<Pair<Int, Boolean>> get() = _isFavorite

    // --------------------------------- 라이브데이터 끝 --------------------------------

    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다. (홈화면 '전체 스터디' 접근 시)
    fun getAllStudyStateTrueDataList() {
        viewModelScope.launch {
            try {
                val currentUserIdx = getCurrentUserIdx()
                val data = studyListRepository.getAllStudyAndMemberCount(currentUserIdx)
                _allStudyStateTrueDataList.value = data
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Error getStudyStateTrueData", e)
            }
        }
    }

    // 전체 스터디 목록 중 내 스터디만 가져온다. (홈화면 '내 스터디' 접근 시)
    fun getMyStudyDataList() {
        viewModelScope.launch {
            try {
                val currentUserIdx = getCurrentUserIdx()
                val data = studyListRepository.getMyStudyList(currentUserIdx)
                _myStudyDataList.value = data
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Error getMyStudyData", e)
            }
        }
    }

    // 좋아요 토글
    fun toggleFavorite(studyIdx: Int) {
        viewModelScope.launch {
            try {
                val currentUserIdx = getCurrentUserIdx()
                val currentState = studyListRepository.toggleFavorite(currentUserIdx, studyIdx)
                _isFavorite.value = Pair(studyIdx, currentState)
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Error toggling favorite", e)
            }
        }
    }

    // 데이터 초기화 메서드
    fun clearData() {
        _allStudyStateTrueDataList.value = emptyList()
        _setNullStudyAllLoading.value = null
        _myStudyDataList.value = emptyList()
        _studyMyDataLoading.value = null
        _filteredStudyList.value = emptyList()
        _filteredMyStudyList.value = emptyList()
        filterData.clear()
    }

    // Dao 코루틴 및 히카리CP 자원 해제하기
    private fun close() {
        viewModelScope.launch {
            studyListRepository.close()
        }
    }
    // 뷰모델에서 Dao 코루틴 및 히카리CP 자원 해제
    override fun onCleared() {
        super.onCleared()
        close()
    }

    // ------------------MySQL 적용 끝-----------------------


    // 필터 데이터
    private val filterData = mutableMapOf<String, String>()

    // ========================로딩 초기화============================
    fun setNullStudyAllLoading() {
        _setNullStudyAllLoading.value = null
    }

    fun setNullStudyMyLoading() {
        _studyMyDataLoading.value = null
    }
    // ===============================================================


    //===========================필터============================

    // 필터링된 스터디 목록 (전체)
    private val _filteredStudyList = MutableLiveData<List<Pair<StudyData, Int>>>()
    val filteredStudyList: LiveData<List<Pair<StudyData, Int>>> get() = _filteredStudyList

    // 필터링된 스터디 목록 (내 스터디)
    private val _filteredMyStudyList = MutableLiveData<List<Pair<StudyData, Int>>>()
    val filteredMyStudyList: LiveData<List<Pair<StudyData, Int>>> get() = _filteredMyStudyList


    // 필터 데이터 업데이트
    fun updateFilterData(newFilterData: Map<String, String>) {
        filterData.putAll(newFilterData)
        Log.d("StudyViewModel", "필터 데이터 업데이트: $filterData")
//        applyFilters()
//        applyMyFilters()
    }

//    // 필터 적용 (전체 스터디)
//    private fun applyFilters() {
//        val studyTypeFilter = filterData["studyType"]?.toIntOrNull()
//        val studyPeriodFilter = filterData["studyPeriod"]?.toIntOrNull()
//        val studyOnOfflineFilter = filterData["studyOnOffline"]?.toIntOrNull()
//        val studyMaxMemberFilter = filterData["studyMaxMember"]?.toIntOrNull()
//        val studyApplyMethodFilter = filterData["studyApplyMethod"]?.toIntOrNull()
//        val studySkillListFilter = filterData["studySkillList"]?.toIntOrNull()
//        val programmingLanguageFilter = filterData["programmingLanguage"]?.toIntOrNull()
//
//        Log.d("StudyViewModel", "필터 적용: studyTypeFilter = $studyTypeFilter, studyPeriodFilter = $studyPeriodFilter, studyOnOfflineFilter = $studyOnOfflineFilter, studyMaxMemberFilter = $studyMaxMemberFilter, studyApplyMethodFilter = $studyApplyMethodFilter, studySkillListFilter = $studySkillListFilter, programmingLanguageFilter = $programmingLanguageFilter")
//
//        val studyStateTrueDataList = _studyStateTrueDataList.value
//        Log.d("StudyViewModel", "전체 스터디 데이터: $studyStateTrueDataList")
//
//        // 타입
//        val typeFilteredList = studyStateTrueDataList?.filter { (study, _) ->
//            studyTypeFilter == null || studyTypeFilter == 0 || study.studyType == studyTypeFilter
//        } ?: emptyList()
//
//        // 기간
//        val periodFilteredList = typeFilteredList.filter { (study, _) ->
//            studyPeriodFilter == null || studyPeriodFilter == 0 || study.studyPeriod == studyPeriodFilter
//        }
//
//        // 온오프라인
//        val onOfflineFilteredList = periodFilteredList.filter { (study, _) ->
//            studyOnOfflineFilter == null || studyOnOfflineFilter == 0 || study.studyOnOffline == studyOnOfflineFilter
//        }
//
//        // 최대 인원수 필터링
//        val maxMemberFilteredList = onOfflineFilteredList.filter { (study, _) ->
//            studyMaxMemberFilter == null || studyMaxMemberFilter == 0 || when (studyMaxMemberFilter) {
//                1 -> study.studyMaxMember in 1..5
//                2 -> study.studyMaxMember in 6..10
//                3 -> study.studyMaxMember >= 11
//                else -> true
//            }
//        }
//
//        // 신청 방식 필터링
//        val applyMethodFilteredList = maxMemberFilteredList.filter { (study, _) ->
//            studyApplyMethodFilter == null || studyApplyMethodFilter == 0 || study.studyApplyMethod == studyApplyMethodFilter
//        }
//
//        // 기술 스택 필터링
//        val skillListFilteredList = applyMethodFilteredList.filter { (study, _) ->
//            studySkillListFilter == null || studySkillListFilter == 0 || study.studySkillList.contains(studySkillListFilter)
//        }
//
//        // 프로그래밍 언어 필터링
//        val programmingLanguageFilteredList = skillListFilteredList.filter { (study, _) ->
//            programmingLanguageFilter == null || programmingLanguageFilter == 0 || study.studySkillList.contains(programmingLanguageFilter)
//        }
//
//        // 필터링된 데이터로 LiveData 업데이트
//        _filteredStudyList.value = programmingLanguageFilteredList
//
//        Log.d("StudyViewModel", "필터링된 결과: ${_filteredStudyList.value?.size} 개, 필터링된 데이터: ${_filteredStudyList.value}")
//    }

//    // 필터 적용 (내 스터디)
//    private fun applyMyFilters() {
//        val studyTypeFilter = filterData["studyType"]?.toIntOrNull()
//        val studyPeriodFilter = filterData["studyPeriod"]?.toIntOrNull()
//        val studyOnOfflineFilter = filterData["studyOnOffline"]?.toIntOrNull()
//        val studyMaxMemberFilter = filterData["studyMaxMember"]?.toIntOrNull()
//        val studyApplyMethodFilter = filterData["studyApplyMethod"]?.toIntOrNull()
//        val studySkillListFilter = filterData["studySkillList"]?.toIntOrNull()
//        val programmingLanguageFilter = filterData["programmingLanguage"]?.toIntOrNull()
//
//        Log.d("StudyViewModel", "필터 적용: studyTypeFilter = $studyTypeFilter, studyPeriodFilter = $studyPeriodFilter, studyOnOfflineFilter = $studyOnOfflineFilter, studyMaxMemberFilter = $studyMaxMemberFilter, studyApplyMethodFilter = $studyApplyMethodFilter, studySkillListFilter = $studySkillListFilter, programmingLanguageFilter = $programmingLanguageFilter")
//
//        val studyMyDataList = _studyMyDataList.value
//        Log.d("StudyViewModel", "내 스터디 데이터: $studyMyDataList")
//
//        // 타입
//        val typeFilteredList = studyMyDataList?.filter { (study, _) ->
//            studyTypeFilter == null || studyTypeFilter == 0 || study.studyType == studyTypeFilter
//        } ?: emptyList()
//
//        // 기간
//        val periodFilteredList = typeFilteredList.filter { (study, _) ->
//            studyPeriodFilter == null || studyPeriodFilter == 0 || study.studyPeriod == studyPeriodFilter
//        }
//
//        // 온오프라인
//        val onOfflineFilteredList = periodFilteredList.filter { (study, _) ->
//            studyOnOfflineFilter == null || studyOnOfflineFilter == 0 || study.studyOnOffline == studyOnOfflineFilter
//        }
//
//        // 최대 인원수 필터링
//        val maxMemberFilteredList = onOfflineFilteredList.filter { (study, _) ->
//            studyMaxMemberFilter == null || studyMaxMemberFilter == 0 || when (studyMaxMemberFilter) {
//                1 -> study.studyMaxMember in 1..5
//                2 -> study.studyMaxMember in 6..10
//                3 -> study.studyMaxMember >= 11
//                else -> true
//            }
//        }
//
//        // 신청 방식 필터링
//        val applyMethodFilteredList = maxMemberFilteredList.filter { (study, _) ->
//            studyApplyMethodFilter == null || studyApplyMethodFilter == 0 || study.studyApplyMethod == studyApplyMethodFilter
//        }
//
//        // 기술 스택 필터링
//        val skillListFilteredList = applyMethodFilteredList.filter { (study, _) ->
//            studySkillListFilter == null || studySkillListFilter == 0 || study.studySkillList.contains(studySkillListFilter)
//        }
//
//        // 프로그래밍 언어 필터링
//        val programmingLanguageFilteredList = skillListFilteredList.filter { (study, _) ->
//            programmingLanguageFilter == null || programmingLanguageFilter == 0 || study.studySkillList.contains(programmingLanguageFilter)
//        }
//
//        // 필터링된 데이터로 LiveData 업데이트
//        _filteredMyStudyList.value = programmingLanguageFilteredList
//
//        Log.d("StudyViewModel", "필터링된 결과: ${_filteredMyStudyList.value?.size} 개, 필터링된 데이터: ${_filteredMyStudyList.value}")
//    }


}
