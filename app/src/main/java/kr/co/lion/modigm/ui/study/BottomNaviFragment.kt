package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.like.LikeFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName


class BottomNaviFragment : Fragment(R.layout.fragment_bottom_navi) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentBottomNaviBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)

        // 뒤로 가기 콜백 설정
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 0) {
                    childFragmentManager.popBackStack()
                } else {
                    requireActivity().finish()
                }
            }
        })
    }

    private fun initView(binding: FragmentBottomNaviBinding) {

        // 최초 화면이 null 이라면 스터디 목록을 띄운다.
        if(childFragmentManager.findFragmentById(R.id.containerBottomNavi) == null){
            childFragmentManager.beginTransaction()
                .replace(R.id.containerBottomNavi, StudyFragment())
                .commit()
        }

        // 바인딩
        with(binding){

            // 바텀 내비게이션
            with(bottomNavigationView){

                // 아이템 클릭 시 전환 설정
                setOnItemSelectedListener { item ->
                    when(item.itemId) {

                        // 스터디 클릭 시
                        R.id.bottomNaviStudy -> {
                            childFragmentManager.beginTransaction()
                                .replace(R.id.containerBottomNavi, StudyFragment())
                                .commit()
                        }

                        // 찜 클릭 시
                        R.id.bottomNaviHeart -> {
                            childFragmentManager.beginTransaction()
                                .replace(R.id.containerBottomNavi, LikeFragment())
                                .commit()
                        }

                        // 채팅 클릭 시
                        R.id.bottomNaviChat -> {
                            childFragmentManager.beginTransaction()
                                .replace(R.id.containerBottomNavi, ChatFragment())
                                .commit()
                        }

                        // 마이 클릭 시
                        R.id.bottomNaviMy -> {
                            childFragmentManager.beginTransaction()
                                .replace(R.id.containerBottomNavi, ProfileFragment())
                                .commit()
                        }

                    }
                    true
                }
            }
        }
    }
}