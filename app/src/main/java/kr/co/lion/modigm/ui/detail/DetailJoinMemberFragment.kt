package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.databinding.FragmentDetailJoinMemberBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.detail.adapter.DetailJoinMembersAdapter

class DetailJoinMemberFragment : Fragment() {

    lateinit var fragmentDetailJoinMemberBinding: FragmentDetailJoinMemberBinding

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentDetailJoinMemberBinding = FragmentDetailJoinMemberBinding.inflate(layoutInflater)
        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

        return fragmentDetailJoinMemberBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

    }

    fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        fragmentDetailJoinMemberBinding.recyclerviewDetailJoin.layoutManager = layoutManager

    }


}