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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteIntroBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.CustomIntroDialog
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName
import java.io.File
import kotlin.random.Random

class WriteIntroFragment : VBBaseFragment<FragmentWriteIntroBinding>(FragmentWriteIntroBinding::inflate) {

    // 뷰모델
    val viewModel: WriteViewModel by activityViewModels()

    // 태그
    private val logTag by lazy { WriteIntroFragment::class.simpleName }

    // 카메라 실행을 위한 런처
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    // 앨범 실행을 위한 런처
    private lateinit var albumLauncher: ActivityResultLauncher<Intent>

    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    private lateinit var contentUri: Uri

    // 확인할 권한 목록
    private val permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_MEDIA_LOCATION
    )

    // 이미지를 첨부한 적이 있는지
    private var isAddPicture = false

    // 권한 요청 런처 선언
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 권한 요청 런처 초기화
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // 권한 요청 결과 처리
            permissions.entries.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Log.d(logTag, "$permission 권한이 허용되었습니다.")
                } else {
                    Log.d(logTag, "$permission 권한이 거부되었습니다.")
                }
            }
        }

        // 권한 요청 실행
        requestPermissionsIfNeeded()

        Log.d(logTag, "onViewCreated: View 초기화 시작")
        initView()

        Log.d(logTag, "onViewCreated: ViewModel 데이터 복원 시작")
        observeViewModel()

        // 텍스트 입력 변경 사항을 관찰
        binding.textInputWriteIntroTitle.addTextChangedListener {
            Log.d(logTag, "onViewCreated: Title 입력 변경 - ${binding.textInputWriteIntroTitle.text}")
            viewModel.updateWriteData("studyTitle", binding.textInputWriteIntroTitle.text.toString())
        }

        binding.textInputWriteIntroContent.addTextChangedListener {
            Log.d(logTag, "onViewCreated: Content 입력 변경")
            viewModel.updateWriteData(
                "studyContent",
                binding.textInputWriteIntroContent.text.toString().replace(System.getProperty("line.separator"), "\\n")
            )
        }
    }



    // 권한 요청 함수
    private fun requestPermissionsIfNeeded() {
        permissionLauncher.launch(permissionList)
    }

    private fun initView() {
        with(binding) {
            initData()
            setupImageButton()

            // 작성 버튼 클릭 시
            buttonWriteIntroNext.setOnClickListener {
                Log.d(logTag, "initView: 작성 버튼 클릭")
                uploadImageAndSaveData(::navigateToDetailFragment)
            }
        }
    }

    private fun observeViewModel() {
        (viewModel.getUpdateData("studyPic") as? String)?.let { uriString ->
            contentUri = Uri.parse(uriString).also {
                Log.d(logTag, "observeViewModel: 이미지 URI 복원 - $uriString")
                loadImageIntoImageView(it)
                isAddPicture = true
            }
        }
    }

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

    private fun initData() {
        requestPermissions(permissionList, 0)
        Log.d(logTag, "initData: 권한 요청 - ${permissionList.joinToString(", ")}")

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                contentUri.let { uri ->
                    processImageFromUri(uri)
                    viewModel.updateWriteData("studyPic", uri.toString())
                }
            }
        }

        albumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { selectedUri ->
                    contentUri = selectedUri
                    processImageFromUri(selectedUri)
                    viewModel.updateWriteData("studyPic", selectedUri.toString())
                }
            }
        }
    }

    private fun processImageFromUri(uri: Uri) {
        val bitmap = loadAndProcessImage(uri)  // 중복된 로직 분리
        bitmap?.let {
            with(binding) {
                cardViewCoverImageSelect.visibility = View.VISIBLE
                imageViewCoverImageSelect.setImageBitmap(it)
                isAddPicture = true
                validateIntro()
            }
        }
    }

    private fun loadAndProcessImage(uri: Uri): Bitmap? {
        return BitmapFactory.decodeFile(uri.path)?.let {
            rotateBitmap(it, getDegree(uri).toFloat()).let { resized -> resizeBitmap(resized, 1024) }
        }
    }

    private fun validateIntro() {
        val isTitleValid = binding.textInputWriteIntroTitle.text.toString().length >= 8
        val isContentValid = binding.textInputWriteIntroContent.text.toString().length >= 10
        Log.d(logTag, "validateIntro: Title 유효성 $isTitleValid, Content 유효성 $isContentValid")
    }

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

    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val ratio = targetWidth.toDouble() / bitmap.width.toDouble()
        val targetHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)
    }

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

    private fun selectRandomImageFromS3(): String {
        val randomImages = listOf(
            "https://modigm-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_1.jpg",
            "https://modigm-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_2.jpg"
        )
        return randomImages[Random.nextInt(randomImages.size)].also {
            Log.d(logTag, "selectRandomImageFromS3: 랜덤 이미지 선택 - $it")
        }
    }

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
}
