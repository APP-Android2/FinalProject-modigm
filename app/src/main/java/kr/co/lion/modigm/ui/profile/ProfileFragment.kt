package kr.co.lion.modigm.ui.profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfileBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.profile.adapter.ProfileStudyAdapter
import kr.co.lion.modigm.ui.profile.adapter.LinkAdapter
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class ProfileFragment: DBBaseFragment<FragmentProfileBinding>(R.layout.fragment_profile) {
    private val profileViewModel: ProfileViewModel by viewModels()

    // onCreateView에서 초기화
    var userIdx: Int? = null
    var isBottomNavi: Boolean? = null

    // 어댑터 선언
    val linkAdapter: LinkAdapter = LinkAdapter(
        // 빈 리스트를 넣어 초기화
        emptyList(),

        // 항목을 클릭: Url을 받아온다
        rowClickListener = { linkUrl ->
            Log.d("테스트 rowClickListener deliveryIdx", linkUrl)
            viewLifecycleOwner.lifecycleScope.launch {
                // bundle 에 필요한 정보를 담는다
                val bundle = Bundle()
                bundle.putString("link", linkUrl)

                // 이동할 프래그먼트로 bundle을 넘긴다
                val profileWebFragment = ProfileWebFragment()
                profileWebFragment.arguments = bundle

                // Fragment 교체
                requireActivity().supportFragmentManager.commit {
                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    replace(R.id.containerMain, profileWebFragment)
                    addToBackStack(FragmentName.PROFILE_WEB.str)
                }
            }
        }
    )

    val partStudyAdapter: ProfileStudyAdapter = ProfileStudyAdapter(
        // 빈 리스트를 넣어 초기화
        emptyList(),

        // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
        rowClickListener = { studyIdx ->
            viewLifecycleOwner.lifecycleScope.launch {
                val detailFragment = DetailFragment()

                // Bundle 생성 및 현재 사용자 uid 담기
                val bundle = Bundle()
                Log.d("zunione", "$studyIdx")
                bundle.putInt("studyIdx", studyIdx)

                // Bundle을 ProfileFragment에 설정
                detailFragment.arguments = bundle

                requireActivity().supportFragmentManager.commit {
                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    replace(R.id.containerMain, detailFragment)
                    addToBackStack(FragmentName.DETAIL.str)
                }
            }
        }
    )

    val hostStudyAdapter: ProfileStudyAdapter = ProfileStudyAdapter(
        // 빈 리스트를 넣어 초기화
        emptyList(),

        // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
        rowClickListener = { studyIdx ->
            viewLifecycleOwner.lifecycleScope.launch {
                val detailFragment = DetailFragment()

                // Bundle 생성 및 현재 사용자 uid 담기
                val bundle = Bundle()
                Log.d("zunione", "$studyIdx")
                bundle.putInt("studyIdx", studyIdx)

                // Bundle을 ProfileFragment에 설정
                detailFragment.arguments = bundle

                requireActivity().supportFragmentManager.commit {
                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    replace(R.id.containerMain, detailFragment)
                    addToBackStack(FragmentName.DETAIL.str)
                }
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Bind ViewModel and lifecycle owner
        binding.profileViewModel = profileViewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        userIdx = arguments?.getInt("userIdx")
        isBottomNavi = arguments?.getBoolean("isBottomNavi")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        setupUserInfo()
    }

    private fun initView() {
        setupToolbar()
        setupRecyclerViewLink()
        setupRecyclerViewPartStudy()
        setupRecyclerViewHostStudy()
        setupIconMoreStudy()

        observeData()
    }

    private fun setupToolbar() {
        binding.toolbarProfile.apply {
            // title
            title = "프로필"

            // 툴바 메뉴
            inflateMenu(R.menu.menu_profile)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_profile_setting -> {
                        requireActivity().supportFragmentManager.commit {
                            setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                            replace(R.id.containerMain, SettingsFragment(this@ProfileFragment))
                            addToBackStack(FragmentName.SETTINGS.str)
                        }
                    }

                    R.id.menu_item_profile_more -> {
                        // TODO("신고하기 기능")
                    }
                }
                true
            }

            // 모든 메뉴를 보이지 않는 상태로 둔다.
            // 사용자 정보를 가져온 다음 메뉴를 노출 시킨다.
            menu.findItem(R.id.menu_item_profile_setting).isVisible = false
            menu.findItem(R.id.menu_item_profile_more).isVisible = false

            // BottomNavigation 으로 접근했을 때: 설정 아이콘
            if (isBottomNavi == true) {
                // 설정 아이콘 표시
                menu.findItem(R.id.menu_item_profile_setting).isVisible = true
            } else {
                // 스터디 상세 화면의 프로필로 접근했을 때: 뒤로 가기, 더보기 아이콘
                // 뒤로 가기
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }

                // 더보기 아이콘: 신고 기능이므로 타인의 프로필일 때만 표시
                if (userIdx != prefs.getInt("currentUserIdx")) {
                    menu.findItem(R.id.menu_item_profile_more).isVisible = true
                }
            }
        }
    }

    private fun setupUserInfo() {
        profileViewModel.profileUserIdx.value = userIdx
        profileViewModel.loadUserData()
        profileViewModel.loadUserLinkListData()
        profileViewModel.loadHostStudyList(userIdx!!)
        profileViewModel.loadPartStudyList(userIdx!!)
    }

    private fun setupRecyclerViewLink() {
        // 리사이클러뷰 구성
        binding.recyclerVIewProfileLink.apply {
            // 리사이클러뷰 어댑터
            adapter = linkAdapter

            // 리사이클러뷰 레이아웃
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
    }

    private fun setupRecyclerViewPartStudy() {
        // 리사이클러뷰 구성
        binding.recyclerViewProfilePartStudy.apply {
            // 리사이클러뷰 어댑터
            adapter = partStudyAdapter

            // 리사이클러뷰 레이아웃
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupRecyclerViewHostStudy() {
        // 리사이클러뷰 구성
        binding.recyclerViewProfileHostStudy.apply {
            // 리사이클러뷰 어댑터
            adapter = hostStudyAdapter

            // 리사이클러뷰 레이아웃
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupIconMoreStudy() {
        binding.apply {
            layoutMoreProfileHostStudy.setOnClickListener {
                // bundle 에 필요한 정보를 담는다
                val bundle = Bundle()
                bundle.putInt("type", 1)
                bundle.putInt("userIdx", userIdx!!)

                // 이동할 프래그먼트로 bundle을 넘긴다
                val profileStudyFragment = ProfileStudyFragment()
                profileStudyFragment.arguments = bundle

                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, profileStudyFragment)
                    addToBackStack(FragmentName.PROFILE_STUDY.str)
                }
            }

            layoutMoreProfilePartStudy.setOnClickListener {
                // bundle 에 필요한 정보를 담는다
                val bundle = Bundle()
                bundle.putInt("type", 2)
                bundle.putInt("userIdx", userIdx!!)

                // 이동할 프래그먼트로 bundle을 넘긴다
                val profileStudyFragment = ProfileStudyFragment()
                profileStudyFragment.arguments = bundle

                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, profileStudyFragment)
                    addToBackStack(FragmentName.PROFILE_STUDY.str)
                }
            }
        }
    }

    // 데이터 변경 관찰
    fun observeData() {
        // 프로필 사진
        lifecycleScope.launch {
            profileViewModel.profileUserImage.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { image ->
                Log.d("ProfileFragment", "Profile Image: $image")
                if (!image.isNullOrEmpty()) {
                    val requestOptions = RequestOptions()
                        .placeholder(R.drawable.image_loading_gray) // 필요 시 기본 플레이스홀더 설정
                        .error(R.drawable.image_detail_1) // 이미지 로딩 실패 시 표시할 이미지

                    Glide.with(requireContext())
                        .load(image)
                        .apply(requestOptions)
                        .into(object : CustomViewTarget<ImageView, Drawable>(binding.imageProfilePic) {
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                // 로딩 실패 시 기본 이미지를 보여줌
                                binding.imageProfilePic.setImageResource(R.drawable.image_default_profile)
                            }

                            override fun onResourceCleared(placeholder: Drawable?) {
                                // 리소스가 클리어 될 때
                            }

                            override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                                // 로딩 성공 시
                                binding.imageProfilePic.setImageDrawable(resource)
                            }
                        })
                } else {
                    // 등록되어 있는 이미지가 없을 때
                    binding.imageProfilePic.setImageResource(R.drawable.image_default_profile)
                }
            }
        }

        // 관심 분야 chipGroup
        lifecycleScope.launch {
            profileViewModel.profileInterests.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { interests ->
                Log.d("ProfileFragment", "Interests: $interests")
                // 기존 칩들 제거
                binding.chipGroupProfile.removeAllViews()

                val interestList = interests?.split(",")?.map { it.trim() }

                // 리스트가 변경될 때마다 for 문을 사용하여 아이템을 처리
                if (interestList != null) {
                    for (interest in interestList) {
                        // 아이템 처리 코드
                        binding.chipGroupProfile.addView(Chip(context).apply {
                            text = interest
                            setTextAppearance(R.style.ChipTextStyle)
                            // 자동 padding 없애기
                            setEnsureMinTouchTargetSize(false)
                            // 배경 흰색으로 지정
                            setChipBackgroundColorResource(android.R.color.white)
                            // 클릭 불가
                            isClickable = false
                        })
                    }
                }
            }
        }

        // 링크 리스트
        lifecycleScope.launch {
            profileViewModel.profileLinkList.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { profileLinkList ->
                Log.d("ProfileFragment", "Profile Link List: $profileLinkList")
                linkAdapter.updateData(profileLinkList)

                if (profileLinkList.isEmpty()) {
                    binding.textView4.visibility = View.GONE
                } else {
                    binding.textView4.visibility = View.VISIBLE
                }
            }
        }

        // 진행한 스터디 리스트
        lifecycleScope.launch {
            profileViewModel.profileHostStudyList.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { profileHostStudyList ->
                Log.d("ProfileFragment", "Profile Host Study List: $profileHostStudyList")
                if (profileHostStudyList != null) {
                    hostStudyAdapter.updateData(profileHostStudyList)
                }

                // 데이터 유무에 따른 뷰 가시성 설정
                if (profileHostStudyList.isNullOrEmpty()) {
                    binding.layoutListProfileHostStudy.visibility = View.GONE
                    binding.layoutBlankProfileHostStudy.visibility = View.VISIBLE
                } else {
                    binding.layoutListProfileHostStudy.visibility = View.VISIBLE
                    binding.layoutBlankProfileHostStudy.visibility = View.GONE
                }

                // 2개 이하이면 더보기 아이콘 표시 안함
                if (profileHostStudyList != null) {
                    if (profileHostStudyList.size < 3) {
                        binding.layoutMoreProfileHostStudy.visibility = View.GONE
                    }
                }
            }
        }

        // 참여한 스터디 리스트
        lifecycleScope.launch {
            profileViewModel.profilePartStudyList.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { profilePartStudyList ->
                Log.d("ProfileFragment", "Profile Part Study List: $profilePartStudyList")
                if (profilePartStudyList != null) {
                    partStudyAdapter.updateData(profilePartStudyList)
                }

                // 데이터 유무에 따른 뷰 가시성 설정
                if (profilePartStudyList.isNullOrEmpty()) {
                    binding.layoutListProfilePartStudy.visibility = View.GONE
                    binding.layoutBlankProfilePartStudy.visibility = View.VISIBLE
                } else {
                    binding.layoutListProfilePartStudy.visibility = View.VISIBLE
                    binding.layoutBlankProfilePartStudy.visibility = View.GONE
                }

                // 2개 이하이면 더보기 아이콘 표시 안함
                if (profilePartStudyList != null) {
                    if (profilePartStudyList.size < 3) {
                        binding.layoutMoreProfilePartStudy.visibility = View.GONE
                    }
                }
            }
        }
    }

