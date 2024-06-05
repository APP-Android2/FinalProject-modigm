package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailEditBinding
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import java.io.File


class DetailEditFragment : Fragment(), OnSkillSelectedListener, OnPlaceSelectedListener {

    lateinit var fragmentDetailEditBinding: FragmentDetailEditBinding

    private val viewModel: DetailViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String


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

    private var currentStudyData: StudyData? = null

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentDetailEditBinding = FragmentDetailEditBinding.inflate(inflater, container, false)

        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

        auth = FirebaseAuth.getInstance()
//        uid = auth.currentUser?.uid.toString()

        // 로그인 구현 완료되면 지우겠습니다
        uid = "J04y39mPQ8fLIm2LukmdpRVGN8b2"

        // 카메라 및 앨범 런처 설정
        initData()

        return fragmentDetailEditBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingToolbar()
        setupBottomSheet()
        setupButton()
        setupChipGroups()

        // ViewModel에서 데이터 요청
        viewModel.selectContentData(studyIdx)

        observeViewModel()
    }
    fun observeViewModel() {

        viewModel.contentData.observe(viewLifecycleOwner) { data ->
            data?.let {
                currentStudyData = it // 여기서 데이터를 업데이트합니다.
                updateUIIfReady() // UI 업데이트 체크
                preselectChips()
            }
        }
    }

    fun updateUIIfReady() {
        val studyData = currentStudyData
        if (studyData != null) {
            updateUI(studyData)
        }
    }

    fun updateUI(data: StudyData) {
        with(fragmentDetailEditBinding) {
            cardViewCoverImageSelect.visibility = View.VISIBLE

            // studyPic이 사용 가능한지 확인하고 커버 이미지 설정
            if (!data.studyPic.isNullOrEmpty()) {

                // Firebase Storage 경로
                val storageReference: StorageReference =
                    FirebaseStorage.getInstance().reference.child("studyPic/${data.studyPic}")

                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this@DetailEditFragment)
                        .load(uri)
                        .into(imageViewCoverImageSelect) // imageViewDetailCover가 ImageView ID라고 가정
                }.addOnFailureListener {
                    // 로그 오류 또는 실패 처리
                    Log.e("DetailFragment", "Storage에서 이미지 로드 실패: ${it.message}")
                }
            } else {

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

//                fragmentDetailEditBinding.buttonDetailCover.icon = BitmapDrawable(resources, bitmap3)
                fragmentDetailEditBinding.cardViewCoverImageSelect.visibility = View.VISIBLE
                fragmentDetailEditBinding.imageViewCoverImageSelect.setImageBitmap(bitmap3)

                // 사진 파일을 삭제한다.
                val file = File(contentUri.path)
                file.delete()
            }

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

