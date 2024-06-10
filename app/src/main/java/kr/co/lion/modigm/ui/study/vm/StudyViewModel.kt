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

    private val _likedStudies = MutableLiveData<List<StudyData>>()
    val likedStudies: LiveData<List<StudyData>> = _likedStudies



    // ========================로딩 초기화============================
    fun setNullStudyAllLoading(){
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
            _setNullStudyAllLoading.value = true
            Log.d("테스트 vm1","${_studyStateTrueDataList.value.toString()}")
        } catch (e: Exception) {
            Log.e("StudyViewModel", "Error vmGetDeliveryDataByUserIdx : ${e.message}")
        }

    }

    // 내 스터디 목록을 가져온다. (홈화면 내 스터디 접근 시)
    fun getStudyMyData() = viewModelScope.launch {
        try {
            val currentUserUid = ModigmApplication.prefs.getUserData("currentUserData")?.userUid ?: ""
            val response = studyRepository.getStudyMyData(currentUserUid)

            _studyMyDataList.value = response
            _studyMyDataLoading.value = true
            Log.d("테스트 vm2","${_studyMyDataList.value.toString()}")
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
}