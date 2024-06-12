package kr.co.lion.modigm.ui.study.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.util.ModigmApplication

class StudyViewModel : ViewModel() {

    // 스터디 Repository
    private val studyRepository = StudyRepository()

    // 전체 스터디 목록 중 모집중인 스터디 리스트
    private val _studyStateTrueDataList = MutableLiveData<List<Pair<StudyData, Int>>>()
    val studyStateTrueDataList: LiveData<List<Pair<StudyData, Int>>> = _studyStateTrueDataList

    // 전체 스터디 목록 중 모집중인 스터디 리스트 로딩
    private val _setNullStudyAllLoading = MutableLiveData<Boolean?>(null)
    val setNullStudyAllLoading: LiveData<Boolean?> = _setNullStudyAllLoading

    // 내 스터디 리스트
    private val _studyMyDataList = MutableLiveData<List<Pair<StudyData, Int>>>()
    val studyMyDataList: LiveData<List<Pair<StudyData, Int>>> = _studyMyDataList

    // 내 스터디 로딩
    private val _studyMyDataLoading = MutableLiveData<Boolean?>(null)
    val studyMyDataLoading: LiveData<Boolean?> = _studyMyDataLoading



    private val _isLiked = MutableLiveData<Boolean>(false)
    val isLiked: LiveData<Boolean> get() = _isLiked

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

