package kr.co.lion.modigm.ui.favorite

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFavoriteBinding
import kr.co.lion.modigm.databinding.RowFavoriteBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.favorite.adapter.FavoriteAdapter
import kr.co.lion.modigm.ui.favorite.vm.FavoriteViewModel
import kr.co.lion.modigm.util.FragmentName

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private lateinit var rowBinding: RowFavoriteBinding

    // 뷰모델
    private val viewModel: FavoriteViewModel by activityViewModels()

    private val currentUserUid = 1

    // 어답터
    private val favoriteAdapter: FavoriteAdapter = FavoriteAdapter(
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

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFavoriteBinding.bind(view)
        rowBinding = RowFavoriteBinding.inflate(layoutInflater)

        // 초기 뷰 세팅
        initView(binding)
        viewModel.getMyFavoriteStudyDataList(1)
        observeData(binding)
        Log.d("StudyAllFragment", "onViewCreated 호출됨")


    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ViewModel 데이터 초기화
        viewModel.clearData()
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFavoriteBinding) {

        with(binding) {

            // 리사이클러뷰
            with(recyclerviewFavorite) {
                // 리사이클러뷰 어답터
                adapter = favoriteAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())

            }
        }
    }


    private fun observeData(binding: FragmentFavoriteBinding) {
//        // 필터링된 데이터 관찰
//        viewModel.filteredStudyList.observe(viewLifecycleOwner) { studyList ->
//            studyAllAdapter.updateData(studyList)
//            Log.d("StudyAllFragment", "필터링된 전체 스터디 목록 업데이트: ${studyList.size} 개, 데이터: $studyList")
//        }


        // 전체 데이터 관찰 (필터링이 없을 때)
        viewModel.favoritedStudyList.observe(viewLifecycleOwner) { studyList ->
            if (studyList.isNotEmpty()) {
                binding.recyclerviewFavorite.visibility = View.VISIBLE
                binding.blankLayoutFavorite.visibility = View.GONE
                favoriteAdapter.updateData(studyList)
            } else {
                binding.recyclerviewFavorite.visibility = View.GONE
                binding.blankLayoutFavorite.visibility = View.VISIBLE
            }

            favoriteAdapter.updateData(studyList)
            Log.d("StudyAllFragment", "전체 스터디 목록 업데이트: ${studyList.size} 개")
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // 좋아요 상태가 변경되었을 때 특정 항목 업데이트
            favoriteAdapter.updateItem(isFavorite.first, isFavorite.second)
        }
    }
}
