package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.databinding.FragmentFilterSortBinding
import kr.co.lion.modigm.util.FragmentName

class FilterSortFragment : Fragment() {

    private lateinit var binding : FragmentFilterSortBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // 바인딩
        binding = FragmentFilterSortBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
    }

    // 초기 뷰 세팅
    fun initView(){

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