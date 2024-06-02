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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.internal.ViewUtils.hideKeyboard
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteIntroBinding
import kr.co.lion.modigm.ui.write.more.CustomDialogWriteIntroExample
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import java.io.File

class WriteIntroFragment : Fragment() {

    lateinit var fragmentWriteIntroBinding: FragmentWriteIntroBinding
    val viewModel: WriteViewModel by activityViewModels()
    val tabName = "intro"

    // 카메라 실행을 위한 런처
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    // 앨범 실행을 위한 런처
    lateinit var albumLauncher: ActivityResultLauncher<Intent>

    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    lateinit var contentUri: Uri

    // 확인할 권한 목록
    val permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_MEDIA_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentWriteIntroBinding = FragmentWriteIntroBinding.inflate(inflater)
        // 카메라 및 앨범 런처 설정
        initData()
        return fragmentWriteIntroBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingView()
        settingEvent()
    }

    fun settingEvent() {
        fragmentWriteIntroBinding.apply {
            // image 버튼 클릭 리스너
            imageViewWriteIntroCoverImage.setOnClickListener {
                showPopupWindow(it)
            }

            // 작성예시 클릭 리스너
            textViewWriteIntroWriteExample.setOnClickListener {
                // 다이얼로그를 띄워준다
                val context = requireContext()
                val dialog = CustomDialogWriteIntroExample(context)
                dialog.show()
            }

            // 제목 완료 처리 이벤트
            textInputWriteIntroTitle.apply {
                addTextChangedListener {
                    validateInput()
                }
            }

            // 내용 완료 처리 이벤트
            textInputWriteIntroContent.apply{
                addTextChangedListener {
                    validateInput()
                }

            }
        }

    }

    fun initData() {
        val context = requireContext()

        // 권한 확인
        requestPermissions(permissionList, 0)

        // 사진 촬영을 위한 런처 생성
        val contract1 = ActivityResultContracts.StartActivityForResult()
        cameraLauncher = registerForActivityResult(contract1) {
            // 사진을 사용하겠다고 한 다음에 돌아왔을 경우
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                // 사진 객체를 생성한다.
                val bitmap = BitmapFactory.decodeFile(contentUri.path)

                // 회전 각도값을 구한다.
                val degree = getDegree(contentUri)
                // 회전된 이미지를 구한다.
                val bitmap2 = rotateBitmap(bitmap, degree.toFloat())
                // 크기를 조정한 이미지를 구한다.
                val bitmap3 = resizeBitmap(bitmap2, 1024)

                fragmentWriteIntroBinding.imageViewWriteIntroCoverImage.setImageBitmap(bitmap3)

                // 사진 파일을 삭제한다.
                val file = File(contentUri.path)
                file.delete()
            }

            // 입력상태 초기화
            viewModel.userDidNotAnswer(tabName)

        }

        // 앨범 실행을 위한 런처
        val contract2 = ActivityResultContracts.StartActivityForResult()
        albumLauncher = registerForActivityResult(contract2) {
            // 사진 선택을 완료한 후 돌아왔다면
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                // 선택한 이미지의 경로 데이터를 관리하는 Uri 객체를 추출한다.
                val uri = it.data?.data
                if (uri != null) {
                    // 안드로이드 Q(10) 이상이라면
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // 이미지를 생성할 수 있는 객체를 생성한다.
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        // Bitmap을 생성한다.
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        // 컨텐츠 프로바이더를 통해 이미지 데이터에 접근한다.
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null) {
                            cursor.moveToNext()

                            // 이미지의 경로를 가져온다.
                            val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(idx)

                            // 이미지를 생성한다
                            BitmapFactory.decodeFile(source)
                        } else {
                            null
                        }
                    }

                    // 회전 각도값을 가져온다.
                    val degree = getDegree(uri)
                    // 회전 이미지를 가져온다
                    val bitmap2 = rotateBitmap(bitmap!!, degree.toFloat())
                    // 크기를 줄인 이미지를 가져온다.
                    val bitmap3 = resizeBitmap(bitmap2, 1024)

                    fragmentWriteIntroBinding.imageViewWriteIntroCoverImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    fun settingView() {

    }

    // 입력 유효성 검사
    fun validateInput(): Boolean {
        val title = fragmentWriteIntroBinding.textInputWriteIntroTitle.text.toString()
        val description = fragmentWriteIntroBinding.textInputWriteIntroContent.text.toString()

        // 제목이 비어있거나 너무 짧은 경우 검사
        if (title.isEmpty() || title.length < 8) {
            fragmentWriteIntroBinding.textInputWriteIntroTitle.error = "제목은 최소 8자 이상이어야 합니다."
            viewModel.userDidNotAnswer(tabName)
            return false
        } else {
            fragmentWriteIntroBinding.textInputWriteIntroTitle.error = null
        }

        // 소개글이 비어있거나 너무 짧은 경우 검사
        if (description.isEmpty() || description.length < 10) {
            fragmentWriteIntroBinding.textInputLayoutWriteIntroContent.error =
                "소개글은 최소 10자 이상이어야 합니다."
            viewModel.userDidNotAnswer(tabName)
            return false
        } else {
            fragmentWriteIntroBinding.textInputLayoutWriteIntroContent.error = null
        }

        viewModel.userDidAnswer(tabName)
        Log.d("TedMoon", "Write Skill : ${viewModel.introClicked.value}")
        return true
    }

    // 사진의 회전 각도값을 반환하는 메서드
    // ExifInterface : 사진, 영상, 소리 등의 파일에 기록한 정보
    // 위치, 날짜, 조리개값, 노출 정도 등등 다양한 정보가 기록된다.
    // ExifInterface 정보에서 사진 회전 각도값을 가져와서 그만큼 다시 돌려준다.
    fun getDegree(uri: Uri): Int {
        val context = requireContext()
        // 사진 정보를 가지고 있는 객체 가져온다.
        var exifInterface: ExifInterface? = null


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 이미지 데이터를 가져올 수 있는 Content Provide의 Uri를 추출한다.
            // val photoUri = MediaStore.setRequireOriginal(uri)
            // ExifInterface 정보를 읽어올 스트림을 추출한다.

            val inputStream = context.contentResolver.openInputStream(uri)!!
            // ExifInterface 객체를 생성한다.
            exifInterface = ExifInterface(inputStream)
        } else {
            // ExifInterface 객체를 생성한다.
            exifInterface = ExifInterface(uri.path!!)
        }

        if (exifInterface != null) {
            // 반환할 각도값을 담을 변수
            var degree = 0
            // ExifInterface 객체에서 회전 각도값을 가져온다.
            val ori = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)

            degree = when (ori) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            return degree
        }

        return 0
    }

    // 회전시키는 메서드
    fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        // 회전 이미지를 생성하기 위한 변환 행렬
        val matrix = Matrix()
        matrix.postRotate(degree)

        // 회전 행렬을 적용하여 회전된 이미지를 생성한다.
        // 첫 번째 : 원본 이미지
        // 두 번째와 세번째 : 원본 이미지에서 사용할 부분의 좌측 상단 x, y 좌표
        // 네번째와 다섯번째 : 원본 이미지에서 사용할 부분의 가로 세로 길이
        // 여기에서는 이미지데이터 전체를 사용할 것이기 때문에 전체 영역으로 잡아준다.
        // 여섯번째 : 변환행렬. 적용해준 변환행렬이 무엇이냐에 따라 이미지 변형 방식이 달라진다.
        val rotateBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

        return rotateBitmap
    }

    // 이미지 사이즈를 조정하는 메서드
    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
        // 이미지의 확대/축소 비율을 구한다.
        val ratio = targetWidth.toDouble() / bitmap.width.toDouble()
        // 세로 길이를 구한다.
        val targetHeight = (bitmap.height * ratio).toInt()
        // 크기를 조장한 Bitmap을 생성한다.
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)

        return resizedBitmap
    }

    fun showPopupWindow(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupView = layoutInflater.inflate(R.layout.custom_popup_menu_write_intro, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        // 팝업 윈도우 위치 조정 (간접적 마진 효과)
        popupWindow.showAsDropDown(anchorView, -50, 0)
        // 팝업 윈도우 크기 조정
        popupWindow.width = 500  // 너비를 500px로 설정
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 따라 자동 조절


        // 각 메뉴 아이템에 대한 클릭 리스너 설정

        // 카메라에서 사진 등록
        popupView.findViewById<TextView>(R.id.textView_cameraLauncher).setOnClickListener {

            val context = requireContext()
            // 촬영한 사진이 저장될 경로
            // 외부 저장소 중에 애플리케이션 영역 경로를 가져온다.
            val rootPath = context.getExternalFilesDir(null).toString()
            // 이미지 파일명을 포함한 경로
            val picPath = "${rootPath}/tempImage.jpg"
            // File 객체 생성
            val file = File(picPath)
            // 사진이 저장된 위치를 관리할 Uri 생성
            // AndroidManfiest.xml 에 등록한 provider의 authorities
            val a1 = "kr.co.lion.modigm.file_provider"
            contentUri = FileProvider.getUriForFile(context, a1, file)

            if (contentUri != null) {
                // 실행할 액티비티를 카메라 액티비티로 지정한다.
                // 단말기에 설치되어 있는 모든 애플리케이션이 가진 액티비티 중에 사진촬영이
                // 가능한 액티비가 실행된다.
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                // 이미지가 저장될 경로를 가지고 있는 Uri 객체를 인텐트에 담아준다.
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
                // 카메라 액티비티 실행
                cameraLauncher.launch(cameraIntent)
            }

            popupWindow.dismiss()
        }

        // 앨범에서 사진 등록
        popupView.findViewById<TextView>(R.id.textView_albumLauncher).setOnClickListener {

            // 앨범에서 사진을 선택할 수 있도록 셋팅된 인텐트를 생성한다.
            val albumIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // 실행할 액티비티의 타입을 설정(이미지를 선택할 수 있는 것이 뜨게 한다)
            albumIntent.setType("image/*")
            // 선택할 수 있는 파들의 MimeType을 설정한다.
            // 여기서 선택한 종류의 파일만 선택이 가능하다. 모든 이미지로 설정한다.
            val mimeType = arrayOf("image/*")
            albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
            // 액티비티를 실행한다.
            albumLauncher.launch(albumIntent)

            popupWindow.dismiss()
        }
        // 팝업 윈도우 표시
        popupWindow.showAsDropDown(anchorView)
    }
}