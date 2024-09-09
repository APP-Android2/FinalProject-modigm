package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudySearchBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.study.adapter.StudySearchAdapter
import kr.co.lion.modigm.ui.study.vm.StudySearchViewModel
import kr.co.lion.modigm.util.FragmentName

class StudySearchFragment : VBBaseFragment<FragmentStudySearchBinding>(FragmentStudySearchBinding::inflate) {

    // 뷰모델
    private val viewModel: StudySearchViewModel by viewModels()

    // 태그
    private val logTag by lazy { StudySearchFragment::class.simpleName }

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
            favoriteClickListener = { studyIdx, _ ->
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
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
        backButton()

    }

    override fun onResume() {
        super.onResume()
        viewModel.getSearchStudyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC END ---------------------------------

    private fun initView() {

        with(binding) {
            // RecyclerView 설정
            recyclerViewStudySearch.apply {
                layoutManager = LinearLayoutManager(requireContext())

                adapter = studySearchAdapter

                // 스크롤 리스너 설정
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    // 스크롤이 변경될 때 호출
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        // 스크롤 중일 때 Glide 이미지 로딩을 일시 중지
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Glide.with(recyclerView.context).resumeRequests()
                        } else {
                            Glide.with(recyclerView.context).pauseRequests()
                        }
                    }
                })
            }


            toolbarStudySearch.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            // 서치뷰 설정
            searchView.isSubmitButtonEnabled = true

            val searchTextView: TextView =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text)
            TextViewCompat.setTextAppearance(searchTextView, R.style.ChipTextStyle)

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        studySearchAdapter.search(it)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }
    }

    private fun observeViewModel() {
        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding){
                if (isLoading) {
                    progressBarStudySearch.visibility = View.VISIBLE
                } else {
                    progressBarStudySearch.visibility = View.GONE
                }
            }
        }

        // 검색 데이터 관찰 (필터링이 없을 때)
        viewModel.searchStudyData.observe(viewLifecycleOwner) { studyList ->
            studySearchAdapter.updateData(studyList)
        }

        // 검색 스터디 목록 오류 관찰
        viewModel.searchStudyError.observe(viewLifecycleOwner) { e ->
            Log.e(logTag, "전체 스터디 목록 오류 발생", e)
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

        studyErrorDialog(message)
    }

    // 오류 다이얼로그 표시
    private fun studyErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        with(dialog){
            setTitle("오류")
            setMessage(message)
            setPositiveButton("확인") {
                dismiss()
            }
            show()
        }

    }

    private fun backButton(){
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }
}
