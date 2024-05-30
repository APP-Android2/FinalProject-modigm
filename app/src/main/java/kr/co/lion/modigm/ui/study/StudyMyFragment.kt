package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyMyBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudyMyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName


class StudyMyFragment : Fragment() {

    // 바인딩
    private lateinit var binding: FragmentStudyMyBinding

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

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.containerMain, detailFragment)
                .addToBackStack(null) // 뒤로가기 버튼으로 이전 상태로 돌아갈 수 있도록
                .commit()
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // 바인딩
        binding = FragmentStudyMyBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
        observeData()
    }

    // 초기 뷰 세팅
    fun initView(){

        with(binding){


            // 필터 버튼
            with(imageViewStudyMyFilter){
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
            with(recyclerViewStudyMy) {
                // 리사이클러뷰 어답터


                adapter = studyMyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())
            }
        }
    }

    fun observeData() {
        // 데이터 변경 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.studyMyDataList.observe(viewLifecycleOwner) { studyList ->
                studyMyAdapter.updateData(studyList)
            }
        }
    }
}