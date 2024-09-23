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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailEditBinding
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.util.Skill
import java.io.File


class DetailEditFragment : VBBaseFragment<FragmentDetailEditBinding>(FragmentDetailEditBinding::inflate), OnSkillSelectedListener, OnPlaceSelectedListener {

//    lateinit var fragmentDetailEditBinding: FragmentDetailEditBinding

    private val viewModel: DetailViewModel by activityViewModels()

//    private lateinit var auth: FirebaseAuth
//    private lateinit var uid: String

    // 선택된 장소 이름
    private var selectedPlaceName: String = ""
    // 선택된 상세 장소 이름
    private var selectedDetailPlaceName: String = ""

    // 선택된 스킬 목록
    private var selectedSkills: List<Int> = listOf()

    // 카메라 실행을 위한 런처
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    // 앨범 실행을 위한 런처
    lateinit var albumLauncher: ActivityResultLauncher<Intent>

    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    var contentUri: Uri? = null

    // 확인할 권한 목록
    val permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_MEDIA_LOCATION
    )

    // 현재 스터디 데이터
    private var currentStudyData: StudyData? = null

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    // 최소 인원 수를 저장할 변수
    private var minMembers: Int = 1

    private var selectedSkillList: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

//        auth = FirebaseAuth.getInstance()
//        uid = auth.currentUser?.uid.toString()

        // 카메라 및 앨범 런처 설정
        initData()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel에서 데이터 요청
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getStudy(studyIdx)
        } // ViewModel을 통해 데이터 요청

        observeViewModel() // ViewModel 관찰

        settingToolbar()  // 툴바 설정
        setupBottomSheet() // 바텀 시트
        setupButton() // 버튼
        setupChipGroups() // 칩 그룹


        // 백 스택 로그 출력
        logFragmentBackStack(parentFragmentManager)

        // 스킬 데이터를 로드
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTechIdxByStudyIdx(studyIdx)
        }

        // 입력값 검증 로직 메서드 호출
