package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.databinding.FragmentDetailJoinMemberBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.detail.adapter.DetailJoinMembersAdapter
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel

class DetailJoinMemberFragment : Fragment() {

    lateinit var binding: FragmentDetailJoinMemberBinding
    private val viewModel: DetailViewModel by activityViewModels()
    private lateinit var adapter: DetailJoinMembersAdapter

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailJoinMemberBinding.inflate(layoutInflater)
        adapter = DetailJoinMembersAdapter()  // adapter 초기화
        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.loadStudyUids(studyIdx)

        viewModel.studyUids.observe(viewLifecycleOwner) { uids ->
            viewModel.loadUserDetails(uids)
            uids.forEach { uid ->
                Log.d("DetailJoinMemberFragment", "User UID: $uid")
            }
        }


        viewModel.userDetails.observe(viewLifecycleOwner) { userDetails ->
            userDetails?.let {
                adapter.submitList(it)
            }
        }

    }

    fun setupRecyclerView() {
        adapter = DetailJoinMembersAdapter()
        binding.recyclerviewDetailJoin.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewDetailJoin.adapter = adapter
    }


}