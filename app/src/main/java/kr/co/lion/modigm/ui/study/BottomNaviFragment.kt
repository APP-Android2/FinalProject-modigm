package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.showLoginSnackBar

class BottomNaviFragment : VBBaseFragment<FragmentBottomNaviBinding>(FragmentBottomNaviBinding::inflate) {

    private val joinType: JoinType? by lazy {
        JoinType.getType(arguments?.getString("joinType")?:"")
    }

    // 로그인 상태를 저장하는 변수
    private var isSnackBarShown = false

    // --------------------------------- LC START ---------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 이전 상태에서 isSnackBarShown 값 복원
        isSnackBarShown = savedInstanceState?.getBoolean("isSnackBarShown") ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 isSnackBarShown 상태 저장
        outState.putBoolean("isSnackBarShown", isSnackBarShown)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        // 프리퍼런스 전체 확인 로그 (필터 입력: SharedPreferencesLog)
        prefs.logAllPreferences()

        // 다시 이 화면으로 돌아왔을 때 로그인 스낵바를 띄우지 않기
        if (!isSnackBarShown) {
            showLoginSnackBar()
            isSnackBarShown = true
        }

        backButton()
    }

    // --------------------------------- LC END ---------------------------------

    // 로그인 스낵바를 표시하는 함수
    private fun showLoginSnackBar() {
        if(joinType != null){
            when(joinType) {
                JoinType.EMAIL -> {
                    requireActivity().showLoginSnackBar("이메일 로그인 성공", R.drawable.email_login_logo)
                }
                JoinType.KAKAO -> {
                    requireActivity().showLoginSnackBar("카카오 로그인 성공", R.drawable.kakaotalk_sharing_btn_small)
                }
                JoinType.GITHUB -> {
                    requireActivity().showLoginSnackBar("깃허브 로그인 성공", R.drawable.icon_github_logo)
                }
                else -> {
                    return
                }
            }
        }
    }

    // 뷰 초기화 함수
    private fun initView() {
        val currentNavItemIndex = hashMapOf("index" to 0)

        if (childFragmentManager.findFragmentById(R.id.containerBottomNavi) == null) {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace<StudyFragment>(R.id.containerBottomNavi)
            }
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->

            val newNavItemIndex = when (item.itemId) {
                R.id.bottomNaviStudy -> 0
                R.id.bottomNaviHeart -> 1
                R.id.bottomNaviChat -> 2
                R.id.bottomNaviMy -> 3
                else -> currentNavItemIndex["index"]!!
            }

            // 현재 선택된 아이템과 동일하면 아무 동작도 하지 않음
            if (newNavItemIndex == currentNavItemIndex["index"]) {
                return@setOnItemSelectedListener true
            }

            // 애니메이션 방향 설정
            val enterAnim =
                if (newNavItemIndex > currentNavItemIndex["index"]!!) {
                    R.anim.slide_in_right
                } else {
                    R.anim.slide_in_left
                }
            val exitAnim =
                if (newNavItemIndex > currentNavItemIndex["index"]!!) {
                    R.anim.slide_out_left
                } else {
                    R.anim.slide_out_right
                }

            when(item.itemId) {
                R.id.bottomNaviStudy -> {
                    childFragmentManager.commit {
                        setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
                        setReorderingAllowed(true)
                        replace<StudyFragment>(R.id.containerBottomNavi)
                        addToBackStack(FragmentName.STUDY.str)
                    }
                }
                R.id.bottomNaviHeart -> {
                    childFragmentManager.commit {
                        setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
                        setReorderingAllowed(true)
                        replace<FavoriteFragment>(R.id.containerBottomNavi)
                        addToBackStack(FragmentName.LIKE.str)
                    }
                }
                R.id.bottomNaviChat -> {
                    val chatFragment = ChatFragment().apply {
                        arguments = Bundle().apply {
                            putInt("currentUserIdx", prefs.getInt("currentUserIdx"))
                        }
                    }
                    childFragmentManager.commit {
                        setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
                        setReorderingAllowed(true)
                        replace(R.id.containerBottomNavi, chatFragment)
                        addToBackStack(FragmentName.CHAT.str)
                    }
                }
                R.id.bottomNaviMy -> {
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putInt("currentUserIdx", prefs.getInt("currentUserIdx"))
                        }
                    }
                    childFragmentManager.commit {
                        setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
                        setReorderingAllowed(true)
                        replace(R.id.containerBottomNavi, profileFragment)
                        addToBackStack(FragmentName.PROFILE.str)
                    }
                }
            }
            currentNavItemIndex["index"] = newNavItemIndex
            true
        }
    }

    // 백버튼 동작 설정 함수
    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            private var doubleBackToExitPressedOnce = false

            override fun handleOnBackPressed() {
                val currentFragment = childFragmentManager.findFragmentById(R.id.containerBottomNavi)
                if (currentFragment !is StudyFragment) {
                    // 항상 StudyFragment로 이동
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<StudyFragment>(R.id.containerBottomNavi)
                    }
                    binding.bottomNavigationView.selectedItemId = R.id.bottomNaviStudy
                } else {
                    // 백버튼을 두 번 눌렀을 때 앱 종료
                    if (doubleBackToExitPressedOnce) {
                        requireActivity().finish()
                    } else {
                        doubleBackToExitPressedOnce = true
                        // Snackbar를 표시하여 사용자에게 알림
                        requireActivity().showLoginSnackBar("한 번 더 누르면 앱이 종료됩니다.", null)
                        // 2초 후에 doubleBackToExitPressedOnce 플래그 초기화
                        view?.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                    }
                }
            }
        })
    }
}
