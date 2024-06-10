package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.like.LikeFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.showCustomSnackbar
import kr.co.lion.modigm.util.ModigmApplication

class BottomNaviFragment : Fragment(R.layout.fragment_bottom_navi) {

    private lateinit var binding: FragmentBottomNaviBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBottomNaviBinding.bind(view)
        initView(binding)

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
                        requireActivity().showCustomSnackbar("한 번 더 누르면 앱이 종료됩니다.",null)
                        // 2초 후에 doubleBackToExitPressedOnce 플래그 초기화
                        view.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                    }
                }
            }
        })
    }

    private fun initView(binding: FragmentBottomNaviBinding) {
        if (childFragmentManager.findFragmentById(R.id.containerBottomNavi) == null) {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace<StudyFragment>(R.id.containerBottomNavi)
            }
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottomNaviStudy -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<StudyFragment>(R.id.containerBottomNavi)
                        addToBackStack(FragmentName.STUDY.str)
                    }
                }
                R.id.bottomNaviHeart -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<LikeFragment>(R.id.containerBottomNavi)
                        addToBackStack(FragmentName.LIKE.str)
                    }
                }
                R.id.bottomNaviChat -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<ChatFragment>(R.id.containerBottomNavi)
                        addToBackStack(FragmentName.CHAT.str)
                    }
                }
                R.id.bottomNaviMy -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<ProfileFragment>(R.id.containerBottomNavi)
                        addToBackStack(FragmentName.PROFILE.str)
                    }
                }
            }
            true
        }
    }
}
