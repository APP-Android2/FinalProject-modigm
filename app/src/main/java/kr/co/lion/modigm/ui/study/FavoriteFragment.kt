package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFavoriteBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.study.adapter.StudyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName

class FavoriteFragment : VBBaseFragment<FragmentFavoriteBinding>(FragmentFavoriteBinding::inflate) {

    // 뷰모델

    private val viewModel: StudyViewModel by viewModels()

    // 어답터
    private val studyAdapter: StudyAdapter by lazy {
        StudyAdapter (
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
            // 좋아요 클릭 시
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
        viewModel.getFavoriteStudyData()
        observeViewModel()
        Log.d("StudyAllFragment", "onViewCreated 호출됨")


    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {

        with(binding) {

            // 리사이클러뷰
            with(recyclerviewFavorite) {
                // 리사이클러뷰 어답터
                adapter = studyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())

            }

        }
    }


    private fun observeViewModel() {
//        // 필터링된 데이터 관찰
//        viewModel.filteredStudyList.observe(viewLifecycleOwner) { studyList ->
//            studyAllAdapter.updateData(studyList)
//            Log.d("StudyAllFragment", "필터링된 전체 스터디 목록 업데이트: ${studyList.size} 개, 데이터: $studyList")
//        }


        // 전체 데이터 관찰 (필터링이 없을 때)
        viewModel.favoritedStudyData.observe(viewLifecycleOwner) { studyList ->
            with(binding) {
                if (studyList.isNotEmpty()) {
                    recyclerviewFavorite.visibility = View.VISIBLE
                    blankLayoutFavorite.visibility = View.GONE
                    studyAdapter.updateData(studyList)

                } else {
                    recyclerviewFavorite.visibility = View.GONE
                    blankLayoutFavorite.visibility = View.VISIBLE
                }
            }

        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // 좋아요 상태가 변경되었을 때 특정 항목 업데이트
            studyAdapter.updateItem(isFavorite.first, isFavorite.second)
        }

        viewModel.favoriteStudyError.observe(viewLifecycleOwner) { e ->
            Log.e("StudyAllFragment", "오류 발생", e)
            if (e != null) {
                showStudyErrorDialog(e)
            }
        }
        viewModel.isFavoriteError.observe(viewLifecycleOwner) { e ->
            Log.e("StudyAllFragment", "오류 발생", e)
            if (e != null) {
                showStudyErrorDialog(e)
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

        showStudyErrorDialog(message)
    }

    // 오류 다이얼로그 표시
    private fun showStudyErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        dialog.setTitle("오류")
        dialog.setMessage(message)
        dialog.setPositiveButton("확인") {
            dialog.dismiss()
        }
        dialog.show()
    }
}
