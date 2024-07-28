package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudySearchBinding
import kr.co.lion.modigm.ui.BaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudySearchAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName

class StudySearchFragment : BaseFragment<FragmentStudySearchBinding>(FragmentStudySearchBinding::inflate) {

    // 뷰모델
    private val viewModel: StudyViewModel by viewModels()

    // 어답터
    private val studySearchAdapter: StudySearchAdapter by lazy {
        StudySearchAdapter(
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

        initView()

        viewModel.allStudyData.observe(viewLifecycleOwner, Observer { studyList ->
            studySearchAdapter.updateData(studyList)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC END ---------------------------------

    private fun initView() {

        with(binding) {
            // RecyclerView 설정
            recyclerViewStudySearch.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewStudySearch.adapter = studySearchAdapter

            toolbarStudySearch.setNavigationOnClickListener{
                parentFragmentManager.popBackStack()
            }

            // init SearchView
            searchView.isSubmitButtonEnabled = true

            val searchTextView: TextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
            TextViewCompat.setTextAppearance(searchTextView, R.style.ChipTextStyle)

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        studySearchAdapter.filter(it)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        studySearchAdapter.filter(it)
                    }
                    return true
                }
            })
        }
    }
}
