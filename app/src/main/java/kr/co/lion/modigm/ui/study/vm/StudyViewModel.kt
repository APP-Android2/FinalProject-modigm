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

class StudyViewModel : ViewModel() {

    // --------------------------------- MySQL 적용 ---------------------------------
    // --------------------------------- 초기화 시작 --------------------------------

    private val tag by lazy { StudyViewModel::class.simpleName }

    private val studyRepository by lazy { StudyRepository() }



    // --------------------------------- 초기화 끝 --------------------------------
    // --------------------------------- 라이브데이터 시작 --------------------------------

    // 전체 스터디 목록 중 모집중인 스터디 리스트
    private val _allStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val allStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _allStudyData



    // 내 스터디 리스트
    private val _myStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val myStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _myStudyData

    // 좋아요한 스터디 목록
    private val _favoritedStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val favoritedStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _favoritedStudyData

    // 좋아요 상태
    private val _isFavorite = MutableLiveData<Pair<Int, Boolean>>()
    val isFavorite: LiveData<Pair<Int, Boolean>> = _isFavorite


    private val _allStudyError = MutableLiveData<Throwable?>()
    val allStudyError: LiveData<Throwable?> = _allStudyError

    private val _myStudyError = MutableLiveData<Throwable?>()
    val myStudyError: LiveData<Throwable?> = _myStudyError

    private val _favoriteStudyError = MutableLiveData<Throwable?>()
    val favoriteStudyError: LiveData<Throwable?> = _favoriteStudyError

    private val _isFavoriteError = MutableLiveData<Throwable?>()
    val isFavoriteError: LiveData<Throwable?> = _isFavoriteError

    // --------------------------------- 라이브데이터 끝 --------------------------------

    // 현재 사용자의 인덱스를 가져오는 함수
    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }

    /**
     * 전체 스터디 목록 중 모집중인 스터디 가져오기 (홈화면 '전체 스터디' 접근 시)
     */
    fun getAllStudyData() {
        viewModelScope.launch {
            val result = studyRepository.getAllStudyData(getCurrentUserIdx())
            result.onSuccess {
                _allStudyData.postValue(it)
            }.onFailure {
                Log.e(tag, "Error getAllStudyData", it)
                _allStudyError.postValue(it)
            }
        }
    }

    /**
     * 전체 스터디 목록 중 내 스터디 목록 가져오기 (홈화면 '내 스터디' 접근 시)
     */
    fun getMyStudyData() {
        viewModelScope.launch {
            val result = studyRepository.getMyStudyData(getCurrentUserIdx())
            result.onSuccess {
                _myStudyData.postValue(it)
            }.onFailure { e ->
                Log.e(tag, "Error getMyStudyData", e)
                _myStudyError.postValue(e)
            }
        }
    }

    /**
     * 좋아요한 스터디 목록 가져오기 (찜화면 접근 시)
     */
    fun getFavoriteStudyData() {
        viewModelScope.launch {
            val result = studyRepository.getFavoriteStudyData(getCurrentUserIdx())
            result.onSuccess {
                _favoritedStudyData.postValue(it)
            }.onFailure { e ->
                Log.e(tag, "Error getFavoriteStudyData", e)
                _favoriteStudyError.postValue(e)
            }
        }
    }

    /**
     * 좋아요 상태 변경
     * @param studyIdx 스터디 인덱스
     * @param currentState 현재 좋아요 상태
     */
    fun changeFavoriteState(studyIdx: Int, currentState: Boolean) {
        viewModelScope.launch {
            // 좋아요 상태 변경
            val result = if (currentState) {
                studyRepository.removeFavorite(getCurrentUserIdx(), studyIdx)
            } else {
                studyRepository.addFavorite(getCurrentUserIdx(), studyIdx)
            }

            // 결과에 따른 UI 업데이트
            result.onSuccess {
                _isFavorite.value = Pair(studyIdx, !currentState)
            }.onFailure { e ->
                Log.e(tag, "Error changing favorite state", e)
                _isFavoriteError.postValue(e)
            }
        }
    }

    /**
     * 데이터 초기화 메서드
     */
    fun clearData() {
        _allStudyData.postValue(emptyList())
        _myStudyData.postValue(emptyList())
        _favoritedStudyData.postValue(emptyList())
        _isFavorite.postValue(Pair(-1, false))
        _favoriteStudyError.postValue(null)
        _isFavoriteError.postValue(null)
        _myStudyError.postValue(null)
        _allStudyError.postValue(null)

        filterData.clear()
    }

    // ------------------MySQL 적용 끝-----------------------

    // 필터 데이터
    private val filterData = mutableMapOf<String, String>()

//    // 필터링된 스터디 목록 (전체)
//    private val _filteredStudyList = MutableLiveData<List<Pair<StudyData, Int>>>()
//    val filteredStudyList: LiveData<List<Pair<StudyData, Int>>> get() = _filteredStudyList
//
//    // 필터링된 스터디 목록 (내 스터디)
//    private val _filteredMyStudyList = MutableLiveData<List<Pair<StudyData, Int>>>()
//    val filteredMyStudyList: LiveData<List<Pair<StudyData, Int>>> get() = _filteredMyStudyList

    /**
     * 필터 데이터 업데이트
     * @param newFilterData 새로운 필터 데이터
     */
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
