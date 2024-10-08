package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailBinding
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.Links
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.Skill
import kr.co.lion.modigm.util.openWebView
import kotlinx.coroutines.flow.collect
import java.util.UUID

class DetailFragment : VBBaseFragment<FragmentDetailBinding>(FragmentDetailBinding::inflate) {

    // 뷰 모델
    private val viewModel: DetailViewModel by activityViewModels()

    private var isPopupShown = false

    private var snackbar: Snackbar? = null

    // 현재 선택된 스터디 idx 번호를 담을 변수
    var studyIdx = 0
    var userIdx = 1

    private var currentStudyData: StudyData? = null
    private var currentUserData: UserData? = null

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx") ?: 0

        userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)

        // 기본 이미지로 초기화
        binding.imageViewDetailUserPic.setImageResource(R.drawable.image_default_profile)

        // 처음에는 글 작성자 이름을 숨김
        binding.textViewDetailUserName.visibility = View.GONE

        viewModel.clearData() // ViewModel 데이터 초기화

        // ProgressBar 제어
        lifecycleScope.launch {
            viewModel.isDataFullyLoaded
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isLoaded ->
                    if (isLoaded) {
                        binding.progressBar.visibility = View.GONE
                        binding.coordinatorLayoutDetail.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.coordinatorLayoutDetail.visibility = View.GONE
                    }
                }
        }

        // ViewModel 상태 초기화 및 데이터 로드
        viewModel.clearLoadingState()  // 새로운 항목 선택 시 상태 초기화

        // 스터디 데이터 및 기타 데이터 로드
        viewModel.loadStudyData(studyIdx)

        // 초기 데이터 로드
        viewModel.fetchUserProfile(userIdx)

        // 앱바 스크롤
        setupAppBarScrollListener()

        // 좋아요 버튼
        setupLikeButtonListener()

        // 메뉴 설정
        setupPopupMenu()

        // 데이터 요청 및 UI 업데이트
        fetchDataAndUpdateUI()

        userprofile()

        observeViewModel()

        // 초기 좋아요 상태 확인
        viewModel.checkIfLiked(userIdx, studyIdx)

        // 이미지 클릭 리스너 설정
        binding.imageViewDetailCover.setOnClickListener {
            showImageZoomDialog()
        }

