package kr.co.lion.modigm.ui.join

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.join.adapter.JoinViewPagerAdapter
import kr.co.lion.modigm.ui.join.vm.JoinViewModel
import kr.co.lion.modigm.util.FragmentName

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding

    private val fragmentList : ArrayList<Fragment> by lazy {
        arrayListOf(
            JoinStep1Fragment(),
            JoinStep2Fragment(),
            JoinStep3Fragment()
        )
    }

    private val viewModel: JoinViewModel by viewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 안드로이드 뒤로가기 기능에 뒤로가기 버튼 기능 추가
        lifecycleScope.launch {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if(binding.viewPagerJoin.currentItem!=0){
                    binding.viewPagerJoin.currentItem -= 1
                }else{
                    // JoinFragment 회원가입 종료, 확인 알림창 띄우기?
                    showCancelJoinDialog()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 회원가입 화면을 이탈한 경우 이미 등록되어있던 Auth 정보를 삭제한다.
        viewModel.deleteUserAuth()
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
                    showCancelJoinDialog()
                }
            }
        }
    }

    // 회원가입 취소 다이얼로그
    fun showCancelJoinDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.dialogColor)
            .setTitle("회원가입 취소")
            .setMessage("정말로 회원가입을 취소하시겠습니까?")
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.btnYes).text = "네"
        dialogView.findViewById<TextView>(R.id.btnYes).setOnClickListener {
            parentFragmentManager.popBackStack(FragmentName.JOIN.str, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnNo).text = "아니오"
        dialogView.findViewById<TextView>(R.id.btnNo).setOnClickListener {
            // 아니요 버튼 로직
            dialog.dismiss()
        }
        dialog.show()
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
                        val email = step1.joinStep1ViewModel.userEmail.value
                        val password = step1.joinStep1ViewModel.userPassword.value
                    }
                    // 이름, 전화번호 인증 화면
                    1 -> {
                        val step2 = viewPagerAdapter.createFragment(1) as JoinStep2Fragment
                        // 유효성 검사
                        val validation = step2.validate()
                        if(!validation) return@setOnClickListener
                        // 응답값
                        val name = step2.joinStep2ViewModel.userName.value
                        val phoneNumber = step2.joinStep2ViewModel.userPhone.value

                        // 중복 계정 여부 확인
                        val isDup = phoneNumber == "010-1234-5678"
                        if(isDup){
                            // 중복 확인 프래그먼트로 이동
                            (requireActivity() as MainActivity).replaceFragment(FragmentName.JOIN_DUPLICATE, true, true, null)
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
                        val interest = step3.joinStep3ViewModel.selectedInterestList
                    }
                }
                // 다음 페이지로 이동
                viewPagerJoin.currentItem += 1
            }
        }

    }

}