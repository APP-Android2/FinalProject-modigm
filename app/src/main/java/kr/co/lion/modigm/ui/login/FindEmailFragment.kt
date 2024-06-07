package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailBinding

class FindEmailFragment : Fragment(R.layout.fragment_find_email) {

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
            with(binding) {
                buttonFindEmailNext.setOnClickListener {

                }
            }


        }
    }
}