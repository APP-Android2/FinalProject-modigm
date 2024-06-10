package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailApplyMemberBinding
import kr.co.lion.modigm.databinding.FragmentDetailMemberBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.detail.adapter.DetailApplyMembersAdapter
import kr.co.lion.modigm.ui.detail.adapter.DetailJoinMembersAdapter
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel

class DetailApplyMemberFragment : Fragment() {

    lateinit var binding: FragmentDetailApplyMemberBinding
    private val viewModel: DetailViewModel by activityViewModels()
    private lateinit var adapter: DetailApplyMembersAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailApplyMemberBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

        adapter = DetailApplyMembersAdapter(viewModel,currentUserId,studyIdx)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.applyMembers.observe(viewLifecycleOwner) { members ->
            Log.d("DetailApplyMemberFragment", "Observed members: $members")
            adapter.submitList(members)
        }

        viewModel.loadApplyMembers(studyIdx)


    }

    fun setupRecyclerView() {
        binding.recyclerviewDetailApply.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewDetailApply.adapter = adapter
    }
}