//                    fragmentDetailEditBinding.buttonDetailCover.icon = BitmapDrawable(resources, bitmap3)
                    fragmentDetailEditBinding.cardViewCoverImageSelect.visibility = View.VISIBLE
                    fragmentDetailEditBinding.imageViewCoverImageSelect.setImageBitmap(bitmap)
                }
            }
        }
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
        val popupView = layoutInflater.inflate(R.layout.custom_popup_cover_image, null)

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

    // 인터페이스 구현
    // bottomSheet에서 선택한 항목의 제목
    override fun onPlaceSelected(placeName: String, detailPlaceName:String) {
        val test = "$placeName\n$detailPlaceName"
        fragmentDetailEditBinding.editTextDetailEditTitleLocation.setText(test)
    }

    // 툴바 설정
    fun settingToolbar() {
        fragmentDetailEditBinding.apply {
            toolBarDetailEdit.apply {
                title="게시글 수정"
                //네비게이션
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    // 칩 그룹 설정
    fun setupChipGroups() {
        setupChipGroup(
            fragmentDetailEditBinding.chipGroupDetailEditType,
            listOf("스터디", "공모전", "프로젝트")
        )

        setupChipGroup(
            fragmentDetailEditBinding.chipGroupDetailEditPlace,
            listOf("오프라인", "온라인", "온·오프 혼합")
        )

        setupChipGroup(
            fragmentDetailEditBinding.chipGroupDetailEditApply,
            listOf("신청제", "선착순")
        )

        // 장소선택 가시성 설정
        setPlaceSelectionListener()
    }

    // 장소 선택에 따른 UI 가시성 조절
    fun setPlaceSelectionListener() {
        // 반복하며 칩에 클릭 리스너 설정
        fragmentDetailEditBinding.chipGroupDetailEditPlace.children.forEach { view ->
            (view as Chip).setOnClickListener {
                // 칩 클릭시 가시성 업데이트 함수 호출
                updatePlaceVisibility(view)
                // 칩 스타일 업데이트
                updateChipStyles(fragmentDetailEditBinding.chipGroupDetailEditPlace, view.id)
            }
        }
    }

    // 가시성 업데이트
    fun updatePlaceVisibility(chip: Chip) {
        when (chip.text.toString()) {
            // '온라인'이 선택된 경우 장소 선택 입력 필드 숨김
            "온라인" -> fragmentDetailEditBinding.textInputLayoutDetailEditPlace.visibility = View.GONE
//            else -> fragmentDetailEditBinding.textInputLayoutDetailEditPlace.visibility = View.VISIBLE
            else -> {
                fragmentDetailEditBinding.textInputLayoutDetailEditPlace.visibility = View.VISIBLE

                // currentStudyData에서 studyPlace와 studyDetailPlace 데이터를 가져와 합친다음 editTextDetailEditTitleLocation에 값을 넣기
                val placeName = currentStudyData?.studyPlace
                val detailPlaceName = currentStudyData?.studyDetailPlace
                val test = "$placeName\n$detailPlaceName"
                fragmentDetailEditBinding.editTextDetailEditTitleLocation.setText(test)
            }
        }
    }

    // 칩 스타일 업데이트
    fun updateChipStyles(group: ChipGroup, checkedId: Int) {
        group.children.forEach { view ->
            if (view is Chip) {
                // 현재 칩이 선택된 상태인지 확인
                val isSelected = view.id == checkedId
                // 선택된 칩의 스타일 업데이트
                updateChipStyle(view, isSelected)
            }
        }
    }

    // 각 그룹에 칩 추가
    fun setupChipGroup(chipGroup: ChipGroup, chipNames: List<String>) {
        chipGroup.isSingleSelection = true  // Single selection 모드 활성화

        chipNames.forEach { name ->
            val chip = Chip(context).apply {
                text = name
                id = View.generateViewId()  // 동적으로 ID 생성
                isClickable = true
                isCheckable = true
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
            }
            // 칩을 칩그룹에 추가
            chipGroup.addView(chip)
            setupChipListener(chip, chipGroup)
        }
    }

    // 개별 칩에 클릭리스너 설정
    fun setupChipListener(chip: Chip, chipGroup: ChipGroup) {
        chip.setOnClickListener {
            // 클릭된 칩이 현재 선택되지 않았다면, 선택 처리
            if (!chip.isChecked) {
                // 모든 칩의 선택 상태를 해제하고, 클릭된 칩만 선택
                chipGroup.children.forEach { view ->
                    (view as? Chip)?.isChecked = false
                }
                // 클릭된 칩을 선택 상태로 설정
                chip.isChecked = true
            }
            // 칩 스타일 업데이트
            chipGroup.children.forEach { view ->
                if (view is Chip) {
                    updateChipStyle(view, view.isChecked)
                }
            }
        }
    }

    // 칩 스타일 업데이트
    fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        val context = chip.context
        val backgroundColor = if (isSelected) ContextCompat.getColor(
            context,
            R.color.pointColor
        ) else ContextCompat.getColor(context, R.color.white)
        val textColor = if (isSelected) ContextCompat.getColor(
            context,
            R.color.white
        ) else ContextCompat.getColor(context, R.color.black)

        chip.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
        chip.setTextColor(textColor)
    }

    override fun onSkillSelected(selectedSkills: List<String>) {
        // ChipGroup에 칩 추가
        addChipsToGroup(fragmentDetailEditBinding.ChipGroupDetailEdit, selectedSkills)
    }

    fun addChipsToGroup(chipGroup: ChipGroup, skills: List<String>) {
        // 기존의 칩들을 삭제
        chipGroup.removeAllViews()

        // 전달받은 스킬 리스트를 이용하여 칩을 생성 및 추가
        for (skill in skills) {
            val chip = Chip(context).apply {
                text = skill
                isClickable = true
                isCheckable = true
                isCloseIconVisible=true
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                id = View.generateViewId()

                // 'X' 아이콘 클릭시 해당 칩을 ChipGroup에서 제거
                setOnCloseIconClickListener {
                    chipGroup.removeView(this)  // 'this'는 현재 클릭된 Chip 인스턴스를 참조
                }
            }
            chipGroup.addView(chip)
        }
    }

    fun setupBottomSheet() {
        fragmentDetailEditBinding.textInputLayoutDetailEditSkill.editText?.setOnClickListener {
            // bottom sheet
            val bottomSheet = SkillBottomSheetFragment().apply {
                setOnSkillSelectedListener(this@DetailEditFragment)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        // 프래그 먼트간 연결 설정
        fragmentDetailEditBinding.textInputLayoutDetailEditPlace.editText?.setOnClickListener {
            val bottomSheet = PlaceBottomSheetFragment().apply {
                setOnPlaceSelectedListener(this@DetailEditFragment)
            }
            bottomSheet.show(childFragmentManager,bottomSheet.tag)
        }

    }

    fun setupButton() {
        fragmentDetailEditBinding.buttonDetailEditDone.setOnClickListener {
            if (validateInputs()) {
                // 모든 입력이 유효한 경우 데이터 저장 또는 처리
                saveData()
            }
        }

        // 작성 예시보기 text클릭
        fragmentDetailEditBinding.textviewDetailIntroEx.setOnClickListener {
            val dialog = CustomIntroDialog(requireContext())
            dialog.show()
        }

        // 커버 이미지 추가
        fragmentDetailEditBinding.imageViewDetailCover.setOnClickListener {
            showPopupWindow(it)
        }
    }

    fun saveData() {

        val snackbar =
            Snackbar.make(fragmentDetailEditBinding.root, "수정되었습니다", Snackbar.LENGTH_LONG)

        // 스낵바의 뷰를 가져옵니다.
        val snackbarView = snackbar.view

        // 스낵바 텍스트 뷰 찾기
        val textView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        // 텍스트 크기를 dp 단위로 설정
        val textSizeInPx = dpToPx(requireContext(), 16f)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)

        snackbar.show()
    }

    // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 입력 유효성 검사
    fun validateInputs(): Boolean {
        val title = fragmentDetailEditBinding.editTextDetailEditTitle.text.toString()
        val description = fragmentDetailEditBinding.editTextDetailEditContext.text.toString()

        // 제목이 비어있거나 너무 짧은 경우 검사
        if (title.isEmpty() || title.length < 8) {
            fragmentDetailEditBinding.textInputLayoutDetailEditTitle.error = "제목은 최소 8자 이상이어야 합니다."
            return false
        } else {
            fragmentDetailEditBinding.textInputLayoutDetailEditTitle.error = null
        }

        // 소개글이 비어있거나 너무 짧은 경우 검사
        if (description.isEmpty() || description.length < 10) {
            fragmentDetailEditBinding.textInputLayoutDetailEditContext.error =
                "소개글은 최소 10자 이상이어야 합니다."
            return false
        } else {
            fragmentDetailEditBinding.textInputLayoutDetailEditContext.error = null
        }

        return true
    }

    // 사용자가 이전에 선택한 내용 선택(나중에 DB에서가져올 예정)
    fun preselectChips() {
        // 스터디 타입 칩 선택
        val studyTypeText = when (currentStudyData?.studyType) {
            1 -> "스터디"
            2 -> "프로젝트"
            3 -> "공모전"
            else -> ""
        }
        findChipByText(fragmentDetailEditBinding.chipGroupDetailEditType, studyTypeText)?.let {
            it.isChecked = true
            updateChipStyle(it, true)
        }

        // 온오프라인 타입 칩 선택
        val onOfflineText = when (currentStudyData?.studyOnOffline) {
            1 -> "온라인"
            2 -> "오프라인"
            3 -> "온·오프 혼합"
            else -> ""
        }
        findChipByText(fragmentDetailEditBinding.chipGroupDetailEditPlace, onOfflineText)?.let {
            it.isChecked = true
            updateChipStyle(it, true)
            updatePlaceVisibility(it) // UI 가시성 설정
        }

        // 신청 방법 칩 선택
        val applyMethodText = when (currentStudyData?.studyApplyMethod) {
            1 -> "신청제"
            2 -> "선착순"
            else -> ""
        }
        findChipByText(fragmentDetailEditBinding.chipGroupDetailEditApply, applyMethodText)?.let {
            it.isChecked = true
            updateChipStyle(it, true)
        }
    }

    // 특정 텍스트를 가진 칩을 찾는 함수
    fun findChipByText(chipGroup: ChipGroup, text: String): Chip? {
        // 해당 텍스트를 가진 첫 번째 칩 반환, 없으면 null 반환
        return chipGroup.children.firstOrNull { (it as Chip).text == text } as? Chip
    }

}