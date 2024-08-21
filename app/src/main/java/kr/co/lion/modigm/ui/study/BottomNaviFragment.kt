package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.ui.study.vm.BottomNaviViewModel
import kr.co.lion.modigm.ui.write.WriteFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.showLoginSnackBar
import kotlin.system.exitProcess

class BottomNaviFragment : VBBaseFragment<FragmentBottomNaviBinding>(FragmentBottomNaviBinding::inflate),
    OnRecyclerViewScrollListener {

    private val joinType: JoinType? by lazy {
        JoinType.getType(arguments?.getString("joinType") ?: "")
    }

    private val viewModel: BottomNaviViewModel by viewModels()

    // FAB 가시성 상태를 위한 플래그
    private var isFabVisible = true

    // 스터디 목록 스크롤 시 FAB 동작
    override fun onRecyclerViewScrolled(dy: Int) {
        if (dy != 0 && isFabVisible) {
            // 스크롤 중일 때 FAB 숨기기
            binding.fabStudyWrite.hide()
            isFabVisible = false
        }
    }

    // 스크롤 상태 변경 시 동작
    override fun onRecyclerViewScrollStateChanged(newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE && !isFabVisible) {
            // 스크롤이 멈췄을 때 0.5초 후에 FAB 보이기
            view?.postDelayed({
                binding.fabStudyWrite.show()
                isFabVisible = true
            }, 800)
        }
    }

    // --------------------------------- LC START ---------------------------------

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 ViewModel의 isSnackBarShown 상태 저장
        outState.putBoolean("isSnackBarShown", viewModel.isSnackBarShown.value ?: false)

        // 현재 선택된 아이템 인덱스 상태 저장
        outState.putInt("currentNavItemIndex", currentNavItemIndex)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // savedInstanceState에서 isSnackBarShown 값 복원
        savedInstanceState?.getBoolean("isSnackBarShown")?.let {
            viewModel.setSnackBarShown(it)
        }

        // savedInstanceState에서 currentNavItemIndex 값 복원
        savedInstanceState?.getInt("currentNavItemIndex")?.let {
            currentNavItemIndex = it
            binding.bottomNavigationView.selectedItemId = when (currentNavItemIndex) {
                1 -> R.id.bottomNaviHeart
                2 -> R.id.bottomNaviChat
                3 -> R.id.bottomNaviMy
                else -> R.id.bottomNaviStudy
            }
        }

        initView()
        // 프리퍼런스 전체 확인 로그 (필터 입력: SharedPreferencesLog)
        prefs.logAllPreferences()

        // 다시 이 화면으로 돌아왔을 때 로그인 스낵바를 띄우지 않기
        if (viewModel.isSnackBarShown.value == false) {
            showLoginSnackBar()
            viewModel.setSnackBarShown(true)
        }

        backButton()
    }

    // --------------------------------- LC END ---------------------------------

    // 로그인 스낵바를 표시하는 함수
    private fun showLoginSnackBar() {
        if (joinType != null) {
            when (joinType) {
                JoinType.EMAIL -> {
                    requireActivity().showLoginSnackBar("이메일 로그인 성공", JoinType.EMAIL.icon)
                }

                JoinType.KAKAO -> {
                    requireActivity().showLoginSnackBar("카카오 로그인 성공", JoinType.KAKAO.icon)
                }

                JoinType.GITHUB -> {
                    requireActivity().showLoginSnackBar("깃허브 로그인 성공", JoinType.GITHUB.icon)
                }

                else -> {
                    return
                }
            }
        }
    }

    // 현재 보여지고 있는 프래그먼트를 기억하기 위한 변수
    private var activeFragment: Fragment? = null

    private var currentNavItemIndex = 0

    // 뷰 초기화 함수
    private fun initView() {

        with(binding) {
            val fragments = mapOf(
                FragmentName.STUDY.str to StudyFragment(),
                FragmentName.LIKE.str to FavoriteFragment(),
                FragmentName.CHAT.str to ChatFragment().apply {
                    arguments = Bundle().apply {
                        putInt("currentUserIdx", prefs.getInt("currentUserIdx"))
                    }
                },
                FragmentName.PROFILE.str to ProfileFragment().apply {
                    arguments = Bundle().apply {
                        putInt("userIdx", prefs.getInt("currentUserIdx"))
                    }
                }
            )

            // 초기 프래그먼트 설정
            if (childFragmentManager.findFragmentById(R.id.containerBottomNavi) == null) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.containerBottomNavi, fragments[FragmentName.STUDY.str]!!, FragmentName.STUDY.str)
                }
                activeFragment = fragments[FragmentName.STUDY.str]
            }

            fabStudyWrite.setOnClickListener {
                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, WriteFragment())
                    addToBackStack(FragmentName.WRITE.str)
                }
            }

            // 바텀 네비게이션 아이템 선택 리스너 설정
            bottomNavigationView.setOnItemSelectedListener { item ->

                val newNavItemIndex = when (item.itemId) {
                    R.id.bottomNaviStudy -> 0
                    R.id.bottomNaviHeart -> 1
                    R.id.bottomNaviChat -> 2
                    R.id.bottomNaviMy -> 3
                    else -> currentNavItemIndex
                }

                // 현재 선택된 아이템과 동일하면 아무 동작도 하지 않음
                if (newNavItemIndex == currentNavItemIndex) {
                    return@setOnItemSelectedListener true
                }

                // 애니메이션 방향 설정
                val enterAnim =
                    if (newNavItemIndex > currentNavItemIndex) {
                        R.anim.slide_in_right
                    } else {
                        R.anim.slide_in_left
                    }
                val exitAnim =
                    if (newNavItemIndex > currentNavItemIndex) {
                        R.anim.slide_out_left
                    } else {
                        R.anim.slide_out_right
                    }

                val fragmentToShow = when (item.itemId) {
                    R.id.bottomNaviStudy -> fragments[FragmentName.STUDY.str]
                    R.id.bottomNaviHeart -> fragments[FragmentName.LIKE.str]
                    R.id.bottomNaviChat -> fragments[FragmentName.CHAT.str]
                    R.id.bottomNaviMy -> fragments[FragmentName.PROFILE.str]
                    else -> null
                }

                fragmentToShow?.let { fragment ->
                    fabStudyWrite.visibility = if (item.itemId == R.id.bottomNaviStudy || item.itemId == R.id.bottomNaviHeart) View.VISIBLE else View.GONE

                    childFragmentManager.commit {
                        setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
                        setReorderingAllowed(true)
                        activeFragment?.let { hide(it) }
                        if (!fragment.isAdded) {
                            add(R.id.containerBottomNavi, fragment, fragment.tag)
                        }
                        show(fragment)
                    }
                    activeFragment = fragment
                }

                // 선택된 아이템 인덱스 업데이트
                currentNavItemIndex = newNavItemIndex
                true
            }
        }

    }

    // 백버튼 종료 동작
    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                private var doubleBackToExitPressedOnce = false

                override fun handleOnBackPressed() {
                    // 백버튼을 두 번 눌렀을 때 앱 종료
                    if (doubleBackToExitPressedOnce) {
                        requireActivity().finishAffinity()
                        exitProcess(0) // 앱 프로세스를 완전히 종료
                    } else {
                        doubleBackToExitPressedOnce = true
                        // Snackbar를 표시하여 사용자에게 알림
                        requireActivity().showLoginSnackBar("한 번 더 누르면 앱이 종료됩니다.", null)
                        // 2초 후에 doubleBackToExitPressedOnce 플래그 초기화
                        view?.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                    }
                }
            })
    }
}
