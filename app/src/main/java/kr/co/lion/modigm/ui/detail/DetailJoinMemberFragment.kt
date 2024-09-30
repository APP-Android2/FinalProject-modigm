package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailJoinMemberBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.adapter.DetailJoinMembersAdapter
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication

//참여중인 멤버
class DetailJoinMemberFragment : VBBaseFragment<FragmentDetailJoinMemberBinding>(FragmentDetailJoinMemberBinding::inflate) {

    private val viewModel: DetailViewModel by activityViewModels()
    private lateinit var adapter: DetailJoinMembersAdapter

    var currentUserId = ModigmApplication.prefs.getInt("currentUserIdx", 0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 studyIdx 값
        val studyIdx = arguments?.getInt("studyIdx") ?: 0
        val studyTitle = arguments?.getString("studyTitle") ?:""
        val imageUrl = arguments?.getString("imageUrl") ?:""
        Log.d("DetailJoinMemberFragment", "Received studyIdx: $studyIdx")

        if (studyIdx == 0) {
            Log.e("DetailJoinMemberFragment", "Invalid studyIdx: $studyIdx")
            return  // studyIdx가 0이면 더 이상 진행하지 않도록 한다
        }

        adapter = DetailJoinMembersAdapter(viewModel, currentUserId, studyIdx, studyTitle, imageUrl) { user ->
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
                viewModel.studyMembers.collect { members ->
                    Log.d("DetailJoinMembersAdapter", "Members list size: ${members.size}")
                    adapter.submitList(members)

                    // 새로운 항목이 추가될 때 RecyclerView를 맨 아래로 스크롤
                    if (members.isNotEmpty()) {
                        binding.recyclerviewDetailJoin.scrollToPosition(members.size - 1)
                    }
                }
            }
        }


        viewModel.fetchMembersInfo(studyIdx)

    }

    fun setupRecyclerView() {
        binding.recyclerviewDetailJoin.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewDetailJoin.adapter = adapter
    }

}