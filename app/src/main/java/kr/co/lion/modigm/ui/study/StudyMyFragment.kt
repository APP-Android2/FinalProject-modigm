package kr.co.lion.modigm.ui.study

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyMyBinding
import kr.co.lion.modigm.databinding.RowStudyBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.ui.write.WriteFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication

class StudyMyFragment : Fragment(R.layout.fragment_study_my) {

    private lateinit var rowbinding: RowStudyBinding

    // 뷰모델
    private val viewModel: StudyViewModel by activityViewModels()

    private val currentUserUid = 1

    // 어답터
    private val studyAdapter: StudyAdapter = StudyAdapter(
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
        },
        favoriteClickListener = { studyIdx ->
            // 현재 접속중인 유저의 userIdx를 전달해야하므로 수정 요망./////////////////////////////////////////////////////////////////////////////////////
            viewModel.toggleFavorite(1, studyIdx)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentStudyMyBinding.bind(view)
        rowbinding = RowStudyBinding.inflate(layoutInflater)

        // 초기 뷰 세팅
        initView(binding)
        viewModel.getMyStudyDataList(1)
        observeData()
        Log.d("StudyMyFragment", "onViewCreated 호출됨")
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentStudyMyBinding) {
        with(binding) {
            // 필터 버튼
            imageViewStudyMyFilter.setOnClickListener {
                // 필터 및 정렬 화면으로 이동
                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, FilterSortFragment())
                    addToBackStack(FragmentName.FILTER_SORT.str)
                }
            }

            // 리사이클러뷰
            recyclerViewStudyMy.apply {
                adapter = studyAdapter
                layoutManager = LinearLayoutManager(requireActivity())
            }

            searchBarStudyMy.setOnClickListener {
                requireActivity().supportFragmentManager.commit {
                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    add(R.id.containerMain, StudySearchFragment())
                    addToBackStack(FragmentName.STUDY_SEARCH.str)
                }
            }

            // FAB 설정
            with(fabStudyWrite) {
                setOnClickListener {
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.containerMain, WriteFragment())
                        addToBackStack(FragmentName.WRITE.str)
                    }
                }
            }
        }
    }

    private fun observeData() {
//        // 필터링된 데이터 관찰
//        viewModel.filteredMyStudyList.observe(viewLifecycleOwner) { studyList ->
//            studyMyAdapter.updateData(studyList)
//            Log.d("StudyMyFragment", "필터링된 내 스터디 목록 업데이트: ${studyList.size} 개, 데이터: $studyList")
//        }

        // 내 스터디 데이터 관찰 (필터링이 없을 때)
        viewModel.myStudyDataList.observe(viewLifecycleOwner) { studyList ->
            studyAdapter.updateData(studyList)
            Log.d("StudyMyFragment", "내 스터디 목록 업데이트: ${studyList.size} 개")
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // 좋아요 상태가 변경되었을 때 특정 항목 업데이트
            studyAdapter.updateItem(isFavorite.first, isFavorite.second)
        }
    }
}
