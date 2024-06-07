package kr.co.lion.modigm.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailBinding
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.Skill

class DetailFragment : Fragment() {

    lateinit var binding: FragmentDetailBinding

    private val viewModel: DetailViewModel by activityViewModels()

    private var isPopupShown = false

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    private var currentStudyData: StudyData? = null
    private var currentUserData: UserData? = null

    // 프래그먼트의 뷰가 생성될 때 호출
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        return binding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 앱바 스크롤
        setupAppBarScrollListener()

        // 좋아요 버튼
        setupLikeButtonListener()

        // 메뉴 설정
        setupPopupMenu()

        // ViewModel에서 데이터 요청
        viewModel.selectContentData(studyIdx)

        viewModel.contentData.observe(viewLifecycleOwner) {
            updateUI(StudyData())
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.selectContentData(studyIdx)  // 프래그먼트로 돌아올 때 데이터를 다시 로드합니다.
        observeViewModel()
    }


    fun observeViewModel() {

        viewModel.contentData.observe(viewLifecycleOwner) { data ->
            data?.let {
                currentStudyData = it // 여기서 데이터를 업데이트합니다.
                updateUIIfReady() // UI 업데이트 체크

                // 스터디 데이터가 로드되면 연관된 사용자 데이터 로드
                viewModel.loadUserDetailsByUid(it.studyWriteUid)
                Log.d("DetailWriteUid","writeUid = ${it.studyWriteUid}")
            }
        }
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            userData?.let {
                currentUserData = it // 여기서 사용자 데이터를 업데이트합니다.
                updateUIIfReady() // UI 업데이트 체크
                updateUserDetails(it)
            }
        }
    }

    fun updateUIIfReady() {
        val studyData = currentStudyData
        val userData = currentUserData
        if (studyData != null && userData != null) {
            updateUI(studyData)
        }
    }

    fun updateUI(data: StudyData) {

        with(binding) {
            //툴바 설정 함수 호출
            settingToolbar(data)

            // 스터디 제목
            textviewDetailTitle.text = data.studyTitle

            // studyWriteUid와 사용자의 uid를 비교하여 이미지 아이콘 설정
            if (data.studyWriteUid == uid) {
                // 기본 아이콘 유지
                imageViewDetailMenu.setImageResource(R.drawable.icon_settings_24px)
            } else {
                // studyWriteUid와 사용자의 uid가 다른 경우: 아이콘을 icon_more_vert_24px로 변경
                imageViewDetailMenu.setImageResource(R.drawable.icon_more_vert_24px)
            }

            // 스터디 소개글(줄바꿈 추가)
            textViewDetailIntro.text =
                data.studyContent.replace("\\n", System.getProperty("line.separator"))

            // 칩 (필요 스킬 목록)
            val chipGroup = chipGroupJoinInterest
            chipGroup.removeAllViews()  // 기존에 추가된 칩을 모두 제거
            data.studySkillList.forEach { skillId ->
                val chip = createSkillChip(skillId)
                chipGroup.addView(chip)
            }

            val studyType = when (data.studyType) {
                1 -> "스터디"
                2 -> "프로젝트"
                else -> "공모전"
            }

            val studyTypeImage = when (studyType) {
                "스터디" -> R.drawable.icon_closed_book_24px
                "공모전" -> R.drawable.icon_trophy_24px
                else -> R.drawable.icon_code_box_24px
            }

            // 스터디 종류에 따라 이미지 변경
            imageViewDetailStudyType.setImageResource(studyTypeImage)

            // 스터디 종류
            textViewDetailStudyType.text = studyType

            // 스터디 인원(총인원)
            textViewDetailMemberTotal.text = data.studyMaxMember.toString()

            // 스터디 인원 (참가 인원)
            textViewDetailMember.text = data.studyUidList.size.toString()


            // 스터디 방식(온라인 / 오프라인 / 온오프 -> 온라인 제외하고는 주소 이름으로 사용)
            val studyOnOffline = when (data.studyOnOffline) {
                1 -> "온라인"
                2 -> "오프라인"
                else -> "온·오프혼합"
            }

            if (studyOnOffline == "온라인") {
                textviewDetailFragmentPlace.text = "온라인"
                textviewDetailFragmentDetailPlace.visibility =
                    View.GONE  // 상세 주소 필드를 숨깁니다
            } else {
                // 오프라인이나 온·오프 혼합인 경우
                textviewDetailFragmentPlace.text = data.studyPlace  // 주소 설정
                textviewDetailFragmentDetailPlace.text = "${data.studyDetailPlace}"  // 상세 주소 설정
                textviewDetailFragmentDetailPlace.visibility = View.VISIBLE  // 상세 주소 필드를 보여줍니다
            }


            // 신청방식(신청제 / 선착순)
            val applyMethod = when (data.studyApplyMethod) {
                1 -> "신청제"
                else -> "선착순"
            }
            textViewDetailApplyMethod.text = "($applyMethod)"

            // uid와 studyWriteUid 비교 버튼 text 변경
            if (data.studyWriteUid == uid) {
                buttonDetailApply.text = "채팅방 입장하기"
            } else {
                if (data.studyApplyMethod == 1) {
                    buttonDetailApply.text = "신청하기"
                } else {
                    buttonDetailApply.text = "참여하기"
                }
            }

            // 모집 상태에 따라 textViewDetailState의 텍스트 설정
            if (data.studyCanApply == true) {
//                Log.d("DetailFragment", "User Profile Pic URL: ${data.studyCanApply}")
                // 모집중 상태
                textViewDetailState.text = "모집중"
                setupStatePopup()
            } else {
                // 모집 마감 상태
                textViewDetailState.text = "모집 마감"
                setupStatePopup()
            }

            if (!data.studyPic.isNullOrEmpty()) {
                val storageReference: StorageReference =
                    FirebaseStorage.getInstance().reference.child("studyPic/${data.studyPic}")

                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    // viewLifecycleOwner를 사용하여 Glide가 프래그먼트의 뷰 생명주기에 따라 작동하도록 함
                    Glide.with(this@DetailFragment)
                        .load(uri)
                        .error(R.drawable.icon_error_24px)  // 에러 이미지 추가
                        .into(imageViewDetailCover)
                }.addOnFailureListener {
                    Log.e("DetailFragment", "Storage에서 이미지 로드 실패: ${it.message}")
                }
            }

        }
    }

    fun updateUserDetails(userData: UserData) {
        with(binding) {
            textViewDetailUserName.text = userData.userName

            // userProfilePic 값을 로그로 출력
            Log.d("DetailFragment", "User Profile Pic URL: ${userData.userProfilePic}")

            if (userData.userProfilePic.isNullOrEmpty()) {
                imageViewDetailUserPic.setImageResource(R.drawable.icon_account_circle)
            } else {
                // Firebase Storage에서 이미지 URL 가져오기
                val storageReference: StorageReference =
                    FirebaseStorage.getInstance().reference.child("userProfile/${userData.userProfilePic}")

                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(requireContext())
                        .load(uri)
                        .error(R.drawable.icon_account_circle) // 오류 발생 시 표시할 대체 이미지
                        .into(imageViewDetailUserPic)
                }.addOnFailureListener {
                    imageViewDetailUserPic.setImageResource(R.drawable.icon_account_circle)
                }
            }
        }
    }


    // 칩 생성 함수
    fun createSkillChip(skillId: Int): Chip {
        val chip = Chip(context)
        val skill = Skill.fromNum(skillId)
        chip.text = skill.displayName
        chip.isClickable = true
        chip.isCheckable = false
        chip.setTextAppearance(R.style.ChipTextStyle)
        chip.setChipBackgroundColorResource(R.color.dividerView)
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        return chip
    }


    // 툴바 설정
    fun settingToolbar(data: StudyData) {
        with(binding) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // 콜랩싱 툴바의 타이틀 설정
            collapsingToolbarDetail.title = data.studyTitle

            // 뒤로 가기
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    // 앱바 스크롤 설정
    fun setupAppBarScrollListener() {
        with(binding) {
            appBarDetail.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                // 전체 스크롤 범위를 계산
                val scrollRange = appBarLayout.totalScrollRange
                // 뒤로가기 아이콘
                val drawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_back_24px)
                // 스크롤 최대일 때 아이콘 색상 변경
                if (scrollRange + verticalOffset == 0) {
                    drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.black))
                } else {
                    drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
                }
                // 네비게이션 아이콘 업데이트
                toolbar.navigationIcon = drawable
            })
        }
    }

    // 좋아요 버튼 설정
    fun setupLikeButtonListener() {
        binding.buttonDetailLike.setOnClickListener {
            toggleLikeButton()
        }
    }

    // 팝업 메뉴 설정
    fun setupPopupMenu() {
        binding.imageViewDetailMenu.setOnClickListener {
            showPopupWindow(it)
        }
    }

    // 좋아요 버튼 상태 토글
    fun toggleLikeButton() {
        // 현재 설정된 이미지 리소스 ID를 확인하고 상태를 토글
        val currentIconResId =
            binding.buttonDetailLike.tag as? Int ?: R.drawable.icon_favorite_24px
        if (currentIconResId == R.drawable.icon_favorite_24px) {
            // 좋아요 채워진 아이콘으로 변경
            binding.buttonDetailLike.setImageResource(R.drawable.icon_favorite_full_24px)
            // 상태 태그 업데이트
            binding.buttonDetailLike.tag = R.drawable.icon_favorite_full_24px

            // 새 색상을 사용하여 틴트 적용
            binding.buttonDetailLike.setColorFilter(Color.parseColor("#D73333"))
        } else {
            // 기본 아이콘으로 변경
            binding.buttonDetailLike.setImageResource(R.drawable.icon_favorite_24px)
            // 상태 태그 업데이트
            binding.buttonDetailLike.tag = R.drawable.icon_favorite_24px

            // 틴트 제거 (원래 아이콘 색상으로 복원)
            binding.buttonDetailLike.clearColorFilter()
        }
    }

    // custom 팝업 메뉴
    fun showPopupWindow(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupView = layoutInflater.inflate(R.layout.custom_detail_popup_menu_item, null)

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
        // 멤버목록
        // studyWriteUid와 사용자의 uid 비교
        if (currentStudyData?.studyWriteUid == uid) {
            // studyWriteUid와 사용자의 uid가 같은 경우
            // 각 메뉴 아이템에 대한 클릭 리스너 설정
            // 멤버목록
            popupView.findViewById<TextView>(R.id.menuItem1).setOnClickListener {
                // 화면이동 로직 추가
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, DetailMemberFragment())
                    .addToBackStack(FragmentName.DETAIL_MEMBER.str)
                    .commit()

                popupWindow.dismiss()
            }

            // 글 편집
            popupView.findViewById<TextView>(R.id.menuItem2).setOnClickListener {
                // DetailEditFragment의 인스턴스를 생성하고 번들을 통해 studyIdx를 전달
                val detailEditFragment = DetailEditFragment().apply {
                    arguments = Bundle().apply {
                        putInt("studyIdx", currentStudyData?.studyIdx?:0)
                    }
                }

                // 화면이동 로직 추가
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, detailEditFragment)
                    .addToBackStack(FragmentName.DETAIL_EDIT.str)
                    .commit()
                popupWindow.dismiss()
            }

            // 글 삭제
            popupView.findViewById<TextView>(R.id.menuItem3).setOnClickListener {
                // 삭제 확인 다이얼로그
                showDeleteDialog()
                popupWindow.dismiss()
            }
        } else {
            // studyWriteUid와 사용자의 uid가 다른 경우
            // 멤버목록 숨김
            popupView.findViewById<LinearLayout>(R.id.layoutDetailMember)?.visibility = View.GONE

            // 글 편집 숨김
            popupView.findViewById<LinearLayout>(R.id.layoutDetailEdit)?.visibility = View.GONE


            // 글 삭제 숨김
            popupView.findViewById<LinearLayout>(R.id.layoutDetailDel)?.visibility = View.GONE

            // 신고하기 보여주기
            popupView.findViewById<LinearLayout>(R.id.layoutDetailReport)?.visibility = View.VISIBLE
            popupView.findViewById<TextView>(R.id.menuItem4).setOnClickListener {
                // 신고하기 기능
                popupWindow.dismiss()
            }
        }

        // 팝업 윈도우 표시
        popupWindow.showAsDropDown(anchorView)
    }

    // custom dialog
    fun showDeleteDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.dialogColor)
            .setTitle("삭제 확인")
            .setMessage("정말로 글을 삭제하시겠습니까?")
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.btnYes).setOnClickListener {
            // 예 버튼 로직
            Log.d("Dialog", "확인을 선택했습니다.")
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnNo).setOnClickListener {
            // 아니요 버튼 로직
            Log.d("Dialog", "취소를 선택했습니다.")
            dialog.dismiss()
        }

        dialog.show()
    }

    fun setupStatePopup() {
        val textViewState = binding.textViewDetailState

        // 사용자 ID와 studyWriteUid를 비교하여 이미지를 설정합니다.
        if (currentStudyData?.studyWriteUid == uid) {
            textViewState.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.icon_expand_more_24px,
                0
            )

            // 팝업 메뉴
            textViewState.setOnClickListener { view ->
                // 사용자 ID와 studyWriteUid 비교하여 팝업 메뉴 표시 여부 결정
                if (!isPopupShown) {
                    showStatePopup(view as TextView)  // TextView를 명시적으로 전달
                    textViewState.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.icon_expand_less_24px,
                        0
                    )
                    isPopupShown = true
                } else {
                    isPopupShown = false
                    textViewState.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.icon_expand_more_24px,
                        0
                    )
                }
            }

            // 버튼 클릭 이벤트(채팅 방 이동)
            binding.buttonDetailApply.setOnClickListener {
                Log.d("DetailFragment", "채팅방 이동1")
            }

        } else {
            textViewState.isEnabled = false
            textViewState.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )

            // textViewState의 text가 "모집중"인 경우 버튼 파란색으로 설정
            if (textViewState.text == "모집중") {
                binding.buttonDetailApply.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pointColor))
                // 버튼 클릭 이벤트(채팅 방 이동 혹은 신청)
                if (currentStudyData?.studyApplyMethod==1) {
                    binding.buttonDetailApply.setOnClickListener {
                        Log.d("DetailFragment", "신청")
                    }
                }else{
                    // 선착순일 경우
                    binding.buttonDetailApply.setOnClickListener {
                        Log.d("DetailFragment", "채팅방 이동2")
                    }
                }

            } else {
                // textViewState의 text가 "모집 마감"인 경우 버튼 회색으로 설정
                val button = binding.buttonDetailApply
                button.setBackgroundColor(Color.parseColor("#777777"))  // 배경색을 회색으로 설정
                button.setTextColor(Color.BLACK)  // 텍스트 색상을 검정색으로 설정

                button.isEnabled = false
            }
        }
    }

    fun showStatePopup(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupView = layoutInflater.inflate(R.layout.custom_detail_popup_menu_stateitem, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true

        // 팝업 뷰를 측정하여 실제 높이를 계산
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val x = location[0]
        val density = resources.displayMetrics.density
        val extraOffset = (10 * density).toInt()
        val y = location[1] - popupHeight - extraOffset

        popupWindow.setOnDismissListener {
            // 팝업이 닫힐 때 이미지를 원래대로 복구
            isPopupShown = false
            binding.textViewDetailState.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.icon_expand_more_24px,
                0
            )
        }

        // 팝업 윈도우 표시
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)

        // 팝업 메뉴 아이템의 클릭 리스너 설정
        popupView.findViewById<TextView>(R.id.textViewDetailState1).setOnClickListener {
            binding.textViewDetailState.text = (it as TextView).text
            viewModel.updateStudyCanApplyByStudyIdx(studyIdx, true)  // 모집중 상태로 업데이트
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.textViewDetailState2).setOnClickListener {
            binding.textViewDetailState.text = (it as TextView).text
            viewModel.updateStudyCanApplyByStudyIdx(studyIdx, false)  // 모집중 상태로 업데이트
            popupWindow.dismiss()
        }
    }
}