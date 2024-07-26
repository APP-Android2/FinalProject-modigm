package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.favorite.FavoriteFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.showLoginSnackBar

class BottomNaviFragment : Fragment(R.layout.fragment_bottom_navi) {

    private val viewModel: StudyViewModel by viewModels()

    private val prefs by lazy {
        ModigmApplication.prefs
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBottomNaviBinding.bind(view)

        initView(binding)
        // 프리퍼런스 전체 확인 로그 (필터 입력: SharedPreferencesLog)
        ModigmApplication.prefs.logAllPreferences()

        backBotton(binding)

    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC START ---------------------------------

    private var currentNavItemIndex = 0

    private fun initView(binding: FragmentBottomNaviBinding) {
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
            currentNavItemIndex = newNavItemIndex
            true
        }
    }

    private fun backBotton(binding: FragmentBottomNaviBinding){
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
                        requireActivity().showLoginSnackBar("한 번 더 누르면 앱이 종료됩니다.",null)
                        // 2초 후에 doubleBackToExitPressedOnce 플래그 초기화
                        view?.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                    }
                }
            }
        })
    }
}
