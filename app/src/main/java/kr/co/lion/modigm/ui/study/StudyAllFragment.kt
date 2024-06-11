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
import kr.co.lion.modigm.databinding.FragmentStudyAllBinding
import kr.co.lion.modigm.databinding.RowStudyAllBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudyAllAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication

class StudyAllFragment : Fragment(R.layout.fragment_study_all) {

    private lateinit var rowbinding: RowStudyAllBinding

    // 뷰모델
    private val viewModel: StudyViewModel by activityViewModels()

    private val currentUserUid = ModigmApplication.prefs.getUserData("currentUserData")?.userUid ?: Firebase.auth.currentUser?.uid ?: ""

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
                addToBackStack(FragmentName.DETAIL.str)
            }

        },
        likeClickListener = { studyIdx ->
            viewModel.viewModelScope.launch {
                viewModel.toggleLike(currentUserUid, studyIdx)
                viewModel.isLiked.observe(viewLifecycleOwner) { isLiked ->
                    if (isLiked) {
                        rowbinding.imageViewStudyAllFavorite.setImageResource(R.drawable.icon_favorite_full_24px)
                        rowbinding.imageViewStudyAllFavorite.setColorFilter(Color.parseColor("#D73333"))
                    } else {
                        rowbinding.imageViewStudyAllFavorite.setImageResource(R.drawable.icon_favorite_24px)
                        rowbinding.imageViewStudyAllFavorite.clearColorFilter()
                    }
                }
            }
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentStudyAllBinding.bind(view)
        rowbinding = RowStudyAllBinding.inflate(layoutInflater)

        // 초기 뷰 세팅
        initView(binding)
        observeData()
        Log.d("StudyAllFragment", "onViewCreated 호출됨")
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentStudyAllBinding) {

        with(binding) {
            // 필터 버튼
            with(imageViewStudyAllFilter) {
                // 클릭 시
                setOnClickListener {
                    // 필터 및 정렬 화면으로 이동
                    requireActivity().supportFragmentManager.commit {
                        add(R.id.containerMain, FilterSortFragment())
                        addToBackStack(FragmentName.FILTER_SORT.str)
                    }
                }
            }

            // 리사이클러뷰
            with(recyclerViewStudyAll) {
                // 리사이클러뷰 어답터
                adapter = studyAllAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())
            }

            with(searchBarStudyAll) {
                setOnClickListener {
                    requireActivity().supportFragmentManager.commit {
                        add(R.id.containerMain, StudySearchFragment())
                        addToBackStack(FragmentName.STUDY_SEARCH.str)
                    }
                }
            }
        }
    }

    private fun observeData() {
        // 필터링된 데이터 관찰
        viewModel.filteredStudyList.observe(viewLifecycleOwner) { studyList ->
            studyAllAdapter.updateData(studyList)
            Log.d("StudyAllFragment", "필터링된 전체 스터디 목록 업데이트: ${studyList.size} 개, 데이터: $studyList")
        }

        // 전체 데이터 관찰 (필터링이 없을 때)
        viewModel.studyStateTrueDataList.observe(viewLifecycleOwner) { studyList ->
            studyAllAdapter.updateData(studyList)
            Log.d("StudyAllFragment", "전체 스터디 목록 업데이트: ${studyList.size} 개")
        }
    }
}
