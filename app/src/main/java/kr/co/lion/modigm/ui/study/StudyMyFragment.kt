package kr.co.lion.modigm.ui.study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.imageview.ShapeableImageView
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

    // 뱃지 객체를 클래스 변수로 관리
    private var badgeDrawable: BadgeDrawable? = null

    // --------------------------------- Lifecycle Start ---------------------------------

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
        // 뷰모델 관찰자 등록
        observeViewModel()

    }

    override fun onResume() {
        super.onResume()
        // 내 스터디 데이터 요청
        viewModel.getMyStudyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ViewModel 데이터 초기화
        viewModel.clearData()
    }

    override fun onDetach() {
        super.onDetach()
        scrollListener = null
    }

    // --------------------------------- Lifecycle End ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {
            // 필터 버튼
            with(imageViewStudyMyFilter) {

                setOnClickListener {

                    // 필터 및 정렬 화면으로 이동
                    val filterSortFragment = FilterSortFragment().apply {
                        arguments = Bundle().apply {
                            putString("filterWhere", FragmentName.STUDY_MY.str)
                        }
                    }

                    requireActivity().supportFragmentManager.commit {
                        add(R.id.containerMain, filterSortFragment)
                        addToBackStack(FragmentName.FILTER_SORT.str)
                    }
                }
            }

            // 리사이클러뷰
            with(recyclerViewStudyMy) {
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
                        // 스크롤 중일 때 Glide 이미지 로딩을 일시 중지
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Glide.with(recyclerView.context).resumeRequests()
                        } else {
                            Glide.with(recyclerView.context).pauseRequests()
                        }

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
        with(binding) {
            // 스와이프 리프레시 로딩 상태 관찰
            viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
                swipeRefreshLayoutStudyMy.isRefreshing = isRefreshing
            }

            // 로딩 상태 관찰
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

                if (isLoading) {
                    Log.d(logTag, "로딩중")
                    progressBarStudyMy.visibility = View.VISIBLE
                } else {
                    Log.d(logTag, "로딩완료")
                    progressBarStudyMy.visibility = View.GONE
                }

            }

            // 필터 적용 여부를 관찰하여, 필터가 적용된 경우와 그렇지 않은 경우를 구분
            viewModel.isFilterApplied.observe(viewLifecycleOwner) { isFilterApplied ->
                // 필터가 적용된 경우
                if (isFilterApplied) {

                    // 필터 아이콘 UI 변경 (뱃지)
                    setBadge(imageViewStudyMyFilter, R.color.redColor)
                    val pointColor = ContextCompat.getColor(requireContext(), R.color.pointColor)
                    imageViewStudyMyFilter.setColorFilter(pointColor)

                    // 필터 적용된 스터디 목록을 관찰
                    viewModel.filteredMyStudyData.observe(viewLifecycleOwner) { studyList ->
                        studyAdapter.updateData(studyList)
                        Log.d(logTag, "필터 적용된 스터디 목록 업데이트: ${studyList.size} 개")
                    }
                // 필터가 적용되지 않은 경우
                } else {
                    // 필터 뱃지 제거
                    removeBadge(imageViewStudyMyFilter)
                    // 필터 아이콘 색상 원래대로 변경
                    imageViewStudyMyFilter.clearColorFilter()
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


    // 뱃지 설정
    @OptIn(ExperimentalBadgeUtils::class)
    private fun setBadge(icon: ShapeableImageView, badgeColor: Int) {
        val setBadgeColor = ContextCompat.getColor(requireContext(), badgeColor)
        if (badgeDrawable == null) {
            badgeDrawable = BadgeDrawable.create(requireContext())
        }
        badgeDrawable?.let { badge ->
            badge.backgroundColor = setBadgeColor
            BadgeUtils.attachBadgeDrawable(badge, icon)
        }
    }

    // 뱃지 제거 메서드
    @OptIn(ExperimentalBadgeUtils::class)
    private fun removeBadge(icon: ShapeableImageView) {
        badgeDrawable?.let { badge ->
            BadgeUtils.detachBadgeDrawable(badge, icon)
        }
    }
}
