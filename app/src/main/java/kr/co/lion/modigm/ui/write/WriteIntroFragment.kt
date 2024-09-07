package kr.co.lion.modigm.ui.write

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
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
import kr.co.lion.modigm.db.write.RemoteWriteStudyDao
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.CustomIntroDialog
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName
import java.io.File
import kotlin.random.Random

class WriteIntroFragment : VBBaseFragment<FragmentWriteIntroBinding>(FragmentWriteIntroBinding::inflate) {

    val viewModel: WriteViewModel by activityViewModels()
    private val TAG = "WriteIntroFragment"  // 로그 태그

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

    private lateinit var remoteWriteStudyDao: RemoteWriteStudyDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: View 초기화 시작")
        initView()

        Log.d(TAG, "onViewCreated: ViewModel 데이터 복원 시작")
        observeViewModel()

        // 텍스트 입력 변경 사항을 관찰
        binding.textInputWriteIntroTitle.addTextChangedListener {
            Log.d(TAG, "onViewCreated: Title 입력 변경 - ${binding.textInputWriteIntroTitle.text}")
            viewModel.updateWriteData("studyTitle", binding.textInputWriteIntroTitle.text.toString())
        }

        binding.textInputWriteIntroContent.addTextChangedListener {
            Log.d(TAG, "onViewCreated: Content 입력 변경")
            viewModel.updateWriteData(
                "studyContent",
                binding.textInputWriteIntroContent.text.toString().replace(System.getProperty("line.separator"), "\\n")
            )
        }
    }

    private fun initView() {
        with(binding) {
            remoteWriteStudyDao = RemoteWriteStudyDao()
            Log.d(TAG, "initView: RemoteWriteStudyDao 초기화 완료")

            initData()
            setupImageButton()

            buttonWriteIntroNext.setOnClickListener {
                Log.d(TAG, "initView: 작성 버튼 클릭")
                uploadImageAndSaveData { studyIdx ->
                    Log.d(TAG, "initView: 데이터 저장 후 DetailFragment로 이동")
                    navigateToDetailFragment(studyIdx)
                }
            }

            remoteWriteStudyDao.setContext(requireContext())
            Log.d(TAG, "initView: RemoteWriteStudyDao context 설정 완료")
        }
    }

    private fun observeViewModel() {
        val studyPic = viewModel.getUpdateData("studyPic") as? String
        studyPic?.let { uriString ->
            val uri = Uri.parse(uriString)
            if (uri != null) {
                contentUri = uri
                Log.d(TAG, "observeViewModel: 이미지 URI 복원 - $uriString")
                loadImageIntoImageView(uri)
                isAddPicture = true
            }
        }
    }

    private fun loadImageIntoImageView(uri: Uri) {
        with(binding) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val degree = getDegree(uri)
                val bitmap2 = rotateBitmap(bitmap, degree.toFloat())
                val bitmap3 = resizeBitmap(bitmap2, 1024)

                Log.d(TAG, "loadImageIntoImageView: 이미지 로드 완료 - $uri")

                cardViewCoverImageSelect.visibility = View.VISIBLE
                imageViewCoverImageSelect.setImageBitmap(bitmap3)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}")
            }
        }
    }

    private fun setupImageButton() {
        with(binding) {
            cardviewWriteIntroCardCover.setOnClickListener {
                Log.d(TAG, "setupImageButton: 이미지 추가 버튼 클릭")
                showPopupWindow(it)
            }

            textViewWriteIntroWriteExample.setOnClickListener {
                Log.d(TAG, "setupImageButton: 작성 예시 클릭")
                val dialog = CustomIntroDialog(requireContext())
                dialog.show()
            }
        }
    }

    private fun initData() {
        with(binding) {
            val context = requireContext()

            requestPermissions(permissionList, 0)
            Log.d(TAG, "initData: 권한 요청 - ${permissionList.joinToString(", ")}")

            val contract1 = ActivityResultContracts.StartActivityForResult()
            cameraLauncher = registerForActivityResult(contract1) {
                if (it.resultCode == AppCompatActivity.RESULT_OK) {
                    contentUri.let { uri ->
                        val bitmap = BitmapFactory.decodeFile(uri.path)
                        val degree = getDegree(uri)
                        val bitmap2 = rotateBitmap(bitmap, degree.toFloat())
                        val bitmap3 = resizeBitmap(bitmap2, 1024)

                        viewModel.updateWriteData("studyPic", uri.toString())
                        Log.d(TAG, "initData: 카메라에서 이미지 저장 - $uri")

                        cardViewCoverImageSelect.visibility = View.VISIBLE
                        imageViewCoverImageSelect.setImageBitmap(bitmap3)
                        isAddPicture = true
                        validateIntro()
                    }
                }
            }

            val contract2 = ActivityResultContracts.StartActivityForResult()
            albumLauncher = registerForActivityResult(contract2) {
                if (it.resultCode == AppCompatActivity.RESULT_OK) {
                    val uri = it.data?.data
                    uri?.let { selectedUri ->
                        contentUri = selectedUri
                        Log.d(TAG, "initData: 앨범에서 이미지 선택 - $selectedUri")

                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val source = ImageDecoder.createSource(context.contentResolver, selectedUri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            val cursor = context.contentResolver.query(selectedUri, null, null, null, null)
                            cursor?.let {
                                it.moveToNext()
                                val idx = it.getColumnIndex(MediaStore.Images.Media.DATA)
                                val source = it.getString(idx)
                                BitmapFactory.decodeFile(source)
                            }
                        }

                        bitmap?.let {
                            val degree = getDegree(selectedUri)
                            val bitmap2 = rotateBitmap(it, degree.toFloat())
                            val bitmap3 = resizeBitmap(bitmap2, 1024)

                            viewModel.updateWriteData("studyPic", uri.toString())
                            Log.d(TAG, "initData: 이미지 URI 저장 - $uri")

                            cardViewCoverImageSelect.visibility = View.VISIBLE
                            imageViewCoverImageSelect.setImageBitmap(bitmap3)
                            isAddPicture = true
                            validateIntro()
                        }
                    }
                }
            }
        }
    }

    private fun validateIntro() {
        val isTitleValid = binding.textInputWriteIntroTitle.text.toString().length >= 8
        val isContentValid = binding.textInputWriteIntroContent.text.toString().length >= 10
        Log.d(TAG, "validateIntro: Title 유효성 $isTitleValid, Content 유효성 $isContentValid")
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
            Log.d(TAG, "getDegree: 이미지 회전 각도 - $degree")
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
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(anchorView, -50, 0)
        popupWindow.width = 500
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT

        popupView.findViewById<TextView>(R.id.textView_cameraLauncher).setOnClickListener {
            Log.d(TAG, "showPopupWindow: 카메라 선택")
            val context = requireContext()
            val rootPath = context.getExternalFilesDir(null).toString()
            val picPath = "${rootPath}/tempImage.jpg"
            val file = File(picPath)
            val a1 = "kr.co.lion.modigm.file_provider"
            contentUri = FileProvider.getUriForFile(context, a1, file)

            if (contentUri != null) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
                cameraLauncher.launch(cameraIntent)
            }

            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.textView_albumLauncher).setOnClickListener {
            Log.d(TAG, "showPopupWindow: 앨범 선택")
            val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            albumIntent.setType("image/*")
            val mimeType = arrayOf("image/*")
            albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
            albumLauncher.launch(albumIntent)
            popupWindow.dismiss()
        }
    }

    private fun uploadImageAndSaveData(callback: (Int) -> Unit) {
        lifecycleScope.launch {
            if (isAddPicture && this@WriteIntroFragment::contentUri.isInitialized && contentUri.toString().isNotEmpty()) {
                try {
                    Log.d(TAG, "uploadImageAndSaveData: 이미지 업로드 시작 - $contentUri")
                    val imageUrl = viewModel.uploadImageToS3(requireContext(), contentUri)
                    viewModel.updateWriteData("studyPic", imageUrl)
                    saveDataToDatabase(callback)
                } catch (e: Exception) {
                    Log.e(TAG, "Error uploading image and saving data", e)
                    saveDataToDatabase(callback)  // 이미지 업로드 실패 시에도 데이터 저장
                }
            } else {
                Log.d(TAG, "uploadImageAndSaveData: 랜덤 이미지 선택")
                selectRandomImageFromS3 { randomImageUrl ->
                    viewModel.updateWriteData("studyPic", randomImageUrl)
                    saveDataToDatabase(callback)
                }
            }
        }
    }

    private fun selectRandomImageFromS3(callback: (String) -> Unit) {
        val randomImages = listOf(
            "https://modigm-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_1.jpg",
            "https://modigm-bucket.s3.ap-northeast-2.amazonaws.com/image_detail_2.jpg"
        )
        val randomIndex = Random.nextInt(randomImages.size)
        Log.d(TAG, "selectRandomImageFromS3: 랜덤 이미지 선택 - ${randomImages[randomIndex]}")
        callback(randomImages[randomIndex])
    }

    private fun saveDataToDatabase(callback: (Int) -> Unit) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "saveDataToDatabase: 데이터 저장 시작")
                val studyIdx = viewModel.writeStudyData()
                if (studyIdx != null) {
                    Log.d(TAG, "saveDataToDatabase: 데이터 저장 성공 - studyIdx: $studyIdx")
                    callback(studyIdx)
                } else {
                    Log.e(TAG, "saveDataToDatabase: studyIdx 생성 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving data to database", e)
            }
        }
    }

    private fun navigateToDetailFragment(studyIdx: Int) {
        Log.d(TAG, "navigateToDetailFragment: DetailFragment로 이동 - studyIdx: $studyIdx")
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
