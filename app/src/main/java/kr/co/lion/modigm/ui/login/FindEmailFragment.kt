package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailBinding
import kr.co.lion.modigm.ui.login.vm.FindEmailViewModel
import kr.co.lion.modigm.util.FragmentName

class FindEmailFragment : Fragment(R.layout.fragment_find_email) {

    private val viewModel: FindEmailViewModel by viewModels()
    lateinit var binding: FragmentFindEmailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_email, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(binding)
        settingObserver()
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFindEmailBinding) {
        with(binding) {
            // 툴바
            with(toolbarFindEmail) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(buttonFindEmailNext) {
                setOnClickListener {
                    isClickable=false
                    // 유효성 검사
                    val validate = viewModel!!.validateInput()
                    if(!validate) {
                        isClickable=true
                        return@setOnClickListener
                    }
                    // 입력한 이름이 계정 정보와 일치하는지 확인
                    viewModel!!.checkNameAndPhone()
                    isClickable=true
                }
            }

            // 번호 입력 시 자동으로 하이픈을 넣어줌
            textInputEditFindEmailPhone.addTextChangedListener(
                PhoneNumberFormattingTextWatcher()
            )
        }
    }

    private fun settingObserver(){
        // 유효성 검사
        viewModel.nameError.observe(viewLifecycleOwner) {
            binding.textInputEditFindEmailName.error = it
            binding.textInputEditFindEmailName.requestFocus()
        }
        viewModel.phoneError.observe(viewLifecycleOwner) {
            binding.textInputEditFindEmailPhone.error = it
            binding.textInputEditFindEmailPhone.requestFocus()
        }

        // 다음으로 이동
        viewModel.isComplete.observe(viewLifecycleOwner) {
            if(it){
                moveToNext()
                // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
                viewModel.isComplete.value = false
            }
        }
    }

    private fun moveToNext(){
        val fragment = FindEmailAuthFragment().apply {
            arguments = Bundle().apply {
                putString("phone", viewModel.phone.value?:"")
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.containerMain, fragment)
            .addToBackStack(FragmentName.FIND_EMAIL_AUTH.str)
            .commit()
    }


}
