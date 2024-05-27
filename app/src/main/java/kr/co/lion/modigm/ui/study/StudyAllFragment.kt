package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyAllBinding
import kr.co.lion.modigm.ui.login.OtherLoginFragment
import kr.co.lion.modigm.ui.study.adapter.StudyAllAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName


class StudyAllFragment : Fragment() {

    private lateinit var binding: FragmentStudyAllBinding
    private val viewModel: StudyViewModel by viewModels()


    val studyAllAdapter: StudyAllAdapter = StudyAllAdapter(
        rowClickListener = {

        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // 바인딩
        binding = FragmentStudyAllBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
    }

    // 초기 뷰 세팅
    fun initView(){

        with(binding){


            // 필터 버튼
            with(imageViewStudyAllFilter){
                // 클릭 시
                setOnClickListener {
                    // 필터 및 정렬 화면으로 이동
                    val supportFragmentManager = parentFragmentManager.beginTransaction()
                    supportFragmentManager.replace(R.id.containerMain, FilterSortFragment())
                        .addToBackStack(FragmentName.FILTER_SORT.str)
                        .commit()
                }
            }


            // 리사이클러뷰
            with(recyclerViewStudyAll) {
                // 리사이클러뷰 어답터


                adapter = studyAllAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())
            }

        }
    }
}