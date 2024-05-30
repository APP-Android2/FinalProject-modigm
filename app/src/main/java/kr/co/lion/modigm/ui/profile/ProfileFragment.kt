package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val addressViewModel: ProfileViewModel by viewModels()

    lateinit var user: UserData
    var myProfile = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentProfileBinding = FragmentProfileBinding.inflate(inflater,container,false)
        mainActivity = activity as MainActivity

        return fragmentProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 uid를 사용해 프로필 주인의 정보를 불러온다
        CoroutineScope(Dispatchers.Main).launch {
            user = RemoteUserDataSource.loadUserDataByUid("fKdVSYNodxYgYJHq8MYKlAC2GCk1")!!

            setupMemberInfo(user)
            setupRecyclerViewLink(user)
            setupRecyclerViewPartStudy()
            setupRecyclerViewHostStudy()
        }

        setupToolbar()
        setupFab()
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

    private fun setupMemberInfo(user: UserData) {
        fragmentProfileBinding.apply {
            // 이름
            textViewProfileName.text = user.userName
            // 자기소개
            textViewProfileIntro.text = user.userIntro
            // 관심분야
            for (interestNum in user.userInterestList) {
                chipGroupProfile.addView(Chip(mainActivity).apply {
                    // chip 텍스트 설정: 저장되어 있는 숫자로부터 enum 클래스를 불러오고 저장된 str 보여주기
                    text = Interest.fromNum(interestNum)!!.str
                    // 자동 padding 없애기
                    setEnsureMinTouchTargetSize(false)
                    // 배경 흰색으로 지정
                    setChipBackgroundColorResource(android.R.color.white)
                    // 클릭 불가
                    isClickable = false
                    // chip에서 X 버튼 보이게 하기
                    //isCloseIconVisible = true
                    // X버튼 누르면 chip 없어지게 하기
                    //setOnCloseIconClickListener { fragmentProfileBinding.chipGroupProfile.removeView(this) }
                })
            }

            // 프로필 이미지
            CoroutineScope(Dispatchers.Main).launch {
                RemoteUserDataSource.loadUserProfilePic(
                    mainActivity,
                    user.userProfilePic,
                    imageProfilePic
                )
            }
        }
    }

    private fun setupRecyclerViewLink(user: UserData) {
        // 어댑터 선언
        val linkAdapter: LinkAdapter = LinkAdapter(
            // 사용자 정보 중 링크 목록
            user.userLinkList,

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
        // 참여한 스터디 리스트 불러오기
        lateinit var partStudyList: List<StudyData>
        CoroutineScope(Dispatchers.Main).launch {
            partStudyList = RemoteStudyDataSource.loadUserPartStudy(user.userNumber)

            // 어댑터 선언
            val partStudyAdapter: PartStudyAdapter = PartStudyAdapter(
                // 빈 리스트를 넣어 초기화
                partStudyList,

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
    }

    private fun setupRecyclerViewHostStudy() {
        // 진행한 스터디 리스트 불러오기
        lateinit var hostStudyList: List<StudyData>
        CoroutineScope(Dispatchers.Main).launch {
            hostStudyList = RemoteStudyDataSource.loadUserHostStudy(user.userNumber)

            // 어댑터 선언
            val hostStudyAdapter: HostStudyAdapter = HostStudyAdapter(
                // 빈 리스트를 넣어 초기화
                hostStudyList,

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
    }
}