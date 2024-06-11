package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailBinding
import kr.co.lion.modigm.ui.login.vm.LoginViewModel

class FindEmailFragment : Fragment(R.layout.fragment_find_email) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFindEmailBinding.bind(view)
        initView(binding)
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
                    val name = textInputEditFindEmailName.text.toString()
                    val phone = textInputEditFindEmailPhone.text.toString()

                    if (name.isNotEmpty() && phone.isNotEmpty()) {


                    } else {
                        // 유효성 검사
                        if (name.isEmpty()) {
                            textInputEditFindEmailName.error = "이름을 입력해주세요."
                        }
                        if (phone.isEmpty()) {
                            textInputEditFindEmailPhone.error = "전화번호를 입력해주세요."
                        }
                    }
                }
            }
        }
    }


}