//    // 1:1 채팅 방 데이터 생성
//    suspend fun addChatRoomData(): Int {
//        var chatIdx = 0
//        val job1 = CoroutineScope(Dispatchers.Main).launch {
//
//            val chatRoomSequence = ChatRoomDataSource.getChatRoomSequence()
//            ChatRoomDataSource.updateChatRoomSequence(chatRoomSequence - 1)
//
//            chatIdx = chatRoomSequence - 1
//            val chatTitle = "1:1 채팅방"
//            val chatRoomImage = ""
//            val chatMemberList = listOf(ModigmApplication.prefs.getUserData("currentUserData")?.userUid, uid)
//            val participantCount = 2
//            val groupChat = false
//            val lastChatMessage = ""
//            val lastChatFullTime = 0L
//            val lastChatTime = ""
//
//            val chatRoomData = ChatRoomData(chatIdx, chatTitle, chatRoomImage, chatMemberList, participantCount, groupChat, lastChatMessage, lastChatFullTime, lastChatTime)
//
//            // 채팅 방 생성
//            ChatRoomDataSource.insertChatRoomData(chatRoomData)
//            Log.d("chatLog5", "ProfileFragment - 1:1 채팅방 생성")
//        }
//        job1.join()
//
//        return chatIdx
//    }
//
//    // 해당 채팅 방으로 입장
//    fun enterChatRoom(chatRoomIdx: Int){
//        val chatRoomFragment = ChatRoomFragment().apply {
//            arguments = Bundle().apply {
//                putInt("chatIdx", chatRoomIdx)
//                putString("chatTitle", "1:1")
//                putStringArrayList("chatMemberList", arrayListOf(ModigmApplication.prefs.getUserData("currentUserData")?.userUid, uid))
//                putInt("participantCount", 2)
//                putBoolean("groupChat", false)
//            }
//        }
//        requireActivity().supportFragmentManager.commit {
//            setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
//            replace(R.id.containerMain, chatRoomFragment)
//            addToBackStack(FragmentName.CHAT_ROOM.str)
//        }
//        Log.d("chatLog5", "ProfileFragment - ${chatRoomIdx}번 채팅방 입장")
//    }
}