    // 뷰모델 인스턴스가 생성될 때마다 가동
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
            Log.d("StudyViewModel", "전체 스터디 데이터 로드: ${response.size} 개")
            applyFilters() // 데이터 로드 후 필터 적용
        } catch (e: Exception) {
            Log.e("StudyViewModel", "Error getStudyStateTrueData: ${e.message}")
        }
    }

    // 내 스터디 목록을 가져온다. (홈화면 내 스터디 접근 시)
    fun getStudyMyData() = viewModelScope.launch {
        try {
            val currentUserUid = ModigmApplication.prefs.getUserData("currentUserData")?.userUid ?: ""
            val response = studyRepository.getStudyMyData(currentUserUid)
            _studyMyDataList.value = response
            Log.d("StudyViewModel", "내 스터디 데이터 로드: ${response.size} 개")
            applyMyFilters() // 데이터 로드 후 필터 적용
        } catch (e: Exception) {
            Log.e("StudyViewModel", "Error vmGetDeliveryDataByUserIdx : ${e.message}")
        }
    }

    fun toggleLike(uid: String, studyIdx: Int) {
        viewModelScope.launch {
            val studyCollection = FirebaseFirestore.getInstance().collection("Study")
            val query = studyCollection.whereEqualTo("studyIdx", studyIdx)
            val querySnapshot = query.get().await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val currentlyLiked = document.getBoolean("studyLikeState") ?: false
                val newLikeState = !currentlyLiked

                document.reference.update("studyLikeState", newLikeState).await()

                if (newLikeState) {
                    studyRepository.addLike(uid, studyIdx)
                } else {
                    studyRepository.removeLike(uid, studyIdx)
                }

                _isLiked.postValue(newLikeState)
            } else {
                studyRepository.removeLike(uid, studyIdx)
                Log.e("StudyViewModel", "No matching document found for studyIdx: $studyIdx")
            }
        }
    }

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
        applyFilters()
        applyMyFilters()
    }

    // 필터 적용 (전체 스터디)
    private fun applyFilters() {
        val studyTypeFilter = filterData["studyType"]?.toIntOrNull()
        val studyPeriodFilter = filterData["studyPeriod"]?.toIntOrNull()
        val studyOnOfflineFilter = filterData["studyOnOffline"]?.toIntOrNull()
        val studyMaxMemberFilter = filterData["studyMaxMember"]?.toIntOrNull()
        val studyApplyMethodFilter = filterData["studyApplyMethod"]?.toIntOrNull()
        val studySkillListFilter = filterData["studySkillList"]?.toIntOrNull()
        val programmingLanguageFilter = filterData["programmingLanguage"]?.toIntOrNull()

        Log.d("StudyViewModel", "필터 적용: studyTypeFilter = $studyTypeFilter, studyPeriodFilter = $studyPeriodFilter, studyOnOfflineFilter = $studyOnOfflineFilter, studyMaxMemberFilter = $studyMaxMemberFilter, studyApplyMethodFilter = $studyApplyMethodFilter, studySkillListFilter = $studySkillListFilter, programmingLanguageFilter = $programmingLanguageFilter")

        val studyStateTrueDataList = _studyStateTrueDataList.value
        Log.d("StudyViewModel", "전체 스터디 데이터: $studyStateTrueDataList")

        // 타입
        val typeFilteredList = studyStateTrueDataList?.filter { (study, _) ->
            studyTypeFilter == null || studyTypeFilter == 0 || study.studyType == studyTypeFilter
        } ?: emptyList()

        // 기간
        val periodFilteredList = typeFilteredList.filter { (study, _) ->
            studyPeriodFilter == null || studyPeriodFilter == 0 || study.studyPeriod == studyPeriodFilter
        }

        // 온오프라인
        val onOfflineFilteredList = periodFilteredList.filter { (study, _) ->
            studyOnOfflineFilter == null || studyOnOfflineFilter == 0 || study.studyOnOffline == studyOnOfflineFilter
        }

        // 최대 인원수 필터링
        val maxMemberFilteredList = onOfflineFilteredList.filter { (study, _) ->
            studyMaxMemberFilter == null || studyMaxMemberFilter == 0 || when (studyMaxMemberFilter) {
                1 -> study.studyMaxMember in 1..5
                2 -> study.studyMaxMember in 6..10
                3 -> study.studyMaxMember >= 11
                else -> true
            }
        }

        // 신청 방식 필터링
        val applyMethodFilteredList = maxMemberFilteredList.filter { (study, _) ->
            studyApplyMethodFilter == null || studyApplyMethodFilter == 0 || study.studyApplyMethod == studyApplyMethodFilter
        }

        // 기술 스택 필터링
        val skillListFilteredList = applyMethodFilteredList.filter { (study, _) ->
            studySkillListFilter == null || studySkillListFilter == 0 || study.studySkillList.contains(studySkillListFilter)
        }

        // 프로그래밍 언어 필터링
        val programmingLanguageFilteredList = skillListFilteredList.filter { (study, _) ->
            programmingLanguageFilter == null || programmingLanguageFilter == 0 || study.studySkillList.contains(programmingLanguageFilter)
        }

        // 필터링된 데이터로 LiveData 업데이트
        _filteredStudyList.value = programmingLanguageFilteredList

        Log.d("StudyViewModel", "필터링된 결과: ${_filteredStudyList.value?.size} 개, 필터링된 데이터: ${_filteredStudyList.value}")
    }

    // 필터 적용 (내 스터디)
    private fun applyMyFilters() {
        val studyTypeFilter = filterData["studyType"]?.toIntOrNull()
        val studyPeriodFilter = filterData["studyPeriod"]?.toIntOrNull()
        val studyOnOfflineFilter = filterData["studyOnOffline"]?.toIntOrNull()
        val studyMaxMemberFilter = filterData["studyMaxMember"]?.toIntOrNull()
        val studyApplyMethodFilter = filterData["studyApplyMethod"]?.toIntOrNull()
        val studySkillListFilter = filterData["studySkillList"]?.toIntOrNull()
        val programmingLanguageFilter = filterData["programmingLanguage"]?.toIntOrNull()

        Log.d("StudyViewModel", "필터 적용: studyTypeFilter = $studyTypeFilter, studyPeriodFilter = $studyPeriodFilter, studyOnOfflineFilter = $studyOnOfflineFilter, studyMaxMemberFilter = $studyMaxMemberFilter, studyApplyMethodFilter = $studyApplyMethodFilter, studySkillListFilter = $studySkillListFilter, programmingLanguageFilter = $programmingLanguageFilter")

        val studyMyDataList = _studyMyDataList.value
        Log.d("StudyViewModel", "내 스터디 데이터: $studyMyDataList")

        // 타입
        val typeFilteredList = studyMyDataList?.filter { (study, _) ->
            studyTypeFilter == null || studyTypeFilter == 0 || study.studyType == studyTypeFilter
        } ?: emptyList()

        // 기간
        val periodFilteredList = typeFilteredList.filter { (study, _) ->
            studyPeriodFilter == null || studyPeriodFilter == 0 || study.studyPeriod == studyPeriodFilter
        }

        // 온오프라인
        val onOfflineFilteredList = periodFilteredList.filter { (study, _) ->
            studyOnOfflineFilter == null || studyOnOfflineFilter == 0 || study.studyOnOffline == studyOnOfflineFilter
        }

        // 최대 인원수 필터링
        val maxMemberFilteredList = onOfflineFilteredList.filter { (study, _) ->
            studyMaxMemberFilter == null || studyMaxMemberFilter == 0 || when (studyMaxMemberFilter) {
                1 -> study.studyMaxMember in 1..5
                2 -> study.studyMaxMember in 6..10
                3 -> study.studyMaxMember >= 11
                else -> true
            }
        }

        // 신청 방식 필터링
        val applyMethodFilteredList = maxMemberFilteredList.filter { (study, _) ->
            studyApplyMethodFilter == null || studyApplyMethodFilter == 0 || study.studyApplyMethod == studyApplyMethodFilter
        }

        // 기술 스택 필터링
        val skillListFilteredList = applyMethodFilteredList.filter { (study, _) ->
            studySkillListFilter == null || studySkillListFilter == 0 || study.studySkillList.contains(studySkillListFilter)
        }

        // 프로그래밍 언어 필터링
        val programmingLanguageFilteredList = skillListFilteredList.filter { (study, _) ->
            programmingLanguageFilter == null || programmingLanguageFilter == 0 || study.studySkillList.contains(programmingLanguageFilter)
        }

        // 필터링된 데이터로 LiveData 업데이트
        _filteredMyStudyList.value = programmingLanguageFilteredList

        Log.d("StudyViewModel", "필터링된 결과: ${_filteredMyStudyList.value?.size} 개, 필터링된 데이터: ${_filteredMyStudyList.value}")
    }
}
