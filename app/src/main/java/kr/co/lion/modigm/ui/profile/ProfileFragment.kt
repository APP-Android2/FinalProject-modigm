package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfileBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.profile.adapter.HostStudyAdapter
import kr.co.lion.modigm.ui.profile.adapter.LinkAdapter
import kr.co.lion.modigm.ui.profile.adapter.PartStudyAdapter
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.Interest

class ProfileFragment: Fragment() {
    lateinit var fragmentProfileBinding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    // arguments에서 불러옴
    val uid = "J04y39mPQ8fLIm2LukmdpRVGN8b2"
    var myProfile = true

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
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, ProfileWebFragment())
                    .addToBackStack(FragmentName.FILTER_SORT.str)
                    .commit()
            }
        }
    )

    val partStudyAdapter: PartStudyAdapter = PartStudyAdapter(
        // 빈 리스트를 넣어 초기화
        emptyList(),

        // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
        rowClickListener = { studyIdx ->
            Log.d("테스트 rowClickListener deliveryIdx", studyIdx)
            viewLifecycleOwner.lifecycleScope.launch {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, DetailFragment())
                    .addToBackStack(FragmentName.FILTER_SORT.str)
                    .commit()
            }
        }
    )

    val hostStudyAdapter: HostStudyAdapter = HostStudyAdapter(
        // 빈 리스트를 넣어 초기화
        emptyList(),

        // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
        rowClickListener = { studyIdx ->
            Log.d("테스트 rowClickListener deliveryIdx", studyIdx)
            viewLifecycleOwner.lifecycleScope.launch {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, DetailFragment())
                    .addToBackStack(FragmentName.FILTER_SORT.str)
                    .commit()
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentProfileBinding.profileViewModel = profileViewModel
        fragmentProfileBinding.lifecycleOwner = this

        return fragmentProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setupToolbar()
        setupFab()
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
//                            parentFragmentManager.beginTransaction()
//                                .replace(R.id.containerMain, SettingsFragment())
//                                .addToBackStack(FragmentName.SETTINGS.str)
//                                .commit()

                            // 현재 프래그먼트의 부모 프래그먼트 (BottomNaviFragment) 가져오기
                            val bottomNaviFragment = parentFragment

                            // bottomNaviFragment가 null이 아니고 상위 액티비티가 존재하는 경우
                            bottomNaviFragment?.let {
                                val fragmentManager = it.requireActivity().supportFragmentManager

                                // FragmentTransaction을 통해 containerMain에 SettingsFragment를 교체
                                fragmentManager.beginTransaction().apply {
                                    replace(R.id.containerMain, SettingsFragment())
                                    addToBackStack(FragmentName.SETTINGS.str)
                                    commit()
                                }
                            } ?: run {
                                // 예외 처리: bottomNaviFragment가 null인 경우 로그 출력
                                Log.e("FragmentReplace", "Cannot access BottomNaviFragment or its FragmentManager")
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
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.containerMain, SettingsFragment())
                            .addToBackStack(FragmentName.FILTER_SORT.str)
                            .commit()
                    }

                    // 더보기 아이콘 표시
                    menu.findItem(R.id.menu_item_profile_more).isVisible = true
                }
            }
        }
    }

    private fun setupFab() {
        fragmentProfileBinding.apply {
            fabProfile.apply {
                if (myProfile) {
                    // 본인의 프로필일 때
                    visibility = View.INVISIBLE
                }

                setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.containerMain, ChatFragment())
                        .addToBackStack(FragmentName.FILTER_SORT.str)
                        .commit()
                }
            }
        }
    }

    private fun setupUserInfo() {
        profileViewModel.loadUserData(uid, requireContext(), fragmentProfileBinding.imageProfilePic)
        profileViewModel.loadPartStudyList(uid)
        profileViewModel.loadHostStudyList(uid)
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

    fun observeData() {
        // 데이터 변경 관찰
        // 관심 분야 chipGroup
        profileViewModel.profileInterestList.observe(viewLifecycleOwner, Observer { list ->
            // 리스트가 변경될 때마다 for 문을 사용하여 아이템을 처리
            for (interestNum in list) {
                // 아이템 처리 코드
                fragmentProfileBinding.chipGroupProfile.addView(Chip(context).apply {
                    // chip 텍스트 설정: 저장되어 있는 숫자로부터 enum 클래스를 불러오고 저장된 str 보여주기
                    text = Interest.fromNum(interestNum)!!.str
                    // 자동 padding 없애기
                    setEnsureMinTouchTargetSize(false)
                    // 배경 흰색으로 지정
                    setChipBackgroundColorResource(android.R.color.white)
                    // 클릭 불가
                    isClickable = false
                })
            }
        })

        // 링크 리스트
        profileViewModel.profileLinkList.observe(viewLifecycleOwner) { profileLinkList ->
            linkAdapter.updateData(profileLinkList)
        }

        // 참여한 스터디 리스트
        profileViewModel.profilePartStudyList.observe(viewLifecycleOwner) { profilePartStudyList ->
            partStudyAdapter.updateData(profilePartStudyList)
        }

        // 진행한 스터디 리스트
        profileViewModel.profileHostStudyList.observe(viewLifecycleOwner) { profileHostStudyList ->
            hostStudyAdapter.updateData(profileHostStudyList)
        }
    }
}