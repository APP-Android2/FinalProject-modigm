package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailApplyMemberBinding
import kr.co.lion.modigm.databinding.FragmentDetailBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.adapter.DetailApplyMembersAdapter
import kr.co.lion.modigm.ui.detail.vm.SqlDetailViewModel
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication

class DetailApplyMemberFragment : VBBaseFragment<FragmentDetailApplyMemberBinding>(FragmentDetailApplyMemberBinding::inflate) {

    private val viewModel: SqlDetailViewModel by activityViewModels()
    private lateinit var adapter: DetailApplyMembersAdapter

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 studyIdx 값
        val studyIdx = arguments?.getInt("studyIdx") ?: 0

        var currentUserId = ModigmApplication.prefs.getInt("currentUserIdx", 0)

        adapter = DetailApplyMembersAdapter(viewModel,currentUserId, studyIdx) { user ->
            val profileFragment = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putInt("userIdx", user.userIdx)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.containerMain, profileFragment)
                .addToBackStack(FragmentName.PROFILE.str)
                .commit()
        }

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.studyRequestMembers.collect { members ->
                    Log.d("DetailApplyMemberFragment", "Members list size: ${members.size}")
                    if (members.isEmpty()) {
                        binding.recyclerviewDetailApply.visibility = View.GONE
                        binding.blankLayoutDetail.visibility = View.VISIBLE
                    } else {
                        binding.recyclerviewDetailApply.visibility = View.VISIBLE
                        binding.blankLayoutDetail.visibility = View.GONE
                        adapter.submitList(members)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        // 승인 결과 처리
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.acceptUserResult.collect { success ->
                if (success) {
                    Log.d("DetailApplyMemberFragment", "User approved successfully")
                    // UI를 갱신하거나 필요한 작업 수행
                } else {
                    Log.d("DetailApplyMemberFragment", "User approval failed")
                }
            }
        }

        viewModel.fetchStudyRequestMembers(studyIdx)

    }

    fun setupRecyclerView() {
        binding.recyclerviewDetailApply.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewDetailApply.adapter = adapter
    }
}