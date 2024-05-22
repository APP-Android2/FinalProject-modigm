package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.MainFragmentName

class ChatFragment : Fragment() {

    lateinit var fragmentChatBinding: FragmentChatBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatBinding = FragmentChatBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // 채팅 - (툴바, ViewPager) 세팅
        settingToolbar()
        viewPagerActiviation()

        return fragmentChatBinding.root
    }

    // 툴바 세팅
    fun settingToolbar() {
        fragmentChatBinding.apply {
            toolbarChat.apply {
                // 오른쪽 툴바 버튼(Search)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        // 검색 클릭 시
                        R.id.chat_toolbar_search -> {
                            Log.d("test1234", "검색 버튼 클릭")
                        }
                    }
                    true
                }
            }
        }
    }

    // ViewPager 설정
    private fun viewPagerActiviation(){
        fragmentChatBinding.apply {
            // 1. 페이지 데이터를 로드
            val list = listOf(ChatGroupFragment(), ChatOnetoOneFragment())
            // 2. Adapter 생성
            val pagerAdapter = FragmentPagerAdapter(list, mainActivity)
            // 3. Adapater와 Pager연결
            viewPagerChat.adapter = pagerAdapter
            // 4. 탭 메뉴의 갯수만큼 제목을 목록으로 생성
            val titles = listOf("참여 중", "1:1")
            // 5. 탭 레이아웃과 뷰페이저 연결
            TabLayoutMediator(tabsChat, viewPagerChat) { tab, position ->
                tab.text = titles.get(position)
            }.attach()

            // ViewPager 드래그 비활성화
            viewPagerChat.isUserInputEnabled = false
        }
    }

    private inner class FragmentPagerAdapter(val fragmentList: List<Fragment>, fragmentActivity: FragmentActivity):
        FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList.get(position)
        }
    }
}