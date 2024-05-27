package kr.co.lion.modigm.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.util.FragmentName

class DetailFragment : Fragment() {

    lateinit var fragmentDetailBinding: FragmentDetailBinding

    lateinit var mainActivity: MainActivity

    lateinit var detailViewModel: DetailViewModel

    private var isPopupShown = false

    // 프래그먼트의 뷰가 생성될 때 호출
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentDetailBinding = FragmentDetailBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity


        return fragmentDetailBinding.root

    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 설정
        settingToolbar()

        // 앱바 스크롤
        setupAppBarScrollListener()

        // 좋아요 버튼
        setupLikeButtonListener()

        // 메뉴 설정
        setupPopupMenu()

        // 모집 상태 변경
        setupStatePopup()
    }

    // 옵션 메뉴 아이템 선택 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 뒤로가기 버튼
            android.R.id.home -> {
                mainActivity.removeFragment(FragmentName.DETAIL)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 툴바 설정
    fun settingToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(fragmentDetailBinding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 콜랩싱 툴바의 타이틀 설정
        fragmentDetailBinding.apply {
            collapsingToolbarDetail.apply {
                title = fragmentDetailBinding.textviewDetailTilte.text.toString()
            }
        }
    }

    // 앱바 스크롤 설정
    fun setupAppBarScrollListener() {
        fragmentDetailBinding.appBarDetail.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // 전체 스크롤 범위를 계산
            val scrollRange = appBarLayout.totalScrollRange
            // 뒤로가기 아이콘
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_back_24px)
            // 스크롤 최대일 때 아이콘 색상 변경
            if (scrollRange + verticalOffset == 0) {
                drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
            }
            // 네비게이션 아이콘 업데이트
            fragmentDetailBinding.toolbar.navigationIcon = drawable
        })
    }

    // 좋아요 버튼 설정
    fun setupLikeButtonListener() {
        fragmentDetailBinding.buttonDetailLike.setOnClickListener {
            toggleLikeButton()
        }
    }

    // 팝업 메뉴 설정
    fun setupPopupMenu(){
        fragmentDetailBinding.imageViewDetailMenu.setOnClickListener {
            showPopupWindow(it)
        }
    }

    // 좋아요 버튼 상태 토글
    fun toggleLikeButton() {
        // 현재 설정된 이미지 리소스 ID를 확인하고 상태를 토글
        val currentIconResId = fragmentDetailBinding.buttonDetailLike.tag as? Int ?: R.drawable.icon_favorite_24px
        if (currentIconResId == R.drawable.icon_favorite_24px) {
            // 좋아요 채워진 아이콘으로 변경
            fragmentDetailBinding.buttonDetailLike.setImageResource(R.drawable.icon_favorite_full_24px)
            // 상태 태그 업데이트
            fragmentDetailBinding.buttonDetailLike.tag = R.drawable.icon_favorite_full_24px

            // 새 색상을 사용하여 틴트 적용
            fragmentDetailBinding.buttonDetailLike.setColorFilter(Color.parseColor("#D73333"))
        } else {
            // 기본 아이콘으로 변경
            fragmentDetailBinding.buttonDetailLike.setImageResource(R.drawable.icon_favorite_24px)
            // 상태 태그 업데이트
            fragmentDetailBinding.buttonDetailLike.tag = R.drawable.icon_favorite_24px

            // 틴트 제거 (원래 아이콘 색상으로 복원)
            fragmentDetailBinding.buttonDetailLike.clearColorFilter()
        }
    }

    // custom 팝업 메뉴
    fun showPopupWindow(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupView = layoutInflater.inflate(R.layout.custom_detail_popup_menu_item,null)

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        popupWindow.isOutsideTouchable = true
        // 팝업 윈도우 위치 조정 (간접적 마진 효과)
        popupWindow.showAsDropDown(anchorView, -50, 0)
        // 팝업 윈도우 크기 조정
        popupWindow.width = 500  // 너비를 500px로 설정
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 따라 자동 조절


        // 각 메뉴 아이템에 대한 클릭 리스너 설정
        // 멤버목록
        popupView.findViewById<TextView>(R.id.menuItem1).setOnClickListener {
            // 화면이동 로직 추가
            mainActivity.replaceFragment(FragmentName.DETAIL_MEMBER, true, false, null)
            popupWindow.dismiss()
        }

        // 글 편집
        popupView.findViewById<TextView>(R.id.menuItem2).setOnClickListener {
            // 화면이동 로직 추가
            mainActivity.replaceFragment(FragmentName.DETAIL_EDIT, true,false,null)
            popupWindow.dismiss()
        }

        // 글 삭제
        popupView.findViewById<TextView>(R.id.menuItem3).setOnClickListener {
            // 화면이동 로직 추가
            popupWindow.dismiss()
        }

        // 팝업 윈도우 표시
        popupWindow.showAsDropDown(anchorView)
    }

    fun setupStatePopup() {
        val textViewState = fragmentDetailBinding.textViewDetailState

        textViewState.setOnClickListener { view ->
            if (!isPopupShown) {
                showStatePopup(view as TextView)  // TextView를 명시적으로 전달
                textViewState.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_expand_less_24px,0)
                isPopupShown = true
            } else {
                isPopupShown = false
                textViewState.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_expand_more_24px,0)
            }
        }
    }

    fun showStatePopup(anchorView: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val popupView = layoutInflater.inflate(R.layout.custom_detail_popup_menu_stateitem, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true

        // 팝업 뷰를 측정하여 실제 높이를 계산
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val x = location[0]
        val density = resources.displayMetrics.density
        val extraOffset = (10 * density).toInt()
        val y = location[1] - popupHeight - extraOffset

        popupWindow.setOnDismissListener {
            // 팝업이 닫힐 때 이미지를 원래대로 복구
            isPopupShown = false
            fragmentDetailBinding.textViewDetailState.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_expand_more_24px,0)
        }

        // 팝업 윈도우 표시
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)

        // 팝업 메뉴 아이템의 클릭 리스너 설정
        popupView.findViewById<TextView>(R.id.textViewDetailState1).setOnClickListener {
            fragmentDetailBinding.textViewDetailState.text = (it as TextView).text

            // buttonDetailApply의 배경색과 텍스트 색상을 원래대로 복원
            val button = fragmentDetailBinding.buttonDetailApply
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pointColor))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            // button 클릭 활성화
            button.isEnabled = true

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.textViewDetailState2).setOnClickListener {
            fragmentDetailBinding.textViewDetailState.text = (it as TextView).text

            // buttonDetailApply의 배경색과 텍스트 색상 변경
            val button = fragmentDetailBinding.buttonDetailApply
            button.setBackgroundColor(Color.parseColor("#777777"))  // 배경색을 회색으로 설정
            button.setTextColor(Color.BLACK)  // 텍스트 색상을 검정색으로 설정

            // button 클릭 비활성화
            button.isEnabled = false

            popupWindow.dismiss()
        }
    }
}