//        setupMemberInputWatcher()

    }

    fun logFragmentBackStack(fragmentManager: FragmentManager) {
        val backStackEntryCount = fragmentManager.backStackEntryCount
        Log.d("BackStack", "Total Back Stack Entry Count: $backStackEntryCount")
        for (index in 0 until backStackEntryCount) {
            val backStackEntry = fragmentManager.getBackStackEntryAt(index)
            Log.d("BackStack", "Entry $index: ${backStackEntry.name}")
        }
    }

    fun safeContext(): Context? {
        return if (isAdded) {
            context
        } else {
            null
        }
    }

    fun observeViewModel() {
        // studyData 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.studyData.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { data ->
                data?.let {
                    currentStudyData = it // 여기서 데이터를 업데이트합니다.
                    Log.d("DetailEditFragment", "Received study data: $it")
                    updateUIIfReady() // UI 업데이트 체크
                    preselectChips() // 칩 선택 사전 설정

                    if (it.studyPic.isNotEmpty()) {
                        viewModel.getStudyPic(it.studyIdx) // 파일 이름을 사용하여 스터디 이미지 로드
                    }

                    setupMemberInputWatcher() // 최소 인원 수를 반영하여 TextWatcher 설정
                }
            }
        }

        // updateResult 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateResult.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { isSuccess ->
                isSuccess?.let {
                    val message = if (it) "정보가 업데이트되었습니다." else "업데이트 실패"
                    val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

                    // 스낵바의 텍스트 뷰 찾기
                    val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                    // dpToPx 메서드를 사용하여 dp를 픽셀로 변환
                    val textSizeInPx = dpToPx(requireContext(), 14f) // 예시: 텍스트 크기 14 dp
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)

                    snackbar.show()

                    if (it) {
                        viewModel.clearUpdateResult()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }

        // studyPic 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.studyPic.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { uri ->
                safeContext()?.let { context ->
                    binding.cardViewCoverImageSelect.visibility = View.VISIBLE // 이미지 선택 카드뷰 가시성 설정

                    Glide.with(context)
                        .load(uri)
                        .error(R.drawable.icon_error_24px) // 에러 발생시 보여줄 이미지
                        .into(binding.imageViewCoverImageSelect)
                }
            }
        }

        // 최소 인원 수 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.memberCount.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { count ->
                minMembers = count
                Log.d("DetailEditFragment", "Minimum members: $minMembers")
            }
        }

        // 스킬 데이터 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.studyTechList.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { techList ->
                Log.d("DetailEditFragment", "Received techList from ViewModel: $techList")
                val skills = techList.map { Skill.fromNum(it) }  // techIdx를 Skill 객체로 변환
                addChipsToGroup(binding.ChipGroupDetailEdit, skills)
            }
        }
    }

    // UI 업데이트가 준비되었는지 확인
    fun updateUIIfReady() {
        val studyData = currentStudyData
        if (studyData != null) {
            updateUI(studyData) // UI 업데이트 실행
        }
    }

    // 실제로 UI 업데이트 수행
    fun updateUI(data: StudyData) {
        with(binding) {
            // 제목
            editTextDetailEditTitle.setText(data.studyTitle)

            // 내용
            editTextDetailEditContext.setText(data.studyContent.replace("\\n", System.getProperty("line.separator")))

            // 인원수
            editTextDetailEditMember.setText(data.studyMaxMember.toString())

            // 참여신청 링크
            editTextDetailEditLink.setText(data.studyChatLink ?: "") // studyChatLink 값을 설정
        }
    }

    // 인원수 입력 설정
    fun setupMemberInputWatcher() {
        binding.editTextDetailEditMember.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 변경 전에 호출됩니다.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경되는 동안 호출됩니다.
                if (s != null && s.startsWith("0") && s.length > 1) {
                    // 입력된 값이 "0"으로 시작하고 길이가 1 초과인 경우, "0"을 제거합니다.
                    val correctString = s.toString().substring(1)
                    binding.editTextDetailEditMember.setText(correctString)
                    binding.editTextDetailEditMember.setSelection(correctString.length) // 커서를 수정된 텍스트의 끝으로 이동
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후 호출됩니다.
                s?.toString()?.let {
                    if(it.isEmpty()){
                        // 사용자가 모든 텍스트를 지웠을 경우 "0"을 자동으로 입력
                        binding.editTextDetailEditMember.setText("0")
                        binding.editTextDetailEditMember.setSelection(binding.editTextDetailEditMember.text.toString().length) // 커서를 텍스트 끝으로 이동
                    }else{
                        val num = it.toIntOrNull()
                        num?.let { value ->
                            if (value > 30) {
                                binding.editTextDetailEditMember.setText("30")
                            }
                        }
                    }
                }
                binding.editTextDetailEditMember.setSelection(binding.editTextDetailEditMember.text.toString().length) // 커서를 끝으로 이동
            }
        })

        binding.editTextDetailEditMember.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 포커스 해제
                v.clearFocus()

                // 키보드 숨기기
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)

                true // 이벤트 처리 완료
            } else {
                false // 다른 액션 ID의 경우 이벤트 처리 안함
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
                contentUri?.let { uri ->
                    // 사진 객체를 생성한다.
                    val bitmap = BitmapFactory.decodeFile(uri.path) // 사진 객체 생성

                    // 회전 각도값을 구한다.
                    val degree = getDegree(uri)
                    // 회전된 이미지를 구한다.
                    val bitmap2 = rotateBitmap(bitmap, degree.toFloat())
                    // 크기를 조정한 이미지를 구한다.
                    val bitmap3 = resizeBitmap(bitmap2, 1024)

                    binding.cardViewCoverImageSelect.visibility = View.VISIBLE
                    binding.imageViewCoverImageSelect.setImageBitmap(bitmap3)
                }

            }

        }

        // 앨범 실행을 위한 런처
        val contract2 = ActivityResultContracts.StartActivityForResult()
        albumLauncher = registerForActivityResult(contract2) {
            // 사진 선택을 완료한 후 돌아왔다면
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                // 선택한 이미지의 경로 데이터를 관리하는 Uri 객체를 추출한다.
                val uri = it.data?.data
                uri?.let { selectedUri ->
                    contentUri = selectedUri
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(context.contentResolver, selectedUri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        val cursor =
                            context.contentResolver.query(selectedUri, null, null, null, null)
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

                        binding.cardViewCoverImageSelect.visibility = View.VISIBLE
                        binding.imageViewCoverImageSelect.setImageBitmap(bitmap3)
                    }
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
        selectedPlaceName = placeName
        selectedDetailPlaceName = detailPlaceName
        val locationText  = "$selectedPlaceName\n$selectedDetailPlaceName"
        binding.editTextDetailEditTitleLocation.setText(locationText )
    }

    // 툴바 설정
    fun settingToolbar() {
        binding.apply {
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
        // 초기화 전에 기존 칩을 모두 제거하여 ID 충돌 방지
        binding.chipGroupDetailEditType.removeAllViews()
        binding.chipGroupDetailEditPlace.removeAllViews()
        binding.chipGroupDetailEditApply.removeAllViews()

        Log.d("DetailEditFragment", "Setting up study type chips")
        setupChipGroup(
            binding.chipGroupDetailEditType,
            listOf("스터디", "공모전", "프로젝트"),
            mapOf("스터디" to "스터디", "프로젝트" to "프로젝트", "공모전" to "공모전") // tag 값을 지정
        )

        Log.d("DetailEditFragment", "Setting up on/offline chips")
        setupChipGroup(
            binding.chipGroupDetailEditPlace,
            listOf("오프라인", "온라인", "온오프혼합"),
            mapOf("온라인" to "온라인", "오프라인" to "오프라인", "온오프혼합" to "온오프혼합") // tag 값을 지정
        )

        Log.d("DetailEditFragment", "Setting up apply method chips")
        setupChipGroup(
            binding.chipGroupDetailEditApply,
            listOf("신청제", "선착순"),
            mapOf("신청제" to "신청제", "선착순" to "선착순") // tag 값을 지정
        )

        // 장소선택 가시성 설정
        setPlaceSelectionListener()

        // 칩이 생성되고 추가된 후에 preselectChips 호출
        preselectChips()
    }

    // 장소 선택에 따른 UI 가시성 조절
    fun setPlaceSelectionListener() {
        // 반복하며 칩에 클릭 리스너 설정
        binding.chipGroupDetailEditPlace.children.forEach { view ->
            (view as Chip).setOnClickListener {
                Log.d("SelectedChip", "Selected chip: ${view.text}")
                // 칩 클릭시 가시성 업데이트 함수 호출
                updatePlaceVisibility(view)
                // 칩 스타일 업데이트
                updateChipStyles(binding.chipGroupDetailEditPlace, view.id)

            }
        }
    }

    // 가시성 업데이트
    fun updatePlaceVisibility(chip: Chip) {
        when (chip.text.toString()) {
            // '온라인'이 선택된 경우 장소 선택 입력 필드 숨김
            "온라인" -> binding.textInputLayoutDetailEditPlace.visibility = View.GONE
            else -> {
                binding.textInputLayoutDetailEditPlace.visibility = View.VISIBLE

                // currentStudyData에서 studyPlace와 studyDetailPlace 데이터를 가져와 합친다음 editTextDetailEditTitleLocation에 값을 넣기
                val placeName = currentStudyData?.studyPlace
                val detailPlaceName = currentStudyData?.studyDetailPlace
                selectedPlaceName = placeName.toString()
                selectedDetailPlaceName = detailPlaceName.toString()
                val test = "$placeName\n$detailPlaceName"
                binding.editTextDetailEditTitleLocation.setText(test)
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
    fun setupChipGroup(chipGroup: ChipGroup, chipNames: List<String>, chipTags: Map<String, String>? = null) {
        chipGroup.isSingleSelection = true  // Single selection 모드 활성화
        chipGroup.removeAllViews()  // 중복 생성을 방지하기 위해 기존 뷰를 제거

        chipNames.forEachIndexed { index, name ->
            val chip = Chip(context).apply {
                text = name
                id = View.generateViewId()  // 동적으로 ID 생성
                isClickable = true
                isCheckable = true
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                tag = chipTags?.get(name) ?: (index + 1).toString()  // 여기를 수정
            }
            Log.d("DetailEditFragment", "Adding chip: $name with tag: ${chip.tag}")
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
            // 선택된 칩의 tag 출력
            Log.d("DetailEditFragment", "Selected Chip Tag: ${chip.tag}")
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

        Log.d("DetailEditFragment", "Chip style updated for chip '${chip.text}': isSelected = $isSelected")
    }

    override fun onSkillSelected(selectedSkills: List<Skill>) {

        // 새로운 스킬 목록으로 칩그룹 업데이트
        Log.d("DetailEditFragment", "Selected skills: ${selectedSkills.joinToString { it.displayName }}")
        this.selectedSkills = selectedSkills.map { it.num }  // Skill 객체에서 num 값 추출
        addChipsToGroup(binding.ChipGroupDetailEdit, selectedSkills)
    }

    fun addChipsToGroup(chipGroup: ChipGroup, skills: List<Skill>) {
        // 기존의 칩들을 모두 제거
        chipGroup.removeAllViews()
        selectedSkillList.clear()

        var addedOtherChip = false // "기타" 칩이 이미 추가됐는지 여부

        // "기타" 칩들을 하나로 그룹화해서 처리하기 위한 리스트
        val groupedSkills = skills.groupBy { if (it.displayName == "기타") "기타" else it.displayName }

        groupedSkills.forEach { (displayName, skillGroup) ->
            // "기타" 칩은 한 번만 UI에 추가하되, 다른 카테고리의 "기타"도 선택된 상태는 유지
            if (displayName == "기타") {
                if (!addedOtherChip) {
                    addedOtherChip = true // 한 번만 추가
                    addSingleChip(chipGroup, skillGroup.first()) // UI에 "기타" 칩 하나만 추가
                }
            } else {
                addSingleChip(chipGroup, skillGroup.first()) // 기타 외의 칩들은 그대로 추가
            }
            // 선택된 상태는 데이터로 유지
            selectedSkillList.addAll(skillGroup.map { it.num })
        }
    }

    fun addSingleChip(chipGroup: ChipGroup, skill: Skill) {
        val chip = Chip(context).apply {
            text = skill.displayName
            isClickable = true
            isCheckable = true
            isChecked = selectedSkillList.contains(skill.num) // 선택 상태 유지
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setTextAppearance(R.style.ChipTextStyle)
            id = View.generateViewId()
            tag = skill.num

            // 'X' 아이콘 클릭 시 해당 칩을 ChipGroup에서 제거
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                selectedSkillList.remove(skill.num) // 선택된 스킬 목록에서 제거
            }
        }
        chipGroup.addView(chip)
    }

    fun setupBottomSheet() {

        binding.textInputLayoutDetailEditSkill.editText?.setOnClickListener {
            val bottomSheet = SkillBottomSheetFragment().apply {
                setSelectedSkills(selectedSkillList.map { Skill.fromNum(it) }) // 이미 선택된 스킬 설정
                setOnSkillSelectedListener(this@DetailEditFragment) // 리스너 설정
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        // 프래그 먼트간 연결 설정
        binding.textInputLayoutDetailEditPlace.editText?.setOnClickListener {
            val bottomSheet = PlaceBottomSheetFragment().apply {
                setOnPlaceSelectedListener(this@DetailEditFragment)
            }
            bottomSheet.show(childFragmentManager,bottomSheet.tag)

            bottomSheet.dialog?.setOnDismissListener {
                clearAllFocus() // 모든 포커스 제거 함수 호출
            }
        }

    }

    fun clearAllFocus() {
        // 모든 뷰에서 포커스 제거
        activity?.window?.decorView?.clearFocus()
    }

    fun setupButton() {
        binding.buttonDetailEditDone.setOnClickListener {
            if (validateInputs()) {
//                uploadImageAndSaveData()
                if (contentUri != null) {
                    // 이미지를 선택한 경우
                    uploadImageAndSaveData()
//                    parentFragmentManager.popBackStack()
                } else {
                    // 이미지를 선택하지 않은 경우 기존 이미지를 사용
                    saveData(currentStudyData?.studyPic ?: "")
//                    parentFragmentManager.popBackStack()
                }
            }
        }

        // 작성 예시보기 text클릭
        binding.textviewDetailIntroEx.setOnClickListener {
            val dialog = CustomIntroDialog(requireContext())
            dialog.show()
        }

        // 커버 이미지 추가
        binding.imageViewDetailCover.setOnClickListener {
            showPopupWindow(it)
        }
    }

    fun getSelectedStudyType(): String {
        val chipGroup = binding.chipGroupDetailEditType
        val selectedChipId = chipGroup.checkedChipId

        // 선택된 Chip이 없을 경우 처리
        if (selectedChipId == View.NO_ID) {
            Log.d("DetailEditFragment", "No chip selected in chipGroupDetailEditStudyType")
            return ""
        }

        Log.d("DetailEditFragment", "Selected Chip ID: $selectedChipId")

        // Chip 객체를 가져오기
        val selectedChip = binding.root.findViewById<Chip>(selectedChipId)

        // Chip이 null인지 체크
        if (selectedChip == null) {
            Log.e("DetailEditFragment", "Selected chip is null. ID: $selectedChipId")
            return ""
        }

        Log.d("DetailEditFragment", "Selected Chip Tag: ${selectedChip.tag}")

        // 선택된 Chip의 Tag 반환
        return selectedChip.tag as? String ?: ""
    }

    fun getSelectedStudyOnOffline(): String {
        val chipGroup = binding.chipGroupDetailEditPlace
        val selectedChipId = chipGroup.checkedChipId
        // 선택된 Chip이 없을 경우 처리
        if (selectedChipId == View.NO_ID) {
            Log.d("DetailEditFragment", "No chip selected in chipGroupDetailEditPlace")
            return ""
        }

        Log.d("DetailEditFragment", "Selected Chip ID: $selectedChipId")

        // Chip 객체를 가져오기
        val selectedChip = binding.root.findViewById<Chip>(selectedChipId)

        // Chip이 null인지 체크
        if (selectedChip == null) {
            Log.e("DetailEditFragment", "Selected chip is null. ID: $selectedChipId")
            return ""
        }

        Log.d("DetailEditFragment", "Selected Chip Tag: ${selectedChip.tag}")

        // 선택된 Chip의 Tag 반환
        return selectedChip.tag as? String ?: ""
    }

    fun getSelectedStudyApplyMethod(): String {
        val chipGroup = binding.chipGroupDetailEditApply
        val selectedChipId = chipGroup.checkedChipId
        // 선택된 Chip이 없을 경우 처리
        if (selectedChipId == View.NO_ID) {
            Log.d("DetailEditFragment", "No chip selected in chipGroupDetailEditApply")
            return "" // 선택된 칩이 없으면 빈 문자열 반환
        }

        // Chip 객체를 가져오기
        val selectedChip = binding.root.findViewById<Chip>(selectedChipId)

        // Chip이 null인지 체크
        if (selectedChip == null) {
            Log.e("DetailEditFragment", "Selected chip is null. ID: $selectedChipId")
            return "" // Chip이 없을 경우에도 빈 문자열 반환
        }

        Log.d("DetailEditFragment", "Selected Chip Tag: ${selectedChip.tag}")

        // 선택된 Chip의 Tag 반환
        return selectedChip.tag as? String ?: ""
    }

    fun uploadImageAndSaveData() {
        contentUri?.let { selectedImageUri ->
            try {
                val fileName = "${System.currentTimeMillis()}.jpg"
                val storageReference = FirebaseStorage.getInstance().reference.child("studyPic/$fileName")
                storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                            saveData(fileName)  // 이미지 URL 대신 파일 이름을 저장하도록 변경
                        }
                    }
                    .addOnFailureListener {
                        Log.e("DetailEditFragment", "Image upload failed: ${it.message}")
                    }
            } catch (e: Exception) {
                Log.e("DetailEditFragment", "Error uploading image: ${e.message}")
            }
        } ?: run {
            saveData("")
            Log.e("DetailEditFragment", "Content URI is not initialized, saving without image")
        }
    }

    fun saveData(imageFileName: String) {
        if (!validateInputs()) {
            Snackbar.make(binding.root, "입력이 유효하지 않습니다.", Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        val studyType = getSelectedStudyType()
        val studyOnOffline = getSelectedStudyOnOffline()
        val studyApplyMethod = getSelectedStudyApplyMethod()
//        val studySkills = if (selectedSkills.isNotEmpty()) selectedSkills else currentStudyData?.studySkillList ?: listOf()

        // EditText로부터 텍스트를 가져와 줄바꿈 문자를 \n으로 변환
        val studyContent = binding.editTextDetailEditContext.text.toString().replace(System.getProperty("line.separator"), "\\n")

        val placeName = if (studyOnOffline == "온라인") "" else selectedPlaceName
        val detailPlaceName = if (studyOnOffline == "온라인") "" else selectedDetailPlaceName

        val updatedStudyData = StudyData(
            studyIdx = studyIdx,
            studyTitle = binding.editTextDetailEditTitle.text.toString(),
            studyContent = studyContent,
            studyType = studyType,
            studyPeriod = currentStudyData?.studyPeriod ?: "",
            studyOnOffline = studyOnOffline,
            studyPlace = placeName,
            studyDetailPlace = detailPlaceName,
            studyApplyMethod = studyApplyMethod,
            studyCanApply = currentStudyData?.studyCanApply ?: "",
            studyPic = imageFileName,
            studyMaxMember = binding.editTextDetailEditMember.text.toString().toInt(),
            studyState = currentStudyData?.studyState ?: true,
            userIdx = currentStudyData?.userIdx ?: -1
        )

        viewModel.updateStudyData(updatedStudyData)
        viewModel.insertSkills(studyIdx, selectedSkillList)
        Log.d("DetailEditFragment", "Updating study data: $updatedStudyData")

    }


    // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 입력 유효성 검사
    fun validateInputs(): Boolean {
        val title = binding.editTextDetailEditTitle.text.toString()
        val description = binding.editTextDetailEditContext.text.toString()

        val memberInput = binding.editTextDetailEditMember.text.toString()
        val memberCount = memberInput.toIntOrNull() ?: 0

        // 최소 인원 수 설정
//        val minMembers = currentStudyData?.studyUidList?.size.toString().toInt()


        val studyOnOffline = getSelectedStudyOnOffline()  // 현재 온라인/오프라인 상태 가져오기
        val placeName = selectedPlaceName
        val detailPlaceName = selectedDetailPlaceName


        // 제목이 비어있거나 너무 짧은 경우 검사
        if (title.isEmpty() || title.length < 8) {
            binding.textInputLayoutDetailEditTitle.error = "제목은 최소 8자 이상이어야 합니다."
            return false
        } else {
            binding.textInputLayoutDetailEditTitle.error = null
        }

        // 소개글이 비어있거나 너무 짧은 경우 검사
        if (description.isEmpty() || description.length < 10) {
            binding.textInputLayoutDetailEditContext.error =
                "소개글은 최소 10자 이상이어야 합니다."
            return false
        } else {
            binding.textInputLayoutDetailEditContext.error = null
        }

        // 온라인이 아닌 경우 위치 유효성 검사
        if (studyOnOffline != "온라인") {
            if (placeName.isEmpty() || detailPlaceName.isEmpty()) {
                binding.editTextDetailEditTitleLocation.error = "장소와 상세 장소를 입력해야 합니다."
                return false
            } else {
                binding.editTextDetailEditTitleLocation.error = null
            }
        }
        // 인원수가 최소 인원보다 작은 경우 검사
        if (memberCount < minMembers) {
            binding.textInputLayoutDetailEditMember.error = "최소 인원은 $minMembers 명 이상이어야 합니다."
            return false
        } else {
            binding.textInputLayoutDetailEditMember.error = null
        }


        return true
    }

    // 사용자가 이전에 선택한 내용 선택
    fun preselectChips() {
        // 스터디 타입 칩 선택
        val studyTypeTag = currentStudyData?.studyType ?: "" // 기본값 설정
        Log.d("DetailEditFragment", "studyTypeTag: $studyTypeTag")
        val studyTypeChip = findChipByText(binding.chipGroupDetailEditType, studyTypeTag)

        studyTypeChip?.let {
            it.isChecked = true
            updateChipStyle(it, true)
            Log.d("DetailEditFragment", "Study type chip selected: ${it.text}")
        } ?: Log.d("DetailEditFragment", "No matching chip found for studyTypeTag: $studyTypeTag")

        // 온오프라인 타입 칩 선택
        val onOfflineTag = currentStudyData?.studyOnOffline ?: "" // 기본값 설정
        val onOfflineChip = findChipByText(binding.chipGroupDetailEditPlace, onOfflineTag)

        onOfflineChip?.let {
            it.isChecked = true
            updateChipStyle(it, true)
            updatePlaceVisibility(it) // UI 가시성 설정
            Log.d("DetailEditFragment", "On/Offline chip selected: ${it.text}")
        } ?: Log.d("DetailEditFragment", "No matching chip found for onOfflineTag: $onOfflineTag")

        // 신청 방법 칩 선택
        val applyMethodTag = currentStudyData?.studyApplyMethod ?: "" // 기본값 설정
        val applyMethodChip = findChipByText(binding.chipGroupDetailEditApply, applyMethodTag)

        applyMethodChip?.let {
            it.isChecked = true
            updateChipStyle(it, true)
            Log.d("DetailEditFragment", "Apply method chip selected: ${it.text}")
        } ?: Log.d("DetailEditFragment", "No matching chip found for applyMethodTag: $applyMethodTag")
    }


    // 특정 텍스트를 가진 칩을 찾는 함수
    fun findChipByText(chipGroup: ChipGroup, tag: String): Chip? {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip?.tag?.toString() == tag) {
                return chip
            }
        }
        return null
    }

}