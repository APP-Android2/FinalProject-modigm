package kr.co.lion.modigm.ui.write

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomPopupCoverImageBinding
import kr.co.lion.modigm.databinding.FragmentWriteIntroBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName
import java.io.File

class WriteIntroFragment : VBBaseFragment<FragmentWriteIntroBinding>(FragmentWriteIntroBinding::inflate) {

    // 뷰모델
    val viewModel: WriteViewModel by activityViewModels()

    // 태그
    private val logTag by lazy { WriteIntroFragment::class.simpleName }

    // 카메라 실행을 위한 런처
    private val cameraLauncher: ActivityResultLauncher<Intent> by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                viewModel.contentUri.value?.let { uri ->
                    // 이미지 로드
                    processImageFromUri(uri)
                    // 뷰모델에 업데이트
                    viewModel.updateWriteData("studyPic", uri.toString())
                }
            }
        }
    }

    // 앨범 실행을 위한 런처
    private val albumLauncher: ActivityResultLauncher<Intent> by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { selectedUri ->
                    // 뷰모델에 contentUri 업데이트
                    viewModel.updateContentUri(selectedUri)
                    processImageFromUri(selectedUri)
                    viewModel.updateWriteData("studyPic", selectedUri.toString())
                }
            }
        }
    }

    // 확인할 권한 목록
    private val permissionList: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29 이상일 때만 ACCESS_MEDIA_LOCATION 권한 요청
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        } else {
            // 그 이하에서는 ACCESS_MEDIA_LOCATION 제외
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // 이미지를 첨부한 적이 있는지
    private var isAddPicture = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // onCreate에서 런처들 미리 초기화
        cameraLauncher
        albumLauncher
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 권한 요청
        requestPermissions(permissionList, 0)

        // 데이터 복원 함수 호출
        restoreInputData()
        // 초기 뷰 설정
        initView()
        // 뷰모델 관찰
        observeViewModel()

    }

    // 데이터 복원 함수
    private fun restoreInputData() {
        val writeDataMap = viewModel.writeDataMap.value ?: return  // 로컬 변수에 저장

        with(binding) {
            // 이미지 복원
            viewModel.contentUri.value?.let {
                loadImageIntoImageView(it)
                isAddPicture = true
            }

            // 제목 복원
            (writeDataMap["studyTitle"] as? String)?.let {
                textInputWriteIntroTitle.setText(it)
            }

            // 내용 복원
            (writeDataMap["studyContent"] as? String)?.let {
                textInputWriteIntroContent.setText(it.replace("\\n", System.lineSeparator()))
            }

            // 링크 복원
            (writeDataMap["studyChatLink"] as? String)?.let {
                textInputWriteIntroLink.setText(it)
            }
        }
    }

    private fun initView() {
        with(binding) {

            // 이미지 추가 버튼
            imageButtonWriteIntroCoverImage.apply {
                setOnClickListener {
                    showPopupWindow(it)
                }
            }

            // 스터디 제목 입력
            textInputWriteIntroTitle.apply {
                // 텍스트 입력 변경 사항을 관찰
                addTextChangedListener(inputWatcher)
            }

            // 스터디 내용 입력
            textInputWriteIntroContent.apply {
                // 텍스트 변경 시 리스너 등록
                addTextChangedListener(inputWatcher)

                // 터치 이벤트 처리
                setOnTouchListener { view, event ->

                    // 포커스가 있을 때 부모의 스크롤 이벤트 가로채기 방지
                    if (view.hasFocus()) {
                        view.parent.requestDisallowInterceptTouchEvent(true)

                        // 터치 끝나면 부모에게 이벤트 권한 반환
                        if (event.action == MotionEvent.ACTION_UP) {
                            view.parent.requestDisallowInterceptTouchEvent(false)
                            view.performClick()  // 접근성을 위한 클릭 처리
                        }
                    }

                    // false 반환하여 기본 동작 유지
                    false
                }
            }

            // 작성 예시 버튼
            textViewWriteIntroWriteExample.apply {
                setOnClickListener {
                    val dialog = CustomIntroDialog(requireContext())
                    dialog.show()
                }
            }

            // 오픈채팅 링크 입력
            textInputWriteIntroLink.apply {
                // 텍스트 입력 변경 사항을 관찰
                addTextChangedListener(inputWatcher)

                // 클릭 시 (추후 클릭으로 웹뷰를 통한 링크 받아오기 구현 예정)
                setOnClickListener {

                }
            }

            // 처음 접근 시 텍스트필드에 데이터가 있을 경우 버튼 활성화
            if (textInputWriteIntroTitle.text.toString() != ""
                && textInputWriteIntroContent.text.toString() != ""
                && textInputWriteIntroLink.text.toString() != ""
            ) {
                buttonWriteIntroNext.isEnabled = true
                updateButtonColor()
            }

            // 작성 버튼
            buttonWriteIntroNext.apply {
                // 클릭 시
                setOnClickListener {
                    // 클릭 시 입력 유효성 검사
                    if(!checkAllInput()) {
                        // 유효하지 않은 경우 리턴
                        return@setOnClickListener
                    }
                    // 스터디 커버 이미지 업데이트
                    viewModel.contentUri.value?.let { uri ->
                        viewModel.updateWriteData(
                            key = "studyPic",
                            value = uri.toString()
                        )
                    }
                    // 스터디 제목 업데이트
                    viewModel.updateWriteData(
                        key = "studyTitle",
                        value = textInputWriteIntroTitle.text.toString())
                    // 스터디 내용 업데이트
                    viewModel.updateWriteData(
                        key = "studyContent",
                        value = System.lineSeparator().let { textInputWriteIntroContent.text.toString().replace(it, "\\n") }
                    )
                    // 오픈채팅 링크 업데이트
                    viewModel.updateWriteData(
                        key = "studyChatLink",
                        value = textInputWriteIntroLink.text.toString()
                    )

                    // 스터디 데이터 업로드
                    viewModel.writeStudyData(requireContext())
                }
            }
        }
    }

    private fun observeViewModel() {
        // 글작성 완료 후 화면전환
        viewModel.writeStudyIdx.observe(viewLifecycleOwner){ studyIdx ->
            if (studyIdx != null) {
                // 글 상세 프래그먼트로 이동
                navigateToDetailFragment(studyIdx)
            }
        }

        // 글작성 에러
        viewModel.writeStudyDataError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showErrorDialog(error)
            }
        }

        // 로딩 상태에 따른 UI 업데이트
        viewModel.writeStudyDataLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                if (isLoading) {
                    overlayView.visibility = View.VISIBLE
                    writeIntroProgressBar.visibility = View.VISIBLE
                    buttonWriteIntroNext.isEnabled = false
                } else {
                    overlayView.visibility = View.GONE
                    writeIntroProgressBar.visibility = View.GONE
                    buttonWriteIntroNext.isEnabled = true
                }
            }
        }

    }

    // 글작성 소개 유효성 검사
    private fun checkAllInput(): Boolean {
        return checkStudyTitle() && checkStudyContent() && checkStudyLink()
    }

    // 스터디 제목 유효성 검사
    private fun checkStudyTitle(): Boolean {
        with(binding) {
            fun showError(message: String) {
                textInputLayoutWriteIntroTitle.error = message
                textInputWriteIntroTitle.requestFocus()
            }
            return when {
                textInputWriteIntroTitle.text.toString().length < 8 -> {
                    showError("제목은 8자 이상 입력해주세요")
                    false
                }
                else -> {
                    textInputLayoutWriteIntroTitle.error = null
                    true
                }
            }
        }
    }

    // 스터디 내용 유효성 검사
    private fun checkStudyContent(): Boolean {
        with(binding) {
            fun showError(message: String) {
                textInputLayoutWriteIntroContent.error = message
                textInputWriteIntroContent.requestFocus()
            }
            return when {
                textInputWriteIntroContent.text.toString().length < 10 -> {
                    showError("내용은 10자 이상 입력해주세요.")
                    false
                }
                else -> {
                    textInputLayoutWriteIntroContent.error = null
                    true
                }
            }
        }
    }

    private fun checkStudyLink(): Boolean {
        with(binding) {
            fun showError(message: String) {
                textInputLayoutWriteIntroLink.error = message
                textInputWriteIntroLink.requestFocus()
            }

            // 카카오톡 오픈채팅방 링크 정규식
            val kakaoOpenChatRegex = Regex("^https://open.kakao.com/.*")

            return when {
                textInputWriteIntroLink.text.toString().isEmpty() -> {
                    showError("링크를 입력해주세요.")
                    false
                }
                !kakaoOpenChatRegex.matches(textInputWriteIntroLink.text.toString()) -> {
                    showError("올바른 형식으로 링크를 입력해주세요.")
                    false
                }
                else -> {
                    textInputLayoutWriteIntroLink.error = null
                    true
                }
            }
        }
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        // 입력 내용 변경 전
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            with(binding) {
                buttonWriteIntroNext.apply {
                    // 입력 데이터가 모두 존재할 경우
                    isEnabled = if (textInputWriteIntroTitle.text.toString() != ""
                        && textInputWriteIntroContent.text.toString() != ""
                        && textInputWriteIntroLink.text.toString() != ""
                    ) {
                        // 버튼 활성화
                        updateButtonColor()
                        true
                    } else {
                        // 버튼 비활성화
                        updateButtonColor()
                        false
                    }
                }

                // 스터디 제목 길이가 8자 이상일 경우
                if (textInputWriteIntroTitle.text.toString().length >= 8) {
                    // 스터디 제목 에러 메시지 초기화
                    textInputLayoutWriteIntroTitle.error = null
                }

                // 스터디 내용 길이가 10자 이상일 경우
                if (textInputWriteIntroContent.text.toString().length >= 10) {
                    // 스터디 내용 에러 메시지 초기화
                    textInputLayoutWriteIntroContent.error = null
                }

                // 스터디 링크가 존재할 경우
                if (textInputWriteIntroLink.text.toString().isNotEmpty()) {
                    // 스터디 링크 에러 메시지 초기화
                    textInputLayoutWriteIntroLink.error = null
                }
            }
        }

        // 입력 내용 변경 시
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding) {

                // 스터디 제목 입력 시 ViewModel에 저장
                viewModel.updateWriteData("studyTitle", textInputWriteIntroTitle.text.toString())

                // 스터디 내용 입력 시 ViewModel에 저장
                viewModel.updateWriteData("studyContent", System.lineSeparator().let {
                    textInputWriteIntroContent.text.toString().replace(it, "\\n")
                })

                // 스터디 링크 입력 시 ViewModel에 저장
                viewModel.updateWriteData("studyChatLink", textInputWriteIntroLink.text.toString())

                // 입력 데이터가 모두 존재할 경우
                if (textInputWriteIntroTitle.text.toString() != ""
                    && textInputWriteIntroContent.text.toString() != ""
                    && textInputWriteIntroLink.text.toString() != ""
                ) {
                    // 버튼 활성화
                    updateButtonColor()
                    buttonWriteIntroNext.isEnabled = true

                } else {
                    // 버튼 비활성화
                    updateButtonColor()
                    buttonWriteIntroNext.isEnabled = false
                }

                // 스터디 제목 길이가 8자 이상일 경우
                if (textInputWriteIntroTitle.text.toString().length >= 8) {
                    // 스터디 제목 에러 메시지 초기화
                    textInputLayoutWriteIntroTitle.error = null
                }

                // 스터디 내용 길이가 10자 이상일 경우
                if (textInputWriteIntroContent.text.toString().length >= 10) {
                    // 스터디 내용 에러 메시지 초기화
                    textInputLayoutWriteIntroContent.error = null
                }

                // 스터디 링크가 존재할 경우
                if (textInputWriteIntroLink.text.toString().isNotEmpty()) {
                    // 스터디 링크 에러 메시지 초기화
                    textInputLayoutWriteIntroLink.error = null
                }
            }
        }

        // 입력 내용 변경 후
        override fun afterTextChanged(p0: Editable?) {
            with(binding) {
                // 스터디 제목 길이가 8자 이상일 경우
                if (textInputWriteIntroTitle.text.toString().length >= 8) {
                    // 스터디 제목 에러 메시지 초기화
                    textInputLayoutWriteIntroTitle.error = null
                }

                // 스터디 내용 길이가 10자 이상일 경우
                if (textInputWriteIntroContent.text.toString().length >= 10) {
                    // 스터디 내용 에러 메시지 초기화
                    textInputLayoutWriteIntroContent.error = null
                }

                // 스터디 링크가 존재할 경우
                if (textInputWriteIntroLink.text.toString().isNotEmpty()) {
                    // 스터디 링크 에러 메시지 초기화
                    textInputLayoutWriteIntroLink.error = null
                }
            }
        }
    }

    // 이미지 로드 및 ImageView에 표시하는 함수
    private fun loadImageIntoImageView(uri: Uri?) {
        uri?.let{
            val processedBitmap = loadAndProcessBitmap(uri)
            processedBitmap?.let {
                with(binding) {
                    cardViewWriteIntroCoverImageSelect.visibility = View.VISIBLE
                    imageViewWriteIntroCoverImageSelect.setImageBitmap(it)
                }
            }
        }
    }

    // 이미지 로드, 회전, 크기 조정 및 View에 반영하는 함수
    private fun processImageFromUri(uri: Uri?) {
        uri?.let {
            val processedBitmap = loadAndProcessBitmap(it)
            processedBitmap?.let {
                with(binding) {
                    cardViewWriteIntroCoverImageSelect.visibility = View.VISIBLE
                    imageViewWriteIntroCoverImageSelect.setImageBitmap(it)
                    isAddPicture = true
                }
            }
        }
    }

    // 이미지 로드, 회전, 크기 조정 작업을 처리하는 공통 함수
    private fun loadAndProcessBitmap(uri: Uri?): Bitmap? {
        return try {
            uri?.let {
                // 이미지 로드
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }

                // Exif 정보를 바탕으로 회전 각도 계산
                val degree = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requireContext().contentResolver.openInputStream(uri)?.let(::ExifInterface)
                } else {
                    uri.path?.let(::ExifInterface)
                }?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED).let { orientation ->
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }

                // 이미지 회전
                val rotatedBitmap = bitmap?.let {
                    Matrix().apply { postRotate(degree.toFloat()) }
                        .run { Bitmap.createBitmap(it, 0, 0, it.width, it.height, this, false) }
                }
                // 이미지 크기 조정
                rotatedBitmap?.run {
                    val ratio = 1024.toDouble() / width.toDouble()
                    val targetHeight = (height * ratio).toInt()
                    Bitmap.createScaledBitmap(this, 1024, targetHeight, false)
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error loading and processing image: ${e.message}")
            null
        }
    }

    // 이미지 선택 팝업창 표시
    private fun showPopupWindow(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupBinding = CustomPopupCoverImageBinding.inflate(layoutInflater)

        // 팝업 창 생성 및 설정
        val popupWindow = PopupWindow(popupBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            isOutsideTouchable = true
            width = 500
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            showAsDropDown(anchorView, -50, 0)
        }

        with(popupBinding) {
            // 카메라 선택 버튼
            textViewCameraLauncher.apply {
                setOnClickListener {
                    // 임시 파일 생성
                    val file = File(requireContext().getExternalFilesDir(null), "tempImage.jpg")
                    val uri = FileProvider.getUriForFile(requireContext(), "kr.co.lion.modigm.file_provider", file)

                    // contentUri를 뷰모델로 업데이트
                    viewModel.updateContentUri(uri)

                    // 카메라 실행
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cameraIntent ->
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        cameraLauncher.launch(cameraIntent)
                    }
                    popupWindow.dismiss()
                }
            }

            // 앨범 선택 버튼
            textViewAlbumLauncher.apply {
                // 클릭 시
                setOnClickListener {
                    val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                    }
                    albumLauncher.launch(albumIntent)
                    popupWindow.dismiss()
                }
            }
        }
    }

    // 글 상세 프래그먼트로 이동
    private fun navigateToDetailFragment(studyIdx: Int) {
        // 데이터 초기화
        viewModel.clearData()

        val detailFragment = DetailFragment().apply {
            arguments = Bundle().apply {
                putInt("studyIdx", studyIdx)
            }
        }
        requireActivity().supportFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str, 0)
        requireActivity().supportFragmentManager.commit {
            replace(R.id.containerMain, detailFragment)
            addToBackStack(FragmentName.DETAIL.str)
        }
    }

    // 오류 다이얼로그 표시
    private fun showErrorDialog(error: Throwable) {
        val dialog = CustomLoginErrorDialog(requireContext())
        with(dialog){
            setTitle("오류")
            setMessage(error.message.toString())
            setPositiveButton("확인") {
                dismiss()
            }
            show()
        }
    }

    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor() {
        with(binding) {
            val colorResId = if (textInputWriteIntroTitle.text.toString() != ""
                && textInputWriteIntroContent.text.toString() != ""
                && textInputWriteIntroLink.text.toString() != ""
            ) {
                R.color.pointColor
            } else {
                R.color.buttonGray
            }
            with(buttonWriteIntroNext) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }
}
