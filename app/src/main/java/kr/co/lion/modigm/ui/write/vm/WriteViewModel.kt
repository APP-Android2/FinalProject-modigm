package kr.co.lion.modigm.ui.write.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.TechStackData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.WriteRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class WriteViewModel : ViewModel() {

    private val logTag = "WriteViewModel"  // 로그 태그

    // 스터디 Repository
    private val writeRepository by lazy { WriteRepository() }
    private val studyRepository by lazy { StudyRepository() }

    // 글작성 데이터
    private val _writeDataMap = MutableLiveData<MutableMap<String, Any?>?>(mutableMapOf())
    val writeDataMap: LiveData<MutableMap<String, Any?>?> = _writeDataMap

    // 글작성 데이터를 업데이트하는 함수
    fun updateWriteData(key: String, value: Any?) {
        Log.d(logTag, "updateWriteData: key=$key, value=$value")
        val currentMap = _writeDataMap.value ?: mutableMapOf()
        currentMap[key] = value
        _writeDataMap.postValue(currentMap)  // 변경된 값을 다시 할당하여 UI에 반영
    }

    // 현재 글작성 중인 데이터를 특정 키로 가져오는 함수
    fun getUpdateData(key: String): Any? {
        val data = _writeDataMap.value?.get(key)
        Log.d(logTag, "getUpdateData: key=$key, value=$data")
        return data
    }

    // ---------------------------------------탭 설정---------------------------------------
    private val _selectedTabPosition = MutableLiveData(0)
    val selectedTabPosition: LiveData<Int> get() = _selectedTabPosition

    private val _progressBarState = MutableLiveData(20)
    val progressBarState: LiveData<Int> get() = _progressBarState

    // 탭 선택 상태 업데이트
    fun updateSelectedTab(position: Int) {
        Log.d(logTag, "updateSelectedTab: position=$position")
        _selectedTabPosition.value = position
        _progressBarState.value = (position + 1) * 20  // 탭 위치에 따라 프로그래스바 업데이트
    }

    // ---------------------------------------탭 설정 끝---------------------------------------

    // 기술 스택 데이터 LiveData
    private val _techStackData = MutableLiveData<List<TechStackData>>()
    val techStackData: LiveData<List<TechStackData>> = _techStackData

    /**
     * 기술 스택 데이터를 가져오는 함수
     */
    fun getTechStackData() {
        Log.d(logTag, "getTechStackData: 기술 스택 데이터 가져오기 시작")
        viewModelScope.launch {
            val result = studyRepository.getTechStackData()
            result.onSuccess {
                Log.d(logTag, "getTechStackData: 성공적으로 데이터를 가져왔습니다 - $it")
                _techStackData.postValue(it)
            }.onFailure { e ->
                Log.e(logTag, "getTechStackData: 기술 스택 데이터 조회 실패", e)
            }
        }
    }

    // 스터디 데이터 업로드 에러
    private val _writeStudyDataError = MutableLiveData<Throwable?>()
    val writeStudyDataError: LiveData<Throwable?> = _writeStudyDataError

    // 스터디 데이터 업로드
    suspend fun writeStudyData(): Int? {
        val userIdx = prefs.getInt("currentUserIdx", 0)

        // _writeDataMap에 저장된 데이터들을 추출하여 StudyData 객체로 변환
        val studyTitle = _writeDataMap.value?.get("studyTitle") as? String
        val studyContent = _writeDataMap.value?.get("studyContent") as? String
        val studyType = _writeDataMap.value?.get("studyType") as? String
        val studyPeriod = _writeDataMap.value?.get("studyPeriod") as? String
        val studyOnOffline = _writeDataMap.value?.get("studyOnOffline") as? String
        val studyPlace = _writeDataMap.value?.get("studyPlace") as? String
        val studyDetailPlace = _writeDataMap.value?.get("studyDetailPlace") as? String
        val studyMaxMember = _writeDataMap.value?.get("studyMaxMember") as? Int
        val studyPic = _writeDataMap.value?.get("studyPic") as? String
        val studyTechStackList =
            _writeDataMap.value?.get("studyTechStackList") as? List<Int>
        // null 값이 존재하는지 검사하는 부분
        if (studyTitle == null || studyContent == null || studyType == null || studyPeriod == null ||
            studyOnOffline == null || studyPlace == null || studyDetailPlace == null ||
            studyMaxMember == null || studyPic == null || studyTechStackList == null) {

            val errorMessage = "필수 입력값 중 누락된 데이터가 있습니다."
            Log.e(logTag, "writeStudyData: $errorMessage")
            _writeStudyDataError.postValue(Throwable(errorMessage))  // 에러 전달
            return null
        }
        // StudyData 객체 생성
        val studyData = StudyData(
            studyTitle = studyTitle,
            studyContent = studyContent,
            studyType = studyType,
            studyPeriod = studyPeriod,
            studyOnOffline = studyOnOffline,
            studyPlace = studyPlace,
            studyDetailPlace = studyDetailPlace,
            studyApplyMethod = "신청제",
            studyCanApply = "모집중",
            studyPic = studyPic,
            studyMaxMember = studyMaxMember,
            studyState = true,
            userIdx = userIdx
        )
        Log.d(logTag, "writeStudyData: 스터디 데이터 변환 완료 - studyData: $studyData")

        // runCatching으로 업로드 처리
        val result =
            writeRepository.uploadStudyData(
                userIdx = userIdx,
                study = studyData,
                studyTechStack = studyTechStackList,
                studyPicUrl = studyPic
            )
        Log.d(logTag, "writeStudyData: 스터디 데이터 업로드 시작 - result: $result")

        return result.getOrNull()
    }

    // 이미지 업로드
    suspend fun uploadImageToS3(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val result = writeRepository.uploadImageToS3(context, uri)
            result.getOrThrow() // 성공 시 String 값 반환
        }
    }

    // 글작성 데이터 초기화
    fun clearData() {
        _writeDataMap.postValue(mutableMapOf())
        _progressBarState.postValue(20)
        _selectedTabPosition.postValue(0)
        _writeStudyDataError.postValue(null)
        _techStackData.postValue(emptyList())
        Log.d(logTag, "clearData: 데이터 초기화 완료")
    }
}
