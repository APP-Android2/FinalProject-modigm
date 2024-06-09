package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentBottomNaviBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.like.LikeFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication


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
            childFragmentManager.commit {
                replace(R.id.containerBottomNavi, StudyFragment())

            }
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
                            childFragmentManager.commit {
                                replace(R.id.containerBottomNavi, StudyFragment())

                            }
                        }

                        // 찜 클릭 시
                        R.id.bottomNaviHeart -> {
                            childFragmentManager.commit {
                                replace(R.id.containerBottomNavi, LikeFragment())

                            }
                        }

                        // 채팅 클릭 시
                        R.id.bottomNaviChat -> {
                            childFragmentManager.commit {
                                replace(R.id.containerBottomNavi, ChatFragment())

                            }

                        }

                        // 마이 클릭 시
                        R.id.bottomNaviMy -> {
                            val profileFragment = ProfileFragment()

                            // Bundle 생성 및 현재 사용자 uid 담기
                            val bundle = Bundle()
                            Log.d("zunione", ModigmApplication.prefs.getUserData("currentUserData")?.userUid!!)
                            bundle.putString("uid", ModigmApplication.prefs.getUserData("currentUserData")?.userUid)

                            // Bundle을 ProfileFragment에 설정
                            profileFragment.arguments = bundle

                            // Fragment 교체
                            childFragmentManager.commit {
                                replace(R.id.containerBottomNavi, profileFragment)
                            }
                        }
                    }
                    true
                }
            }
        }
    }
}