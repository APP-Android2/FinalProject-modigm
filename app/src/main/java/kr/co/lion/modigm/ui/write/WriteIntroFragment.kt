package kr.co.lion.modigm.ui.write

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteIntroBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.CustomIntroDialog
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake
import java.io.File
import kotlin.random.Random

class WriteIntroFragment : VBBaseFragment<FragmentWriteIntroBinding>(FragmentWriteIntroBinding::inflate) {

    // 뷰모델
    val viewModel: WriteViewModel by activityViewModels()

    // 태그
    private val logTag by lazy { WriteIntroFragment::class.simpleName }

    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    private lateinit var contentUri: Uri

    // 카메라 실행을 위한 런처
    private val cameraLauncher: ActivityResultLauncher<Intent> by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                contentUri.let { uri ->
                    processImageFromUri(uri)
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
                    contentUri = selectedUri
                    processImageFromUri(selectedUri)
                    viewModel.updateWriteData("studyPic", selectedUri.toString())
                }
            }
        }
    }

    // 확인할 권한 목록
    private val permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_MEDIA_LOCATION
    )

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
        with(binding) {
            // 제목 복원
            viewModel.getUpdateData("studyTitle")?.let {
                textInputWriteIntroTitle.setText(it as String)
            }

            // 내용 복원
            viewModel.getUpdateData("studyContent")?.let {
                Log.d(logTag, "스터디 내용 복원됨: $it")
                textInputWriteIntroContent.setText((it as String).replace("\\n", System.lineSeparator()))
            } ?: Log.d(logTag, "스터디 내용이 없습니다.")

            // 이미지 복원
            viewModel.getUpdateData("studyPic")?.let {
                contentUri = Uri.parse(it as String)
                loadImageIntoImageView(contentUri)
                isAddPicture = true
            }
        }
    }

    private fun initView() {
        with(binding) {

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


            // 이미지 추가 버튼
            setupImageButton()

            // 작성 버튼
            buttonWriteIntroNext.apply {
                // 버튼 비활성화
                isEnabled = false
                // 클릭 시
                setOnClickListener {
                    // 클릭 시 입력 유효성 검사
                    if(!checkAllInput()) {
                        // 유효하지 않은 경우 리턴
                        return@setOnClickListener
                    }
                    // 스터디 제목 업데이트
                    viewModel.updateWriteData("studyTitle", textInputWriteIntroTitle.text.toString())
                    // 스터디 내용 업데이트
                    viewModel.updateWriteData("studyContent", System.lineSeparator().let {
                        textInputWriteIntroContent.text.toString().replace(it, "\\n")
                    })
                    Log.d(logTag, "스터디 내용 저장됨: ${textInputWriteIntroContent.text.toString()}")

                    // 이미지 업로드 및 데이터 저장
                    uploadImageAndSaveData { studyIdx ->
                        // 상세 프래그먼트로 이동
                        navigateToDetailFragment(studyIdx)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        // 이미지 URI 데이터 복원
        (viewModel.getUpdateData("studyPic") as? String)?.let { uriString ->
            contentUri = Uri.parse(uriString).also {
                Log.d(logTag, "observeViewModel: 이미지 URI 복원 - $uriString")
                loadImageIntoImageView(it)
                isAddPicture = true
            }
        }

        viewModel.writeStudyDataError.observe(viewLifecycleOwner) { error ->
            showErrorDialog()
        }
    }

    // 글작성 소개 유효성 검사
    private fun checkAllInput(): Boolean {
        return checkStudyTitle()&& checkStudyContent()
    }

    // 스터디 제목 유효성 검사
    private fun checkStudyTitle(): Boolean {
        with(binding) {
            fun showError(message: String) {
                textInputLayoutWriteIntroTitle.error = message
                textInputWriteIntroTitle.requestFocus()
                textInputWriteIntroTitle.shake()
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
                textInputWriteIntroContent.shake()
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

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        // 입력 내용 변경 전
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        // 입력 내용 변경 시
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding) {
                // 작성 버튼
                buttonWriteIntroNext.apply {
                    // 스터디 제목 입력 시 ViewModel에 저장
                    viewModel.updateWriteData("studyTitle", textInputWriteIntroTitle.text.toString())
                    // 스터디 내용 입력 시 ViewModel에 저장
                    viewModel.updateWriteData("studyContent", System.lineSeparator().let {
                        textInputWriteIntroContent.text.toString().replace(it, "\\n")
                    })

                    // 입력 데이터가 모두 존재할 경우
                    if(!textInputWriteIntroTitle.text.isNullOrEmpty() && !textInputWriteIntroContent.text.isNullOrEmpty()) {
                        // 버튼 활성화
                        isEnabled = true
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
                }
            }

        }
        override fun afterTextChanged(p0: Editable?) {}
    }


    // 이미지 로드 및 ImageView에 표시
    private fun loadImageIntoImageView(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap =
                    resizeBitmap(
                        rotateBitmap(
                            BitmapFactory.decodeStream(inputStream),
                            getDegree(uri).toFloat()
                        ), 1024
                    )

                Log.d(logTag, "loadImageIntoImageView: 이미지 로드 완료 - $uri")
                with(binding) {
                    cardViewCoverImageSelect.visibility = View.VISIBLE
                    imageViewCoverImageSelect.setImageBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error loading image: ${e.message}")
        }
    }

    private fun setupImageButton() {
        with(binding) {
            cardviewWriteIntroCardCover.setOnClickListener {
                Log.d(logTag, "setupImageButton: 이미지 추가 버튼 클릭")
                showPopupWindow(it)
            }

            textViewWriteIntroWriteExample.setOnClickListener {
                Log.d(logTag, "setupImageButton: 작성 예시 클릭")
                val dialog = CustomIntroDialog(requireContext())
                dialog.show()
            }
        }
    }

    // 이미지 로드 및 처리
    private fun processImageFromUri(uri: Uri) {
        val bitmap = loadAndProcessImage(uri)  // 중복된 로직 분리
        bitmap?.let {
            with(binding) {
                cardViewCoverImageSelect.visibility = View.VISIBLE
                imageViewCoverImageSelect.setImageBitmap(it)
                isAddPicture = true
            }
        }
    }

    // 이미지 로드 및 비트맵 반환
    private fun loadAndProcessImage(uri: Uri): Bitmap? {
        return BitmapFactory.decodeFile(uri.path)?.let {
            resizeBitmap(rotateBitmap(it, getDegree(uri).toFloat()), 1024)
        }
    }

    // 이미지 회전 각도 가져오기
    private fun getDegree(uri: Uri): Int {
        val context = requireContext()
        var exifInterface: ExifInterface? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val inputStream = context.contentResolver.openInputStream(uri)!!
            exifInterface = ExifInterface(inputStream)
        } else {
            exifInterface = ExifInterface(uri.path!!)
        }

        exifInterface?.let {
            val ori = it.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            val degree = when (ori) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
            Log.d(logTag, "getDegree: 이미지 회전 각도 - $degree")
            return degree
        }

        return 0
    }

    // 비트맵 회전
    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    // 비트맵 크기 조정
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val ratio = targetWidth.toDouble() / bitmap.width.toDouble()
        val targetHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)
    }

    // 이미지 선택 팝업창 표시
    private fun showPopupWindow(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupView = layoutInflater.inflate(R.layout.custom_popup_cover_image, null)

        // 팝업 윈도우 생성 및 설정
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            isOutsideTouchable = true
            width = 500
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            showAsDropDown(anchorView, -50, 0)
        }

        // 카메라 선택 버튼 클릭 리스너
        popupView.findViewById<TextView>(R.id.textView_cameraLauncher).setOnClickListener {
            Log.d(logTag, "showPopupWindow: 카메라 선택")
            val file = File(requireContext().getExternalFilesDir(null), "tempImage.jpg")
            contentUri = FileProvider.getUriForFile(requireContext(), "kr.co.lion.modigm.file_provider", file)

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cameraIntent ->
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
                cameraLauncher.launch(cameraIntent)
            }
            popupWindow.dismiss()
        }

        // 앨범 선택 버튼 클릭 리스너
        popupView.findViewById<TextView>(R.id.textView_albumLauncher).setOnClickListener {
            Log.d(logTag, "showPopupWindow: 앨범 선택")
            val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
            }
            albumLauncher.launch(albumIntent)
            popupWindow.dismiss()
        }
    }

    // 이미지 업로드 및 데이터 저장
    private fun uploadImageAndSaveData(callback: (Int) -> Unit) {
        lifecycleScope.launch {
            try {
                if (isAddPicture && this@WriteIntroFragment::contentUri.isInitialized && contentUri.toString().isNotEmpty()) {
                    Log.d(logTag, "uploadImageAndSaveData: 이미지 업로드 시작 - $contentUri")
                    val imageUrl = viewModel.uploadImageToS3(requireContext(), contentUri)
                    viewModel.updateWriteData("studyPic", imageUrl)
                } else {
                    Log.d(logTag, "uploadImageAndSaveData: 랜덤 이미지 선택")
                    val randomImageUrl = selectRandomImageFromS3()
                    viewModel.updateWriteData("studyPic", randomImageUrl)
                }
                saveDataToDatabase(callback)
            } catch (e: Exception) {
                Log.e(logTag, "Error uploading image and saving data", e)
                saveDataToDatabase(callback)
            }
        }
    }

    // 랜덤 이미지 선택
    private fun selectRandomImageFromS3(): String {
        val randomImages = listOf(
            "https://modigm-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_1.jpg",
            "https://modigm-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_2.jpg"
        )
        return randomImages[Random.nextInt(randomImages.size)].also {
            Log.d(logTag, "selectRandomImageFromS3: 랜덤 이미지 선택 - $it")
        }
    }

    // 데이터베이스에 데이터 저장
    private fun saveDataToDatabase(callback: (Int) -> Unit) {
        lifecycleScope.launch {
            try {
                Log.d(logTag, "saveDataToDatabase: 데이터 저장 시작")
                val studyIdx = viewModel.writeStudyData()
                if (studyIdx != null) {
                    Log.d(logTag, "saveDataToDatabase: 데이터 저장 성공 - studyIdx: $studyIdx")
                    callback(studyIdx)
                } else {
                    Log.e(logTag, "saveDataToDatabase: studyIdx 생성 실패")
                }
            } catch (e: Exception) {
                Log.e(logTag, "Error saving data to database", e)
            }
        }
    }

    // 글 상세 프래그먼트로 이동
    private fun navigateToDetailFragment(studyIdx: Int) {
        Log.d(logTag, "navigateToDetailFragment: DetailFragment로 이동 - studyIdx: $studyIdx")
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
    private fun showErrorDialog() {
        val dialog = CustomLoginErrorDialog(requireContext())
        with(dialog){
            setTitle("오류")
            setMessage("모두 작성해 주세요!")
            setPositiveButton("확인") {
                dismiss()
            }
            show()
        }
    }
}
