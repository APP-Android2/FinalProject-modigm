package kr.co.lion.modigm.ui.study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyMyBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.study.adapter.StudyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName

class StudyMyFragment : VBBaseFragment<FragmentStudyMyBinding>(FragmentStudyMyBinding::inflate) {

    // 뷰모델
    private val viewModel: StudyViewModel by activityViewModels()

    // 태그
    private val logTag by lazy { StudyMyFragment::class.simpleName }

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

    // 스크롤 리스너 인터페이스
    private var scrollListener: OnRecyclerViewScrollListener? = null

    // --------------------------------- LC START ---------------------------------

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 스크롤 리스너 인터페이스를 구현한 부모 프래그먼트(StudyFragment)에 연결
        if (parentFragment is OnRecyclerViewScrollListener) {
            scrollListener = parentFragment as OnRecyclerViewScrollListener
        } else {
            throw RuntimeException("$context or parentFragment must implement OnRecyclerViewScrollListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
        observeViewModel()
        viewModel.getMyStudyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    override fun onDetach() {
        super.onDetach()
        scrollListener = null
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {
            // 필터 버튼
            imageViewStudyMyFilter.setOnClickListener {
                // 필터 및 정렬 화면으로 이동
                val filterSortFragment = FilterSortFragment().apply {
                    arguments = Bundle().apply {
                        putString("filterWhere", logTag)
                    }
                }

                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, filterSortFragment)
                    addToBackStack(FragmentName.FILTER_SORT.str)
                }
            }

            // 리사이클러뷰
            recyclerViewStudyMy.apply {
                adapter = studyAdapter
                layoutManager = LinearLayoutManager(requireActivity())

                // 리사이클러뷰 스크롤 리스너
                addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    // 리사이클러뷰 스크롤 시 호출되는 메서드
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        scrollListener?.onRecyclerViewScrolled(dy)
                    }

                    // 리사이클러뷰 스크롤 상태 변경 시 호출되는 메서드
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        scrollListener?.onRecyclerViewScrollStateChanged(newState)
                    }
                })
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

            // 쓸어내려 새로고침 기능
            with(swipeRefreshLayoutStudyMy){
                setOnRefreshListener {
                    viewModel.refreshMyStudyData()
                }
            }
        }
    }

    private fun observeViewModel() {

        // 스와이프 리프레시 로딩 상태 관찰
        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayoutStudyMy.isRefreshing = isRefreshing
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding){
                if (isLoading) {
                    Log.d(logTag, "로딩중")
                    progressBarStudyMy.visibility = View.VISIBLE
                } else {
                    Log.d(logTag, "로딩완료")
                    progressBarStudyMy.visibility = View.GONE
                }
            }
        }

        // 필터 적용 여부를 관찰하여, 필터가 적용된 경우와 그렇지 않은 경우를 구분
        viewModel.isFilterApplied.observe(viewLifecycleOwner) { isFilterApplied ->
            if (isFilterApplied) {
                viewModel.filteredMyStudyData.observe(viewLifecycleOwner) { studyList ->
                    studyAdapter.updateData(studyList)
                    Log.d(logTag, "필터 적용된 스터디 목록 업데이트: ${studyList.size} 개")
                }
            } else {
                // 내 스터디 데이터 관찰 (필터링이 없을 때)
                viewModel.myStudyData.observe(viewLifecycleOwner) { studyList ->
                    studyAdapter.updateData(studyList)
                    Log.d(logTag, "내 스터디 목록 업데이트: ${studyList.size} 개")
                }
            }
        }

        // 좋아요 상태 관찰
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // 좋아요 상태가 변경되었을 때 특정 항목 업데이트
            studyAdapter.updateItem(isFavorite.first, isFavorite.second)
        }

        // 내 스터디 목록 오류 관찰
        viewModel.myStudyError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                Log.e(logTag, "내 스터디 목록 오류 발생", e)
                showStudyErrorDialog(e)
            }
        }

        // 좋아요 오류 관찰
        viewModel.isFavoriteError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                Log.e(logTag, "좋아요 오류 발생", e)
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
}
