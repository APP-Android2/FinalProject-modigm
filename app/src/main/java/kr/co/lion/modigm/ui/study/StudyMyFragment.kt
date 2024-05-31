package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyMyBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudyMyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName


class StudyMyFragment : Fragment(R.layout.fragment_study_my) {

    // 뷰모델
    private val viewModel: StudyViewModel by viewModels()

    // 어답터
    private val studyMyAdapter: StudyMyAdapter = StudyMyAdapter(
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
                addToBackStack(FragmentName.DETAIL.str)
            }

        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentStudyMyBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)
        observeData()
    }

    // 초기 뷰 세팅
    fun initView(binding: FragmentStudyMyBinding) {

        with(binding){


            // 필터 버튼
            with(imageViewStudyMyFilter){
                // 클릭 시
                setOnClickListener {
                    // 필터 및 정렬 화면으로 이동
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.containerMain, FilterSortFragment())
                        addToBackStack(FragmentName.FILTER_SORT.str)
                    }
                }
            }

            // 리사이클러뷰
            with(recyclerViewStudyMy) {
                // 리사이클러뷰 어답터


                adapter = studyMyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())
            }
        }
    }

    private fun observeData() {
        // 데이터 변경 관찰
        viewModel.studyMyDataList.observe(viewLifecycleOwner) { studyList ->
            studyMyAdapter.updateData(studyList)
        }
    }
}