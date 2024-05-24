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

data class Member(
    val name: String,
    val intro: String
)

class DetailJoinMemberFragment : Fragment() {

    lateinit var fragmentDetailJoinMemberBinding: FragmentDetailJoinMemberBinding

    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentDetailJoinMemberBinding = FragmentDetailJoinMemberBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity


        return fragmentDetailJoinMemberBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

    }

    fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        fragmentDetailJoinMemberBinding.recyclerviewDetailJoin.layoutManager = layoutManager
        // 임시 데이터(확인용)
        val members = listOf(
            Member("홍길동", "열심히 일하는 개발자입니다."),
            Member("김철수", "디자인을 사랑하는 크리에이터입니다."),
            Member("이영희", "프로젝트 매니지먼트가 전문인 매니저입니다.")
        )
        fragmentDetailJoinMemberBinding.recyclerviewDetailJoin.adapter = DetailJoinMembersAdapter(members)
        Log.d("DetailJoinMemberFragment", "Adapter set with ${members.size} members.")
    }


}