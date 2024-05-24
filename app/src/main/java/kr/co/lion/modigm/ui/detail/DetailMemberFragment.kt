package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailMemberBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.detail.adapter.DetailViewPagerAdapter
import kr.co.lion.modigm.util.FragmentName

class DetailMemberFragment : Fragment() {

    lateinit var fragmentDetailMemberBinding: FragmentDetailMemberBinding

    lateinit var mainActivity: MainActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentDetailMemberBinding = FragmentDetailMemberBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity


        return fragmentDetailMemberBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPagerAndTabs()
        setupToolbar()

    }

    fun setupViewPagerAndTabs() {
        val adapter = DetailViewPagerAdapter(this)
        fragmentDetailMemberBinding.viewPagerDetail.adapter = adapter

        TabLayoutMediator(fragmentDetailMemberBinding.tabLayoutDetail, fragmentDetailMemberBinding.viewPagerDetail) { tab, position ->
            tab.text = when (position) {
                0 -> "참여 중"
                1 -> "대기 중"
                else -> null
            }
        }.attach()
    }
    fun setupToolbar() {
        fragmentDetailMemberBinding.apply {
            toolBarDetailMember.apply {
                title = "멤버 목록"
                //네비게이션
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
//                    mainActivity.removeFragment(FragmentName.DETAIL_MEMBER)
                }
            }

        }
    }

}