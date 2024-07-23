package kr.co.lion.modigm.ui.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfileBinding
import kr.co.lion.modigm.ui.chat.vm.ChatRoomViewModel
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.profile.adapter.HostStudyAdapter
import kr.co.lion.modigm.ui.profile.adapter.LinkAdapter
import kr.co.lion.modigm.ui.profile.adapter.PartStudyAdapter
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.FragmentName

class ProfileFragment: Fragment() {
    lateinit var fragmentProfileBinding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val chatRoomViewModel: ChatRoomViewModel by viewModels()

    // onCreateView에서 초기화
    var userIdx: Int? = null
    var myProfile: Boolean = true

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
                    add(R.id.containerMain, profileWebFragment)
                    addToBackStack(FragmentName.PROFILE_WEB.str)
                }
            }
        }
    )

    val partStudyAdapter: PartStudyAdapter = PartStudyAdapter(
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
                    add(R.id.containerMain, detailFragment)
                    addToBackStack(FragmentName.DETAIL.str)
                }
            }
        }
    )

    val hostStudyAdapter: HostStudyAdapter = HostStudyAdapter(
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
                    add(R.id.containerMain, detailFragment)
                    addToBackStack(FragmentName.DETAIL.str)
                }
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentProfileBinding.profileViewModel = profileViewModel
        fragmentProfileBinding.lifecycleOwner = this

        userIdx = 9689//arguments?.getInt("userIdx")
        //myProfile = userIdx == ModigmApplication.prefs.getUserData("currentUserData")?.userIdx

        return fragmentProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    fun updateViews() {
        setupUserInfo()
    }

    private fun initView() {
        setupToolbar()
        //setupFab()
        setupUserInfo()
        setupRecyclerViewLink()
        setupRecyclerViewPartStudy()
        setupRecyclerViewHostStudy()

        observeData()
    }

    private fun setupToolbar() {
        fragmentProfileBinding.apply {
            toolbarProfile.apply {
                // title
                title = "프로필"

                // 툴바 메뉴
                inflateMenu(R.menu.menu_profile)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_item_profile_setting -> {
                            requireActivity().supportFragmentManager.commit {
                                setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                                add(R.id.containerMain, SettingsFragment(this@ProfileFragment))
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

                // 본인의 프로필일 때: 설정 아이콘
                if (myProfile) {
                    // 설정 아이콘 표시
                    menu.findItem(R.id.menu_item_profile_setting).isVisible = true
                } else {
                    // 타인의 프로필일 때: 뒤로 가기, 더보기 아이콘
                    // 뒤로 가기
                    setNavigationIcon(R.drawable.icon_arrow_back_24px)
                    setNavigationOnClickListener {
                        parentFragmentManager.popBackStack()
                    }

                    // 더보기 아이콘 표시
                    menu.findItem(R.id.menu_item_profile_more).isVisible = true
                }
            }
        }
    }

//    private fun setupFab() {
//        fragmentProfileBinding.apply {
//            fabProfile.apply {
//                if (myProfile) {
//                    // 본인의 프로필일 때
//                    visibility = View.INVISIBLE
//                }
//                else {
//                    // 1:1 채팅 방 찾기
//                    lifecycleScope.launch {
//                        chatRoomViewModel.findChatRoomIdx(ModigmApplication.prefs.getUserData("currentUserData")?.userUid!!, uid!!)
//                    }
//                    setOnClickListener {
//                        chatRoomViewModel.chatRoomIdx.observe(viewLifecycleOwner, Observer { chatRoomIdx ->
//                            // 채팅방 없음(생성 O)
//                            if (chatRoomIdx == 0) {
//                                CoroutineScope(Dispatchers.Main).launch {
//                                    val thisChatRoomIdx = addChatRoomData()
//                                    enterChatRoom(thisChatRoomIdx)
//                                }
//                            }
//                            // 채팅방 있음(생성 X)
//                            else {
//                                enterChatRoom(chatRoomIdx)
//                            }
//                        })
//                    }
//                }
//            }
//        }
//    }

    private fun setupUserInfo() {
        Log.d("zunione", "setupUserInfo")
        profileViewModel.profileUserIdx.value = userIdx
        profileViewModel.loadUserData()
        profileViewModel.loadUserLinkListData()
        //profileViewModel.loadPartStudyList(uid!!)
        //profileViewModel.loadHostStudyList(uid!!)
    }

    private fun setupRecyclerViewLink() {
        // 리사이클러뷰 구성
        fragmentProfileBinding.apply {
            recyclerVIewProfileLink.apply {
                // 리사이클러뷰 어댑터
                adapter = linkAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
        }
    }

    private fun setupRecyclerViewPartStudy() {
        // 리사이클러뷰 구성
        fragmentProfileBinding.apply {
            recyclerViewProfilePartStudy.apply {
                // 리사이클러뷰 어댑터
                adapter = partStudyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
        }
    }

    private fun setupRecyclerViewHostStudy() {
        // 리사이클러뷰 구성
        fragmentProfileBinding.apply {
            recyclerViewProfileHostStudy.apply {
                // 리사이클러뷰 어댑터
                adapter = hostStudyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    // 데이터 변경 관찰
    fun observeData() {
        // 프로필 사진
        profileViewModel.profileUserImage.observe(viewLifecycleOwner) { image ->
            if (image.isNotEmpty()) {
                val imageBytes = Base64.decode(image, Base64.DEFAULT) // Base64 문자열을 바이트 배열로 디코딩
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) // 바이트 배열을 비트맵으로 디코딩
                Glide.with(requireContext()) // Glide를 사용하여 이미지를 로드
                    .load(bitmap)
                    .into(fragmentProfileBinding.imageProfilePic)
            } else {
                // Handle the case where the image string is null (e.g., show a default image)
                fragmentProfileBinding.imageProfilePic.setImageResource(R.drawable.image_default_profile)
            }
        }

        // 관심 분야 chipGroup
        profileViewModel.profileInterests.observe(viewLifecycleOwner) { interests ->
            // 기존 칩들 제거
            fragmentProfileBinding.chipGroupProfile.removeAllViews()

            val interestList = interests.split(",").map { it.trim() }

            // 리스트가 변경될 때마다 for 문을 사용하여 아이템을 처리
            for (interest in interestList) {
                // 아이템 처리 코드
                fragmentProfileBinding.chipGroupProfile.addView(Chip(context).apply {
                    // chip 텍스트 설정: 저장되어 있는 숫자로부터 enum 클래스를 불러오고 저장된 str 보여주기
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

        // 링크 리스트
        profileViewModel.profileLinkList.observe(viewLifecycleOwner) { profileLinkList ->
            linkAdapter.updateData(profileLinkList)

            if (profileLinkList.isEmpty()) {
                fragmentProfileBinding.textView4.visibility = View.GONE
            } else {
                fragmentProfileBinding.textView4.visibility = View.VISIBLE
            }
        }

        // 참여한 스터디 리스트
        profileViewModel.profilePartStudyList.observe(viewLifecycleOwner) { profilePartStudyList ->
            partStudyAdapter.updateData(profilePartStudyList)

            // 데이터 유무에 따른 뷰 가시성 설정
            if (profilePartStudyList.isEmpty()) {
                fragmentProfileBinding.recyclerViewProfilePartStudy.visibility = View.GONE
                fragmentProfileBinding.layoutBlankProfilePartStudy.visibility = View.VISIBLE
            } else {
                fragmentProfileBinding.recyclerViewProfilePartStudy.visibility = View.VISIBLE
                fragmentProfileBinding.layoutBlankProfilePartStudy.visibility = View.GONE
            }
        }

        // 진행한 스터디 리스트
        profileViewModel.profileHostStudyList.observe(viewLifecycleOwner) { profileHostStudyList ->
            hostStudyAdapter.updateData(profileHostStudyList)

            // 데이터 유무에 따른 뷰 가시성 설정
            if (profileHostStudyList.isEmpty()) {
                fragmentProfileBinding.recyclerViewProfileHostStudy.visibility = View.GONE
                fragmentProfileBinding.layoutBlankProfileHostStudy.visibility = View.VISIBLE
            } else {
                fragmentProfileBinding.recyclerViewProfileHostStudy.visibility = View.VISIBLE
                fragmentProfileBinding.layoutBlankProfileHostStudy.visibility = View.GONE
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