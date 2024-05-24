package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailApplyMemberBinding
import kr.co.lion.modigm.databinding.FragmentDetailMemberBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.detail.adapter.DetailApplyMembersAdapter
import kr.co.lion.modigm.ui.detail.adapter.DetailJoinMembersAdapter


class DetailApplyMemberFragment : Fragment() {

    lateinit var fragmentDetailApplyMemberBinding: FragmentDetailApplyMemberBinding

    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentDetailApplyMemberBinding = FragmentDetailApplyMemberBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity


        return fragmentDetailApplyMemberBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

    }

    fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        fragmentDetailApplyMemberBinding.recyclerviewDetailApply.layoutManager = layoutManager

        // 임시 데이터(데이터 있음 확인용)
        val members = listOf(
            Member("홍길동", "열심히 일하는 개발자입니다."),
            Member("김철수", "디자인을 사랑하는 크리에이터입니다."),
            Member("이영희", "프로젝트 매니지먼트가 전문인 매니저입니다.")
        )

        // 임시 데이터(데이터 없음 확인용)
//        val members = listOf<Member>()

        // recyclerview 어댑처 설정
        fragmentDetailApplyMemberBinding.recyclerviewDetailApply.adapter = DetailApplyMembersAdapter(members)

        // 데이터 유무에 따른 뷰 가시성 조정
        if (members.isEmpty()) {
            fragmentDetailApplyMemberBinding.recyclerviewDetailApply.visibility = View.GONE
            fragmentDetailApplyMemberBinding.blankLayoutDetail.visibility = View.VISIBLE
        } else {
            fragmentDetailApplyMemberBinding.recyclerviewDetailApply.visibility = View.VISIBLE
            fragmentDetailApplyMemberBinding.blankLayoutDetail.visibility = View.GONE
        }

        Log.d("DetailJoinMemberFragment", "Adapter set with ${members.size} members.")
    }
}