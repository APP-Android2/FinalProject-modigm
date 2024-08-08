package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfileBinding
import kr.co.lion.modigm.databinding.FragmentProfileStudyBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.profile.adapter.ProfileStudyAdapter
import kr.co.lion.modigm.ui.profile.vm.ProfileStudyViewModel
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.FragmentName

class ProfileStudyFragment : Fragment() {
    lateinit var fragmentProfileStudyBinding: FragmentProfileStudyBinding
    private val profileStudyViewModel: ProfileStudyViewModel by viewModels()

    val studyAdapter: ProfileStudyAdapter = ProfileStudyAdapter(
        // 빈 리스트를 넣어 초기화
        emptyList(),

        // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
        rowClickListener = { studyIdx ->
            viewLifecycleOwner.lifecycleScope.launch {
                val detailFragment = DetailFragment()

                // Bundle 생성 및 현재 사용자 uid 담기
                val bundle = Bundle()
                bundle.putInt("studyIdx", studyIdx)

                // Bundle을 ProfileStudyFragment에 설정
                detailFragment.arguments = bundle

                requireActivity().supportFragmentManager.commit {
                    add(R.id.containerMain, detailFragment)
                    addToBackStack(FragmentName.DETAIL.str)
                }
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentProfileStudyBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_study, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentProfileStudyBinding.profileStudyViewModel = profileStudyViewModel
        fragmentProfileStudyBinding.lifecycleOwner = this

        return fragmentProfileStudyBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setupToolbar()
        setupRecyclerView()
        loadStudyList()
        observeData()
    }

    private fun setupToolbar() {
        fragmentProfileStudyBinding.apply {
            toolbarProfileStudy.apply {
                // title
                title = "스터디 목록"

                // 뒤로 가기
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun loadStudyList() {
        // 진행한 스터디
        if (arguments?.getInt("type") == 1) {
            Log.d("zunione", "${arguments?.getInt("type")}")

            profileStudyViewModel.loadHostStudyList(arguments?.getInt("userIdx")!!)
        } else {
            Log.d("zunione", "${arguments?.getInt("type")}")
            // 참여한 스터디
            profileStudyViewModel.loadPartStudyList(arguments?.getInt("userIdx")!!)
        }

    }

    private fun setupRecyclerView() {
        // 리사이클러뷰 구성
        fragmentProfileStudyBinding.apply {
            recyclerViewProfileStudy.apply {
                // 리사이클러뷰 어댑터
                adapter = studyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    fun observeData() {
        // 진행한 스터디 리스트
        profileStudyViewModel.profileStudyList.observe(viewLifecycleOwner) { profileStudyList ->
            studyAdapter.updateData(profileStudyList)
        }
    }
}