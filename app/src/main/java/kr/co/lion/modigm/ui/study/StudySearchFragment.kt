package kr.co.lion.modigm.ui.study

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudySearchBinding
import kr.co.lion.modigm.databinding.RowStudyMyBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.adapter.StudySearchAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication

class StudySearchFragment : Fragment(R.layout.fragment_study_search) {

    private var _binding: FragmentStudySearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var rowbinding: RowStudyMyBinding

    // 뷰모델
    private val viewModel: StudyViewModel by viewModels()

    private val currentUserUid = ModigmApplication.prefs.getUserData("currentUserData")?.userUid ?: Firebase.auth.currentUser?.uid ?: ""

    private val studySearchAdapter: StudySearchAdapter = StudySearchAdapter(
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
                        rowbinding.imageViewStudyMyFavorite.setImageResource(R.drawable.icon_favorite_full_24px)
                        rowbinding.imageViewStudyMyFavorite.setColorFilter(Color.parseColor("#D73333"))
                    } else {
                        rowbinding.imageViewStudyMyFavorite.setImageResource(R.drawable.icon_favorite_24px)
                        rowbinding.imageViewStudyMyFavorite.clearColorFilter()
                    }
                }
            }
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStudySearchBinding.bind(view)
        rowbinding = RowStudyMyBinding.inflate(layoutInflater)

        initView(binding)

        viewModel.studyStateTrueDataList.observe(viewLifecycleOwner, Observer { studyList ->
            studySearchAdapter.updateData(studyList)
        })
    }

    private fun initView(binding: FragmentStudySearchBinding) {

        with(binding) {
            // RecyclerView 설정
            recyclerViewStudySearch.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewStudySearch.adapter = studySearchAdapter

            toolbarStudySearch.setNavigationOnClickListener{
                parentFragmentManager.popBackStack()
            }

            // init SearchView
            searchView.isSubmitButtonEnabled = true

            val searchTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as TextView
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
