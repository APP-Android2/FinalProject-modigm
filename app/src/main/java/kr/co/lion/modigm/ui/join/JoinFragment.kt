package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinBinding
import kr.co.lion.modigm.ui.join.adapter.JoinViewPagerAdapter
import kr.co.lion.modigm.ui.join.vm.JoinStep1ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinStep2ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinStep3ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinViewModel
import kr.co.lion.modigm.ui.study.StudyFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding

    private val viewModel: JoinViewModel by viewModels()
    private val viewModelStep1: JoinStep1ViewModel by activityViewModels()
    private val viewModelStep2: JoinStep2ViewModel by activityViewModels()
    private val viewModelStep3: JoinStep3ViewModel by activityViewModels()

    private val joinType: JoinType? by lazy {
        JoinType.getType(arguments?.getString("joinType")?:"")
    }

    private val customToken: String? by lazy {
        arguments?.getString("customToken")
    }
//    var joinType = "email"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentJoinBinding.inflate(inflater)

        settingToolBar()
        observePhoneAuth()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingViewPagerAdapter()

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
        // 회원가입을 완료하지 않고 화면을 이탈한 경우 이미 등록되어있던 Auth 정보를 삭제한다.
        if(!viewModel.joinCompleted.value!!){
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteCurrentUser()
            }
        }
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
    private fun showCancelJoinDialog() {
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

        when(joinType){
            // 이메일로 회원가입할 때
            JoinType.EMAIL -> {
                viewPagerAdapter.addFragments(
                    arrayListOf(
                        JoinStep1Fragment(),
                        JoinStep2Fragment(),
                        JoinStep3Fragment()
                    )
                )
            }
            // SNS계정으로 회원가입할 때
            else -> {
                viewPagerAdapter.addFragments(
                    arrayListOf(
                        JoinStep2Fragment(),
                        JoinStep3Fragment()
                    )
                )
            }
        }

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

                when(joinType){
                    // 이메일로 회원가입할 때
                    JoinType.EMAIL -> {
                        when(viewPagerJoin.currentItem){
                            // 이메일, 비밀번호 화면
                            0 -> step1Process()
                            // 이름, 전화번호 인증 화면
                            1 -> step2Process()
                            // 관심 분야 선택 화면
                            2 -> step3Process()
                        }
                    }
                    // SNS계정으로 회원가입할 때
                    else -> {
                        when(viewPagerJoin.currentItem){
                            // 이름, 전화번호 인증 화면
                            0 -> step2Process()
                            // 관심 분야 선택 화면
                            1 -> step3Process()
                        }
                    }
                }

            }
        }

    }

    private fun step1Process(){
        // 유효성 검사
        val validation = viewModelStep1.validate()
        if(!validation) return

        // 응답 받은 이메일, 비밀번호
        viewModel.setEmailAndPw(
            viewModelStep1.userEmail.value.toString(),
            viewModelStep1.userPassword.value.toString()
        )

        lifecycleScope.launch {
            // 처음 화면인 경우
            if(viewModel.verifiedEmail.isEmpty()
                // 다음 화면으로 넘어갔다가 다시 돌아와서 이메일을 변경한 경우
                || (viewModelStep1.userEmail.value != viewModel.verifiedEmail && viewModel.verifiedEmail.isNotEmpty())
                ){
                if(viewModelStep1.userEmail.value != viewModel.verifiedEmail && viewModel.verifiedEmail.isNotEmpty()){
                    // 다음 화면으로 넘어갔다가 다시 돌아와서 이메일을 변경한 경우에는 기존에 등록한 이메일 계정을 삭제
                    viewModel.deleteCurrentUser()
                }
                // 계정 중복 확인
                val isDup = viewModel.createEmailUser()
                if(isDup.isNotEmpty()){
                    viewModelStep1.emailValidation.value = isDup
                    return@launch
                }
            }
            // 다음 화면으로 이동
            binding.viewPagerJoin.currentItem += 1
        }
    }

    private fun step2Process(){
        // 유효성 검사
        val validation = viewModelStep2.validate()
        if(!validation) return
        // 응답 받은 이름, 전화번호
        viewModel.setUserNameAndPhoneNumber(
            viewModelStep2.userName.value.toString(),
            viewModelStep2.userPhone.value.toString()
        )

        lifecycleScope.launch {
            // 뒤로가기로 돌아왔을 때 이미 인증된 상태인 경우에는 바로 다음페이지로 넘어갈 수 있음
            if(viewModel.phoneVerification.value==true){
                binding.viewPagerJoin.currentItem += 1
                return@launch
            }

            val result = viewModelStep2.createPhoneUser()
            if(result.isEmpty()){
                viewModelStep2.credential.value?.let { viewModel.setPhoneCredential(it) }
                viewModel.setPhoneVerificated(true)
            }else{
                viewModel.setPhoneVerificated(false)
            }
            if(!viewModel.phoneVerification.value!! && result=="이미 해당 번호로 가입한 계정이 있습니다."){
                viewModel.alreadyRegisteredUserEmail = viewModelStep2.alreadyRegisteredUserEmail
                viewModel.alreadyRegisteredUserProvider = viewModelStep2.alreadyRegisteredUserProvider
                viewModel.isPhoneAlreadyRegistered.value = true
            }
        }
    }

    // 번호 인증 옵저버
    private fun observePhoneAuth(){
        // 인증이 확인 되었을 때
        viewModel.phoneVerification.observe(viewLifecycleOwner){
            if(it){
                // 인증이 되었으면 다음으로 이동
                binding.viewPagerJoin.currentItem += 1
            }
        }

        // 전화번호가 기존에 등록된 번호인 것이 확인되었을 때
        viewModel.isPhoneAlreadyRegistered.observe(viewLifecycleOwner){
            if(it){
                // 중복인 경우 중복 알림 프래그먼트로 이동
                val bundle = Bundle()
                bundle.putString("email", viewModel.alreadyRegisteredUserEmail)
                bundle.putString("provider", viewModel.alreadyRegisteredUserProvider)
                val joinFragment = JoinDuplicateFragment()
                joinFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, joinFragment)
                    .addToBackStack(FragmentName.JOIN_DUPLICATE.str)
                    .commit()
            }
        }
    }

    private fun step3Process(){
        // 유효성 검사
        val validation = viewModelStep3.validate()
        if(!validation) return
        // 응답값
        viewModelStep3.selectedInterestList.value?.let { it1 ->
            viewModel.setInterests(
                it1
            )
        }

        lifecycleScope.launch {
            when(joinType){
                JoinType.EMAIL -> viewModel.completeJoinEmailUser()
                JoinType.KAKAO -> customToken?.let { viewModel.completeJoinSnsUser(it) }
                else -> {}
            }
        }

        // 회원가입 완료 시 다음 화면으로 이동
        viewModel.joinCompleted.observe(viewLifecycleOwner){
            if(it){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, StudyFragment())
                    .commit()
            }
        }
    }

}