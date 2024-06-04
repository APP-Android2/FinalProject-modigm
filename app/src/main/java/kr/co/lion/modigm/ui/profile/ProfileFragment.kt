package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfileBinding
import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.MainActivity
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
    lateinit var mainActivity: MainActivity
    private val profileViewModel: ProfileViewModel by viewModels()

    val uid = "fKdVSYNodxYgYJHq8MYKlAC2GCk1"
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
        mainActivity = activity as MainActivity

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
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.containerMain, SettingsFragment())
                                .addToBackStack(FragmentName.FILTER_SORT.str)
                                .commit()
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
                        mainActivity.replaceFragment(FragmentName.PROFILE, false,true,null)
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
        profileViewModel.loadUserData(uid, requireContext(), fragmentProfileBinding.imageProfilePic, fragmentProfileBinding.chipGroupProfile)
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
                layoutManager = LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)
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
                layoutManager = LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)
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
                layoutManager = LinearLayoutManager(mainActivity)
            }
        }
    }

    fun observeData() {
        // 데이터 변경 관찰
        viewLifecycleOwner.lifecycleScope.launch {
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
}