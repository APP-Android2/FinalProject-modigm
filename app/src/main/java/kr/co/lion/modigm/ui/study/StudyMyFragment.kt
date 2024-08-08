package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyMyBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.study.adapter.StudyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.ui.write.WriteFragment
import kr.co.lion.modigm.util.FragmentName

class StudyMyFragment : VBBaseFragment<FragmentStudyMyBinding>(FragmentStudyMyBinding::inflate) {

    // 뷰모델
    private val viewModel: StudyViewModel by activityViewModels()

    // 어답터
    private val studyAdapter: StudyAdapter by lazy {
        StudyAdapter(
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
            favoriteClickListener = { studyIdx, currentState ->
                viewModel.changeFavoriteState(studyIdx, currentState)
            }
        )
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
        viewModel.getMyStudyData()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
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
                    setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
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

    private fun observeViewModel() {
//        // 필터링된 데이터 관찰
//        viewModel.filteredMyStudyList.observe(viewLifecycleOwner) { studyList ->
//            studyMyAdapter.updateData(studyList)
//            Log.d("StudyMyFragment", "필터링된 내 스터디 목록 업데이트: ${studyList.size} 개, 데이터: $studyList")
//        }

        // 내 스터디 데이터 관찰 (필터링이 없을 때)
        viewModel.myStudyData.observe(viewLifecycleOwner) { studyList ->
            studyAdapter.updateData(studyList)
            Log.d(tag, "내 스터디 목록 업데이트: ${studyList.size} 개")
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // 좋아요 상태가 변경되었을 때 특정 항목 업데이트
            studyAdapter.updateItem(isFavorite.first, isFavorite.second)
        }

        if (!viewModel.myStudyError.hasObservers()) {
            viewModel.allStudyError.observe(viewLifecycleOwner) { e ->
                Log.e(tag, "내 스터디 목록 오류 발생", e)
                if (e != null) {
                    showStudyErrorDialog(e)
                }
            }
        }

        if (!viewModel.isFavoriteError.hasObservers()) {
            viewModel.isFavoriteError.observe(viewLifecycleOwner) { e ->
                Log.e(tag, "좋아요 오류 발생", e)
                if (e != null) {
                    showStudyErrorDialog(e)
                }
            }
        }
    }

    // 스터디 오류 처리 메서드
    private fun showStudyErrorDialog(e: Throwable) {
        val message = if (e.message != null) {
            e.message.toString()
        } else {
            "알 수 없는 오류!"
        }
        studyErrorDialog(message)
    }

    // 오류 다이얼로그 표시
    private fun studyErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        dialog.setTitle("오류")
        dialog.setMessage(message)
        dialog.setPositiveButton("확인") {
            dialog.dismiss()
        }
        dialog.show()
    }
}
