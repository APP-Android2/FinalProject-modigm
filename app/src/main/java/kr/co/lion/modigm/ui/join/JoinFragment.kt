package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinBinding
import kr.co.lion.modigm.ui.join.adapter.JoinViewPagerAdapter

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding

    private val fragmentList : ArrayList<Fragment> by lazy {
        arrayListOf(
            JoinStep1Fragment(),
            JoinStep2Fragment(),
            JoinStep3Fragment()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        // 키보드가 올려올때 다음 버튼이 같이 올라와 텍스트필드를 막는 부분을 아래 코드로 셋팅하여 다음 버튼이 가려지게 함
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        binding = FragmentJoinBinding.inflate(inflater)

        settingToolBar()
        settingViewPagerAdapter()

        return binding.root
    }

    private fun settingToolBar(){
        with(binding.toolbarJoin){
            title = "회원가입"
            setNavigationIcon(R.drawable.arrow_back_24px)
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

        viewPagerAdapter.addFragments(fragmentList)

        with(binding){
            // 어댑터 설정
            viewPagerJoin.adapter = viewPagerAdapter
            // 전환 방향
            viewPagerJoin.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            // 터치로 스크롤 막기
            viewPagerJoin.isUserInputEnabled = false

            // 프로그래스바 설정
            progressBarJoin.max = viewPagerAdapter.itemCount

            viewPagerJoin.registerOnPageChangeCallback(
                object: ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        binding.progressBarJoin.progress = position + 1
                    }
                }
            )

            // 다음 버튼 클릭 시 다음 화면으로 넘어가기
            buttonJoinNext.setOnClickListener {

                // 화면별로 유효성 검사 먼저 하고
                when(viewPagerJoin.currentItem){
                    // 이메일, 비밀번호 화면
                    0 -> {
                        val step1 = viewPagerAdapter.createFragment(0) as JoinStep1Fragment
                        // 유효성 검사
                        val validation = step1.validate()
                        if(!validation) return@setOnClickListener
                        // 응답값
                        val email = step1.getJoinUserEmail()
                        val password = step1.getJoinUserPassword()
                        Log.d("JoinFragment", "email : $email")
                        Log.d("JoinFragment", "password : $password")
                    }
                    // 이름, 전화번호 인증 화면
                    1 -> {
                        val step2 = viewPagerAdapter.createFragment(1) as JoinStep2Fragment
                        // 유효성 검사
                        val validation = step2.validate()
                        if(!validation) return@setOnClickListener
                        // 응답값
                        val name = step2.getUserName()
                        val phoneNumber = step2.getUserPhone()
                        Log.d("JoinFragment", "name : $name")
                        Log.d("JoinFragment", "phoneNumber : $phoneNumber")

                        // 중복 계정 여부 확인
                        val isDup = phoneNumber == "010-1234-5678"
                        if(isDup){
                            // 중복 확인 프래그먼트로 이동
                            replaceFragment(Bundle())
                            return@setOnClickListener
                        }
                    }
                    // 관심 분야 선택 화면
                    2 -> {
                        val step3 = viewPagerAdapter.createFragment(2) as JoinStep3Fragment
                        // 유효성 검사
                        val validation = step3.validate()
                        if(!validation) return@setOnClickListener
                        // 응답값
                        val interest = step3.getInterests()
                        Log.d("JoinFragment", "interest : $interest")
                    }
                }
                // 다음 페이지로 이동
                viewPagerJoin.currentItem += 1
            }
        }

    }

    private fun replaceFragment(bundle: Bundle){
        // 추후 수정
        val supportFragmentManager = parentFragmentManager.beginTransaction()
        val newFragment = JoinDuplicateFragment()
        newFragment.arguments = bundle
        supportFragmentManager.replace(R.id.containerMain, newFragment)
            .addToBackStack("JoinDuplicateFragment")
        supportFragmentManager.commit()
    }
}