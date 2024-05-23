package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import kr.co.lion.modigm.databinding.FragmentJoinBinding
import kr.co.lion.modigm.ui.join.adapter.JoinViewPagerAdapter

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentJoinBinding.inflate(inflater)
        settingToolBar()
        settingViewPagerAdapter()

        return binding.root
    }

    private fun settingToolBar(){
        with(binding.toolbarJoin){
            title = "회원가입"
            setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
            setNavigationOnClickListener {
                if(binding.viewPagerJoin.currentItem!=0){
                    binding.viewPagerJoin.currentItem -= 1
                }else{
                    // JoinFragment 회원가입 종료, 확인 알림창 띄우기?
                }
            }
        }
    }

    private fun settingViewPagerAdapter(){

        val viewPagerAdapter = JoinViewPagerAdapter(this)
        viewPagerAdapter.addFragment(JoinStep1Fragment())
        viewPagerAdapter.addFragment(JoinStep2Fragment())
        viewPagerAdapter.addFragment(JoinStep3Fragment())

        with(binding){
            // 어댑터 설정
            viewPagerJoin.adapter = viewPagerAdapter
            // 전환 방향
            viewPagerJoin.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            // 터치로 스크롤 막기
            viewPagerJoin.isUserInputEnabled = false
        }

        // 다음 버튼 클릭 시 다음 화면으로 넘어가기
        binding.buttonJoinNext.setOnClickListener {
            // 화면별로 유효성 검사 먼저 하고
            when(binding.viewPagerJoin.currentItem){
                // 이메일, 비밀번호 화면
                0 -> {
                    val fragment = viewPagerAdapter.createFragment(0) as JoinStep1Fragment
                    // 유효성 검사
                    val validation = fragment.validate()
                    if(!validation) return@setOnClickListener
                    // 응답값
                    val email = fragment.getJoinUserEmail()
                    val password = fragment.getJoinUserPassword()
                }
                // 이름, 전화번호 인증 화면
                1 -> {
                    val fragment = viewPagerAdapter.createFragment(1) as JoinStep2Fragment
                    // 유효성 검사
                    val validation = fragment.validate()
                    if(!validation) return@setOnClickListener
                    // 응답값
                    val name = fragment.getUserName()
                    val phoneNumber = fragment.getUserPhone()
                }
                // 관심 분야 선택 화면
                2 -> {
                    val fragment = viewPagerAdapter.createFragment(2) as JoinStep3Fragment
                    // 유효성 검사
                    val validation = fragment.validate()
                    if(!validation) return@setOnClickListener
                    // 응답값
                    val interest = fragment.getInterests()
                }
            }
            // 다음 페이지로 이동
            binding.viewPagerJoin.currentItem += 1
        }

    }

}