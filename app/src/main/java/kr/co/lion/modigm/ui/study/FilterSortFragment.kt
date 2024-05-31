package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFilterSortBinding
import kr.co.lion.modigm.util.FragmentName

class FilterSortFragment : Fragment(R.layout.fragment_filter_sort) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentFilterSortBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFilterSortBinding) {

        // 바인딩
        with(binding){

            // 툴바
            with(toolbarFilter){

                // 뒤로가기 내비게이션 아이콘 클릭 시
                setNavigationOnClickListener{
                    parentFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str, 0)
                }
            }

        }
    }
}