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
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.TechStackData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.WriteRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class WriteViewModel : ViewModel() {

    // --------------------------------------- 초기화 ---------------------------------------
    //태그
    private val logTag by lazy { WriteViewModel::class.simpleName } // 로그 태그

    // 스터디 Repository
    private val writeRepository by lazy { WriteRepository() }
    private val studyRepository by lazy { StudyRepository() }
    // --------------------------------------- 초기화 ---------------------------------------

    // --------------------------------------- 공통 ---------------------------------------
    // 현재 사용자의 인덱스를 가져오는 함수
    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }
    // --------------------------------------- 공통 ---------------------------------------

    // --------------------------------------- 글작성 ---------------------------------------

    // 글작성 데이터
    private val _writeDataMap = MutableLiveData<MutableMap<String, Any?>?>(mutableMapOf())
    val writeDataMap: LiveData<MutableMap<String, Any?>?> = _writeDataMap

    // 글작성 로딩
    private val _writeStudyDataLoading = MutableLiveData(false)
    val writeStudyDataLoading: LiveData<Boolean> = _writeStudyDataLoading

    // 글작성 에러
    private val _writeStudyDataError = MutableLiveData<Throwable?>(null)
    val writeStudyDataError: LiveData<Throwable?> = _writeStudyDataError

    // studyIdx를 관찰하기 위한 LiveData
    private val _writeStudyIdx = MutableLiveData<Int?>()
    val writeStudyIdx: LiveData<Int?> = _writeStudyIdx

    // 카메라 촬영, 앨범 사진에서 가져은 Uri
    private val _contentUri = MutableLiveData<Uri?>()
    val contentUri: LiveData<Uri?> = _contentUri

    // Uri 업데이트 함수
    fun updateContentUri(uri: Uri?) {
        _contentUri.postValue(uri)
    }

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

    // 스터디 데이터 업로드
    fun writeStudyData(context: Context) {

        viewModelScope.launch(Dispatchers.IO) {
            // 로딩 시작
            _writeStudyDataLoading.postValue(true)

            val userIdx = getCurrentUserIdx()

            // 스터디 기본 정보 추출
            val studyTitle = _writeDataMap.value?.get("studyTitle") as? String ?: ""
            val studyContent = _writeDataMap.value?.get("studyContent") as? String ?: ""
            val studyType = _writeDataMap.value?.get("studyType") as? String ?: ""
            val studyPeriod = _writeDataMap.value?.get("studyPeriod") as? String ?: ""
            val studyOnOffline = _writeDataMap.value?.get("studyOnOffline") as? String ?: ""
            val studyPlace = _writeDataMap.value?.get("studyPlace") as? String ?: ""
            val studyDetailPlace = _writeDataMap.value?.get("studyDetailPlace") as? String ?: ""
            val studyMaxMember = _writeDataMap.value?.get("studyMaxMember") as? Int ?: 2
            val studyTechStackList = _writeDataMap.value?.get("studyTechStackList") as? List<Int> ?: listOf()
            val studyChatLink = _writeDataMap.value?.get("studyChatLink") as? String ?: ""

            // studyPic URI 확인 및 변환
            val studyPicString = _writeDataMap.value?.get("studyPic") as? String
            val studyPicUri = studyPicString?.let { Uri.parse(it) }

            // 이미지가 있을 경우 S3에 업로드
            val studyS3Url = if (studyPicUri != null) {
                try {
                    // 이미지 업로드
                    val uploadResult = writeRepository.uploadImageToS3(context, studyPicUri)
                    uploadResult.getOrThrow()  // 성공하면 이미지 URL 반환
                } catch (e: Exception) {
                    Log.e(logTag, "writeStudyData: 이미지 업로드 실패", e)
                    _writeStudyDataError.postValue(e)
                    return@launch  // 실패 시 함수 종료
                }
            } else {
                listOf(
                    "https://modigm-app-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_1.jpg",
                    "https://modigm-app-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_2.jpg"
                ).random()
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
                studyPic = studyS3Url,  // S3에서 업로드된 이미지 URL 사용
                studyMaxMember = studyMaxMember,
                studyState = true,
                studyChatLink = studyChatLink,
                userIdx = userIdx
            )

            Log.d(logTag, "writeStudyData: 스터디 데이터 변환 완료 - studyData: $studyData")

            // 스터디 데이터 업로드
            val result = writeRepository.uploadStudyData(
                studyData = studyData,
                studyTechStack = studyTechStackList
            )

            result.onSuccess {
                // 로딩 종료
                _writeStudyDataLoading.postValue(false)

                Log.d(logTag, "writeStudyData: 스터디 데이터 업로드 성공 - studyIdx: $it")
                _writeStudyIdx.postValue(it)
            }.onFailure { e ->
                // 로딩 종료
                _writeStudyDataLoading.postValue(false)

                Log.e(logTag, "writeStudyData: 스터디 데이터 업로드 실패", e)
                _writeStudyDataError.postValue(e)

            }
        }
    }

    // --------------------------------------- 글작성 ---------------------------------------

    // --------------------------------------- 탭설정 ---------------------------------------
    private val _selectedTabPosition = MutableLiveData(0)
    val selectedTabPosition: LiveData<Int> = _selectedTabPosition

    private val _progressBarState = MutableLiveData(20)
    val progressBarState: LiveData<Int> = _progressBarState

    // 탭 선택 상태 업데이트
    fun updateSelectedTab(position: Int) {
        Log.d(logTag, "updateSelectedTab: position=$position")
        _selectedTabPosition.value = position
        _progressBarState.value = (position + 1) * 20  // 탭 위치에 따라 프로그래스바 업데이트
    }
    // --------------------------------------- 탭설정 ---------------------------------------

    // -------------------------------------- 바텀 시트 설정 --------------------------------------
    // 기술 스택 데이터 LiveData
    private val _techStackData = MutableLiveData<List<TechStackData>>()
    val techStackData: LiveData<List<TechStackData>> = _techStackData

    // 선택된 기술 스택 목록을 저장할 LiveData
    private val _selectedTechStacks = MutableLiveData<MutableSet<TechStackData>>(mutableSetOf())
    val selectedTechStacks: LiveData<MutableSet<TechStackData>> = _selectedTechStacks

    // 선택된 기술 스택을 업데이트하는 함수
    fun updateSelectedTechStacks(techStacks: Set<TechStackData>) {
        val distinctTechStacks = techStacks.distinctBy { it.techName }.toMutableSet()
        _selectedTechStacks.postValue(distinctTechStacks)
    }

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
    // -------------------------------------- 바텀 시트 설정 --------------------------------------

    private var isDataCleared: Boolean = false // 데이터 초기화 플래그

    // 글작성 데이터 초기화
    fun clearData() {
        _writeDataMap.postValue(mutableMapOf())
        _progressBarState.postValue(20)
        _selectedTabPosition.postValue(0)
        _writeStudyDataError.postValue(null)
        _techStackData.postValue(emptyList())
        _writeStudyIdx.postValue(null)
        _contentUri.postValue(null)
        _writeStudyDataLoading.postValue(false)
        _selectedTechStacks.postValue(mutableSetOf())

        isDataCleared = true // 데이터가 초기화되었음을 표시

        Log.d(logTag, "clearData: 데이터 초기화 완료")
    }
}
