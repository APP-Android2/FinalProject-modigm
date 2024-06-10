package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyAllBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudyAllAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName


class StudyAllFragment : Fragment(R.layout.fragment_study_all) {


    // 뷰모델
    private val viewModel: StudyViewModel by viewModels()

    // 어답터
    private val studyAllAdapter: StudyAllAdapter = StudyAllAdapter(
        // 최초 리스트
        emptyList(),

        // 항목 클릭 시
        rowClickListener = { studyIdx ->

            // DetailFragment로 이동
            val detailFragment = DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("studyIdx", studyIdx)
                }
            }

            requireActivity().supportFragmentManager.commit {
                replace(R.id.containerMain, detailFragment)
                addToBackStack(null)
            }

        }
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentStudyAllBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)
        observeData()
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentStudyAllBinding) {

        with(binding){


            // 필터 버튼
            with(imageViewStudyAllFilter){
                // 클릭 시
                setOnClickListener {
                    // 필터 및 정렬 화면으로 이동
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.containerMain, FilterSortFragment())
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
    private fun observeData() {
        // 데이터 변경 관찰
        viewModel.studyStateTrueDataList.observe(viewLifecycleOwner) { studyList ->
            studyAllAdapter.updateData(studyList)
        }
    }
}