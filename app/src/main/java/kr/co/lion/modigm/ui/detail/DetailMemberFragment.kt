package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailJoinMemberBinding
import kr.co.lion.modigm.databinding.FragmentDetailMemberBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.adapter.DetailViewPagerAdapter
import kr.co.lion.modigm.util.FragmentName

class DetailMemberFragment : VBBaseFragment<FragmentDetailMemberBinding>(FragmentDetailMemberBinding::inflate) {

    // 현재 선택된 스터디 idx 번호를 담을 변수
    var studyIdx = 0
    var studyTitle = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달된 studyIdx 값을 저장
        studyIdx = arguments?.getInt("studyIdx") ?: 0
        studyTitle = arguments?.getString("studyTitle") ?:""

        setupViewPagerAndTabs()
        setupToolbar()

    }

    fun setupViewPagerAndTabs() {
        val adapter = DetailViewPagerAdapter(this, studyIdx,studyTitle)
        binding.viewPagerDetail.adapter = adapter

        TabLayoutMediator(binding.tabLayoutDetail, binding.viewPagerDetail) { tab, position ->
            tab.text = when (position) {
                0 -> "참여 중"
                1 -> "대기 중"
                else -> null
            }
        }.attach()
    }
    fun setupToolbar() {
        binding.apply {
            toolBarDetailMember.apply {
                title = "멤버 목록"
                //네비게이션
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

        }
    }

}