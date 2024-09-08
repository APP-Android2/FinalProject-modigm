package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.notification.NotificationFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.ui.study.vm.BottomNaviViewModel
import kr.co.lion.modigm.ui.write.WriteFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.showLoginSnackBar
import kotlin.system.exitProcess

class BottomNaviFragment : VBBaseFragment<FragmentBottomNaviBinding>(FragmentBottomNaviBinding::inflate), OnRecyclerViewScrollListener {

    // 회원가입 타입
    private val joinType: JoinType? by lazy {
        JoinType.getType(arguments?.getString("joinType") ?: "")
    }

    // 태그
    private val logTag by lazy { BottomNaviFragment::class.simpleName }

    // 뷰모델
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
            view?.postDelayed({
                if (isAdded && view != null) {
                    binding.fabStudyWrite.show()
                    isFabVisible = true
                }
            }, 600)
        }
    }

    // 백버튼 콜백
    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            private var doubleBackToExitPressedOnce = false

            override fun handleOnBackPressed() {

                with(binding){
                    if(bottomNavigationView.selectedItemId == R.id.bottomNaviStudy){
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
                    } else {
                        // 현재 프래그먼트가 StudyFragment가 아닌 경우 StudyFragment로 이동
                        bottomNavigationView.selectedItemId = R.id.bottomNaviStudy
                    }
                }

            }
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
                2 -> R.id.bottomNaviNotification
                3 -> R.id.bottomNaviMy
                else -> R.id.bottomNaviStudy
            }
        }

        // 뷰 초기화
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

    override fun onDestroyView() {
        super.onDestroyView()
        // 백버튼 콜백 제거
        backPressedCallback.remove()
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

    // 현재 선택된 아이템의 인덱스를 기억하기 위한 변수
    private var currentNavItemIndex = 0

    // 뷰 초기화 함수
    private fun initView() {
        with(binding){
            // 스터디 작성 버튼 클릭 시
            fabStudyWrite.setOnClickListener {
                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, WriteFragment())
                    addToBackStack(FragmentName.WRITE.str)
                }
            }

            if (childFragmentManager.findFragmentById(R.id.containerBottomNavi) == null) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<StudyFragment>(R.id.containerBottomNavi)
                }
            }
            bottomNavigationView.setOnItemSelectedListener { item ->

                val newNavItemIndex = when (item.itemId) {
                    R.id.bottomNaviStudy -> 0
                    R.id.bottomNaviHeart -> 1
                    R.id.bottomNaviNotification -> 2
                    R.id.bottomNaviMy -> 3
                    else -> currentNavItemIndex
                }

                // 현재 선택된 아이템과 동일하면 아무 동작도 하지 않음
                if (newNavItemIndex == currentNavItemIndex) {
                    return@setOnItemSelectedListener true
                }

                when(item.itemId) {
                    R.id.bottomNaviStudy -> {
                        fabStudyWrite.show()
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace<StudyFragment>(R.id.containerBottomNavi)
                            addToBackStack(FragmentName.STUDY.str)
                        }
                    }
                    R.id.bottomNaviHeart -> {
                        fabStudyWrite.hide()
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace<FavoriteFragment>(R.id.containerBottomNavi)
                            addToBackStack(FragmentName.FAVORITE.str)
                        }
                    }
                    R.id.bottomNaviNotification -> {
                        fabStudyWrite.hide()
                        val notificationFragment = NotificationFragment().apply {
                            arguments = Bundle().apply {
                                putInt("currentUserIdx", prefs.getInt("currentUserIdx"))
                            }
                        }
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.containerBottomNavi, notificationFragment)
                            addToBackStack(FragmentName.NOTI.str)
                        }
                    }
                    R.id.bottomNaviMy -> {
                        fabStudyWrite.hide()
                        val profileFragment = ProfileFragment().apply {
                            arguments = Bundle().apply {
                                putInt("userIdx", prefs.getInt("currentUserIdx"))
                            }
                        }
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.containerBottomNavi, profileFragment)
                            addToBackStack(FragmentName.PROFILE.str)
                        }
                    }
                }
                currentNavItemIndex = newNavItemIndex
                true
            }
        }

    }

    // 백버튼 종료 동작
    private fun backButton() {
        // 백버튼 콜백을 안전하게 추가
        backPressedCallback.let { callback ->
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
    }
}