//        observeApplyButton()

    }

    // 확대된 이미지를 보여주는 Dialog
    private fun showImageZoomDialog() {
        // Dialog 생성
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_detail_image_zoom, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).create()
        dialog.setView(dialogView)

        // ImageView 참조
        val imageViewZoomed = dialogView.findViewById<ImageView>(R.id.imageViewZoomed)
        val imageViewClose = dialogView.findViewById<ImageView>(R.id.imageViewClose)

        // 이미지를 Glide로 로드하여 ImageView에 설정
        currentStudyData?.let { data ->
            Glide.with(this)
                .load(data.studyPic) // 확대할 이미지 URL
                .apply(RequestOptions()
                    .placeholder(R.drawable.image_loading_gray)
                    .error(R.drawable.icon_error_24px)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(1600, 1200)) // 이미지 크기 조정 (선택 사항)
                .into(imageViewZoomed)
        }

        // "X" 버튼 클릭 시 Dialog 닫기
        imageViewClose.setOnClickListener {
            dialog.dismiss()
        }

        // Dialog를 보여줍니다.
        dialog.show()
    }


    fun fetchDataAndUpdateUI() {
        lifecycleScope.launch {
            // 동시에 데이터 로드
            val dataDeferred = async { viewModel.getStudy(studyIdx) }
            val membersDeferred = async { viewModel.countMembersByStudyIdx(studyIdx) }
            val techDeferred = async { viewModel.getTechIdxByStudyIdx(studyIdx) }
            val imageDeferred = async { loadImage() }

            // 모든 데이터 로드 완료까지 대기
            awaitAll(dataDeferred, membersDeferred, techDeferred, imageDeferred)
        }

    }
    private suspend fun loadImage() {
        viewModel.studyPic.collect { imageUrl ->
            if (isViewActive()) {
                imageUrl?.let {
                    withContext(Dispatchers.Main) {
                        Glide.with(this@DetailFragment)
                            .load(it)
                            .apply(
                                RequestOptions()
                                    .format(DecodeFormat.PREFER_RGB_565)
                                    .placeholder(R.drawable.image_loading_gray)
                                    .error(R.drawable.icon_error_24px)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(800, 600)
                            )
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(binding.imageViewDetailCover)
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        // ViewModel의 로딩 상태 초기화
//        viewModel.clearLoadingState()

        // 이미 데이터가 로드된 상태라면 로딩을 다시 하지 않음
        if (!viewModel.isDataFullyLoaded.value) {
            viewModel.loadStudyData(studyIdx)
        }

        fetchDataAndUpdateUI()
    }

    private fun observeViewModel() {
        // 스터디 데이터
        lifecycleScope.launch {
            viewModel.studyData.flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).collect { data ->
                data?.let {
                    currentStudyData = it
                    // 스터디 데이터를 수신한 후 사용자 데이터를 요청
                    viewModel.getUserById(it.userIdx)
                    // 데이터가 로드된 후에 버튼 상태를 설정
                    setupApplyButton()

                    updateUI(it)

                    // 링크 데이터 설정
                    val link = it.studyChatLink ?: ""
                    binding.textviewDetailFragmentLink.text = link

                    // 링크에 따른 아이콘 업데이트
                    updateLinkIcon(link)

                    // 링크 클릭 시 웹뷰로 열기
                    binding.textviewDetailFragmentLink.setOnClickListener {
                        openWebView(viewLifecycleOwner, parentFragmentManager, link)
                    }
                }
            }
        }

        // 스터디 멤버 수
        lifecycleScope.launch {
            viewModel.memberCount.flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).collect { count ->
                Log.d("DetailFragment", "Member count: $count")
                binding.textViewDetailMember.text = count.toString()
            }
        }

        // 글 작성자 정보
        lifecycleScope.launch {
            viewModel.userData.flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).collect { user ->
                if (user != null) {
                    // 작성자 데이터가 있으면 이름을 보여줌
                    binding.textViewDetailUserName.visibility = View.VISIBLE
                    // 데이터가 있을 경우 UI 업데이트
                    binding.textViewDetailUserName.text = user.userName
                    loadUserImage(user.userProfilePic)

                } else {
                    // 데이터가 없을 경우 기본 설정
                    binding.textViewDetailUserName.visibility = View.GONE // 데이터가 없을 때는 이름을 숨김
                    Glide.with(this@DetailFragment)
                        .load(R.drawable.image_default_profile)
                        .into(binding.imageViewDetailUserPic)
                }
            }
        }

        // 스터디 스킬
        lifecycleScope.launch {
            viewModel.studyTechList.flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).collect { techList ->
                updateTechChips(techList)
            }
        }

        //addUserResult를 관찰하여 UI를 업데이트
        lifecycleScope.launch {
            viewModel.addUserResult.flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).collect { success ->
                if (success) {
                    showSnackbar(requireView(), "성공적으로 신청되었습니다.")
                } else {
                    showSnackbar(requireView(), "신청에 실패하였습니다.")
                }
            }
        }

        // 새로운 코드 추가: 알림 성공 여부를 관찰
        lifecycleScope.launch {
            viewModel.notificationResult.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { result ->
                if (result) {
                    Log.d("DetailFragment", "Notification sent successfully.")
                } else {
                    Log.e("DetailFragment", "Failed to send notification.")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLiked.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { isLiked ->
                updateLikeButton(isLiked)
            }
        }

    }

    fun updateLinkIcon(link: String) {
        if (link.isEmpty()) {
            // 링크가 없으면 아이콘과 텍스트뷰 모두 숨김
            binding.imageViewDetailLink.visibility = View.GONE
            binding.textviewDetailFragmentLink.visibility = View.GONE
        } else {
            val iconResource = when {
                link.contains("kakao", ignoreCase = true) -> R.drawable.kakaotalk_sharing_btn_small
                link.contains("google.com/forms", ignoreCase = true) -> R.drawable.icon_googleforms
                else -> R.drawable.icon_link
            }

            // 이미지 아이콘 설정
            binding.imageViewDetailLink.setImageResource(iconResource)
            binding.imageViewDetailLink.visibility = View.VISIBLE
            binding.textviewDetailFragmentLink.visibility = View.VISIBLE
        }
    }


    private fun updateLikeButton(isLiked: Boolean) {
        if (isLiked) {
            binding.buttonDetailLike.setImageResource(R.drawable.icon_favorite_full_24px)
            binding.buttonDetailLike.setColorFilter(Color.parseColor("#D73333"))
        } else {
            binding.buttonDetailLike.setImageResource(R.drawable.icon_favorite_24px)
            binding.buttonDetailLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.pointColor))
        }
    }

    private fun loadUserImage(imageUrl: String?) {
        if (isViewActive()) {
            // 이미지 뷰 초기화 (이전 이미지를 지워줍니다)
            Glide.with(this)
                .clear(binding.imageViewDetailUserPic)

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.image_loading_gray)
                            .error(R.drawable.image_default_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                    )
                    .into(binding.imageViewDetailUserPic)
                    .clearOnDetach()
            } else {
                binding.imageViewDetailUserPic.setImageResource(R.drawable.image_default_profile)
                binding.imageViewDetailUserPic.invalidate()
                binding.imageViewDetailUserPic.requestLayout()
            }
        }
    }

    private fun isViewActive(): Boolean {
        return view != null && isAdded && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    fun userprofile(){
        binding.imageViewDetailUserPic.setOnClickListener {
            val profileFragment = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putInt("userIdx", currentStudyData?.userIdx.toString().toInt())
                    putBoolean("isBottomNavi", false)
                }
            }

            // 화면이동 로직 추가
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out) // 애니메이션 추가
                .replace(R.id.containerMain, profileFragment)
                .addToBackStack(FragmentName.DETAIL_MEMBER.str)
                .commit()
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
            if (data.userIdx == userIdx) {
                // 기본 아이콘 유지
                imageViewDetailMenu.setImageResource(R.drawable.icon_settings_24px)
            } else {
                // studyWriteUid와 사용자의 uid가 다른 경우: 아이콘을 icon_more_vert_24px로 변경
                imageViewDetailMenu.setImageResource(R.drawable.icon_more_vert_24px)
            }

            // 스터디 소개글(줄바꿈 추가)
            textViewDetailIntro.text =
                data.studyContent.replace("\\n", System.getProperty("line.separator"))

            val studyType = when (data.studyType) {
                "스터디" -> "스터디"
                "프로젝트" -> "프로젝트"
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


            // 스터디 방식(온라인 / 오프라인 / 온오프 -> 온라인 제외하고는 주소 이름으로 사용)
            val studyOnOffline = when (data.studyOnOffline) {
                "온라인" -> "온라인"
                "오프라인" -> "오프라인"
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
                "신청제" -> "신청제"
                else -> "선착순"
            }
            textViewDetailApplyMethod.text = "($applyMethod)"

            // 모집 상태에 따라 textViewDetailState의 텍스트 설정
            if (data.studyCanApply == "모집중") {
                // 모집중 상태
                textViewDetailState.text = "모집중"
                setupStatePopup()
            } else {
                // 모집 마감 상태
                textViewDetailState.text = "모집 마감"
                setupStatePopup()
            }

            // 링크 데이터 반영 및 클릭 리스너 추가
            if (data.studyChatLink.isNullOrEmpty()) {
                textviewDetailFragmentLink.text = "링크 없음"
            } else {
                val link = data.studyChatLink
                textviewDetailFragmentLink.text = link

                // 링크에 따라 아이콘 설정
                updateLinkIcon(link)

                // 링크 클릭 시 웹뷰에서 열기
                textviewDetailFragmentLink.setOnClickListener {
                    openWebView(viewLifecycleOwner, parentFragmentManager, link)
                }
            }
        }
    }

    // 칩 업데이트 함수
    fun updateTechChips(techList: List<Int>) {
        val chipGroup = binding.chipGroupJoinInterest
        chipGroup.removeAllViews()

        techList.forEach { techId ->
            val chip = createSkillChip(techId)
            chipGroup.addView(chip)
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

            // 커스텀 네비게이션 아이콘 설정
            toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.custom_back_icon)


            // 콜랩싱 툴바의 타이틀 설정
            collapsingToolbarDetail.title = data.studyTitle

            // 뒤로 가기
            toolbar.setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str, 0)
            }
        }
    }

    fun setupAppBarScrollListener() {
        with(binding) {
            appBarDetail.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if (!isAdded) return@OnOffsetChangedListener // 프래그먼트가 액티비티에 추가되지 않았으면 반환

                val context = context ?: return@OnOffsetChangedListener // context가 null이면 반환

                val scrollRange = appBarLayout.totalScrollRange
                if (scrollRange + verticalOffset == 0) {
                    // 스크롤이 완료된 상태 - 기본 아이콘 사용
                    toolbar.navigationIcon = ContextCompat.getDrawable(context, R.drawable.icon_arrow_back_24px)
                } else {
                    // 스크롤이 진행 중이거나 완료되지 않은 상태 - 커스텀 아이콘 사용
                    toolbar.navigationIcon = ContextCompat.getDrawable(context, R.drawable.custom_back_icon)
                }
            })
        }
    }

    // 좋아요 버튼 설정
    fun setupLikeButtonListener() {
        binding.buttonDetailLike.setOnClickListener {
            viewModel.toggleFavoriteStatus(userIdx, studyIdx)
        }
    }

    // 팝업 메뉴 설정
    fun setupPopupMenu() {
        binding.imageViewDetailMenu.setOnClickListener {
            showPopupWindow(it)
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
        if (currentStudyData?.userIdx == userIdx) {
            // studyWriteUid와 사용자의 uid가 같은 경우
            // 각 메뉴 아이템에 대한 클릭 리스너 설정
            // 멤버목록
            popupView.findViewById<TextView>(R.id.menuItem1).setOnClickListener {
                val detailMemberFragment = DetailMemberFragment().apply {
                    arguments = Bundle().apply {
                        putInt("studyIdx", currentStudyData?.studyIdx?:0)
                        putString("studyTitle",currentStudyData?.studyTitle?:"") // study title 전달
                    }
                }

                // 화면이동 로직 추가
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out) // 애니메이션 추가
                    .replace(R.id.containerMain, detailMemberFragment)
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
                    .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out) // 애니메이션 추가
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
                // 고객센터 구글폼 띄우기로 수정 by ms.
                val link = StringBuilder(Links.STUDY_SERVICE.url)
                // 신고 스터디 idx
                link.append("&entry.1666022374=${UUID.randomUUID().toString().split("-")[4]}#${studyIdx}")
                // 신고자 idx
                link.append("&entry.218705015=${UUID.randomUUID().toString().split("-")[4]}#${viewModel.userData.value?.userIdx}")

                openWebView(viewLifecycleOwner, parentFragmentManager, link.toString())
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
            viewModel.updateStudyState(studyIdx, false)  // studyState 값을 false로 업데이트
            // 예 버튼 로직
            Log.d("Dialog", "확인을 선택했습니다.")
            dialog.dismiss()
            parentFragmentManager.popBackStack()
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

        if (currentStudyData?.userIdx == userIdx) {
            setupOwnerView(textViewState)
            // "멤버관리"로 텍스트 설정
            binding.buttonDetailApply.text = "멤버관리"
            binding.buttonDetailApply.isEnabled = true
            binding.buttonDetailApply.setOnClickListener {
                navigateToMemberFragment()
            }
        } else {
            setupNonOwnerView(textViewState)
        }
    }

    fun navigateToMemberFragment() {
        val detailMemberFragment = DetailMemberFragment().apply {
            arguments = Bundle().apply {
                putInt("studyIdx", currentStudyData?.studyIdx ?: 0)
                putString("studyTitle", currentStudyData?.studyTitle ?: "")
            }
        }

        // 화면 이동 로직
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out) // 애니메이션 추가
            .replace(R.id.containerMain, detailMemberFragment)
            .addToBackStack(FragmentName.DETAIL_MEMBER.str)
            .commit()
    }


    private fun setupOwnerView(textViewState: TextView) {
        textViewState.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.icon_expand_more_24px,
            0
        )

        textViewState.isEnabled = true
        binding.buttonDetailApply.isEnabled = false
        binding.buttonDetailApply.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pointColor))

        textViewState.setOnClickListener { view ->
            Log.d("DetailFragment", "TextViewState clicked for owner")
            if (!isPopupShown) {
                showStatePopup(view as TextView)
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
    }
    private fun setupNonOwnerView(textViewState: TextView) {
        textViewState.isEnabled = false
        textViewState.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        if (textViewState.text == "모집중") {
            binding.buttonDetailApply.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pointColor))
            binding.buttonDetailApply.setOnClickListener {
                handleNonOwnerButtonClick(it)
            }
        } else {
            binding.buttonDetailApply.setBackgroundColor(Color.parseColor("#777777"))
            binding.buttonDetailApply.setTextColor(Color.BLACK)
            binding.buttonDetailApply.isEnabled = false
        }
    }

    private fun handleNonOwnerButtonClick(view: View) {
        val currentMemberCount = binding.textViewDetailMember.text.toString().toIntOrNull() ?: 0
        val maxMemberCount = currentStudyData?.studyMaxMember ?: 0

        // 신청 인원이 다 찼을 경우 스낵바로 알림 표시 후 리턴
        if (currentMemberCount >= maxMemberCount) {
            showSnackbar(view, "신청 인원이 다 찼습니다. 신청이 불가능합니다.")
            return
        }

        val applyMethod = currentStudyData?.studyApplyMethod ?: "선착순" // 기본값 설정

        lifecycleScope.launch {
            val (isAlreadyMember, isAlreadyApplied, success) = withContext(Dispatchers.IO) {
                // 사용자가 이미 참여 중이거나 이미 신청했는지 먼저 확인
                val isAlreadyMember = viewModel.checkIfUserAlreadyMember(studyIdx, userIdx)
                val isAlreadyApplied = viewModel.isAlreadyApplied(userIdx, studyIdx)

                // 로그로 상태 확인
                Log.d("DetailViewModel", "isAlreadyMember: $isAlreadyMember, isAlreadyApplied: $isAlreadyApplied")

                // 1. 사용자가 이미 참여 중일 때
                if (isAlreadyMember) {
                    return@withContext Triple(true, false, false)
                }

                // 2. 사용자가 이미 신청했을 때
                if (isAlreadyApplied) {
                    return@withContext Triple(false, true, false)
                }

                // 3. 신청 가능한 경우
                val success = viewModel.addUserToStudyOrRequest(
                    studyIdx,
                    userIdx,
                    applyMethod,
                    requireContext(),
                    requireView(),
                    currentStudyData?.studyTitle ?: ""
                )
                return@withContext Triple(false, false, success)
            }

            // 메인 스레드에서 결과 처리
            withContext(Dispatchers.Main) {
                when {
                    isAlreadyMember -> showSnackbar(view, "이미 참여중인 스터디입니다.")
                    isAlreadyApplied -> showSnackbar(view, "이미 신청한 스터디입니다.")
                    success -> {
                        showSnackbar(view, "성공적으로 신청되었습니다.")
                        setupApplyButton()
                        refreshScreen()  // 신청 후 화면 갱신
                    }
                    else -> showSnackbar(view, "신청에 실패하였습니다.")
                }
            }
        }
    }


    private fun showSnackbar(view: View, message: String) {
        // 기존 스낵바가 표시 중이면 닫기
        snackbar?.dismiss()

        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val textView = snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        val textSizeInPx = dpToPx(requireContext(), 14f)
        textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)
        snackbar?.show()
        Log.d("DetailFragment", "Snackbar shown for apply")
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
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
            popupWindow.dismiss()
            // "모집중" 상태로 업데이트
            viewModel.updateStudyCanApplyInBackground(studyIdx, true)
        }
        popupView.findViewById<TextView>(R.id.textViewDetailState2).setOnClickListener {
            binding.textViewDetailState.text = (it as TextView).text
            popupWindow.dismiss()
            // "모집완료" 상태로 업데이트
            viewModel.updateStudyCanApplyInBackground(studyIdx, false)
        }
    }

    private fun setupApplyButton() {
        lifecycleScope.launch {
            // 글 작성자인지 확인
            val isOwner = currentStudyData?.userIdx == userIdx

            if (isOwner) {
                // 작성자는 버튼 비활성화하고 '멤버관리'로 설정
                binding.buttonDetailApply.text = "멤버관리"
                binding.buttonDetailApply.isEnabled = true
                binding.buttonDetailApply.setOnClickListener {
                    navigateToMemberFragment()
                }
            } else {
                // 비작성자인 경우: 신청 상태에 따라 버튼을 설정
                val isMember = viewModel.checkIfUserAlreadyMember(studyIdx, userIdx)
                val isApplied = viewModel.isAlreadyApplied(userIdx, studyIdx)

                // 버튼 상태 업데이트
                when {
                    isMember -> {
                        // 스터디 탈퇴 버튼 설정
                        binding.buttonDetailApply.text = "스터디 탈퇴"
                        binding.buttonDetailApply.setOnClickListener {
                            leaveStudy()
                        }
                    }
                    isApplied -> {
                        // 신청 취소 버튼 설정
                        binding.buttonDetailApply.text = "신청 취소"
                        binding.buttonDetailApply.setOnClickListener {
                            cancelStudyApplication()
                        }
                    }
                    else -> {
                        // 신청하기 버튼 설정
                        binding.buttonDetailApply.text = "신청하기"
                        binding.buttonDetailApply.setOnClickListener {
                            handleNonOwnerButtonClick(it)
                        }
                    }
                }
            }
        }
    }

    private fun cancelStudyApplication() {
        lifecycleScope.launch {
            val success = viewModel.removeUserFromApplyList(studyIdx, userIdx)
            if (success) {
                showSnackbar(requireView(), "스터디 신청이 취소되었습니다.")
                setupApplyButton() // 버튼 상태 업데이트
                refreshScreen() // 화면 갱신
            } else {
                showSnackbar(requireView(), "신청 취소에 실패했습니다.")
            }
        }
    }

    private fun leaveStudy() {
        lifecycleScope.launch {
            val success = viewModel.removeUserFromStudy(studyIdx, userIdx)
            if (success) {
                showSnackbar(requireView(), "스터디에서 탈퇴하였습니다.")
                viewModel.notificationUserLeave(requireContext(), userIdx, studyIdx) // 탈퇴 알림 전송
                setupApplyButton() // 버튼 상태 업데이트
                refreshScreen() // 화면 갱신
            } else {
                showSnackbar(requireView(), "스터디 탈퇴에 실패했습니다.")
            }
        }
    }

    private fun refreshScreen() {
        // 데이터를 다시 불러옵니다
        viewModel.clearLoadingState()  // 상태 초기화
        viewModel.loadStudyData(studyIdx)

        // UI 업데이트를 다시 호출
        fetchDataAndUpdateUI()
    }

}