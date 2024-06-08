package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentEditProfileBinding
import kr.co.lion.modigm.ui.profile.adapter.LinkAddAdapter
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.Interest
import kr.co.lion.modigm.util.JoinType

class EditProfileFragment : Fragment() {
    lateinit var fragmentEditProfileBinding: FragmentEditProfileBinding
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    // 어댑터 선언
    private lateinit var linkAddAdapter: LinkAddAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentEditProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentEditProfileBinding.editProfileViewModel = editProfileViewModel
        fragmentEditProfileBinding.lifecycleOwner = this

        firebaseAuth = Firebase.auth
        user = firebaseAuth.currentUser!!

        return fragmentEditProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setupToolbar()
        setupUserInfo()
        setupRecyclerViewLink()

        observeData()
    }

    private fun setupToolbar() {
        fragmentEditProfileBinding.apply {
            toolbarEditProfile.apply {
                // title
                title = "프로필 수정"

                // 뒤로 가기
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack(FragmentName.EDIT_PROFILE.str, 0)
                }
            }
        }
    }

    private fun setupUserInfo() {
        // 데이터베이스로부터 데이터를 불러와 뷰모델에 담기
        editProfileViewModel.loadUserData(user, requireContext(), fragmentEditProfileBinding.imageProfilePic)
    }

    private fun setupRecyclerViewLink() {
        // 리사이클러뷰 어댑터와 뷰모델을 함께 초기화
        linkAddAdapter = LinkAddAdapter(emptyList(), requireContext(), editProfileViewModel)
        fragmentEditProfileBinding.apply {
            recyclerViewEditProfileLink.apply {
                adapter = linkAddAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    fun observeData() {
        // 데이터 변경 관찰
        // 로그인 방식
        editProfileViewModel.editProfileProvider.observe(viewLifecycleOwner) { provider ->
            when (provider) {
                JoinType.KAKAO -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "카카오로 로그인된 계정입니다."
                JoinType.GITHUB -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "깃허브로 로그인된 계정입니다."
                JoinType.EMAIL -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "이메일로 로그인된 계정입니다."
                JoinType.ERROR -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "이메일로 로그인된 계정입니다."
            }
        }
        // 관심 분야 chipGroup
        editProfileViewModel.editProfileInterestList.observe(viewLifecycleOwner) { list ->
            // 기존 칩들 제거
            fragmentEditProfileBinding.chipGroupProfile.removeAllViews()

            // 리스트가 변경될 때마다 for 문을 사용하여 아이템을 처리
            for (interestNum in list) {
                // 아이템 처리 코드
                fragmentEditProfileBinding.chipGroupProfile.addView(Chip(context).apply {
                    // chip 텍스트 설정: 저장되어 있는 숫자로부터 enum 클래스를 불러오고 저장된 str 보여주기
                    text = Interest.fromNum(interestNum)!!.str
                    // 자동 padding 없애기
                    setEnsureMinTouchTargetSize(false)
                    // 배경 흰색으로 지정
                    setChipBackgroundColorResource(android.R.color.white)
                    // 클릭 불가
                    isClickable = false
                    // chip에서 X 버튼 보이게 하기
                    isCloseIconVisible = true
                    // X버튼 누르면 chip 없어지게 하기
                    setOnCloseIconClickListener {
                        fragmentEditProfileBinding.chipGroupProfile.removeView(this)

                        // ViewModel의 리스트에서 해당 항목 삭제
                        val currentList = editProfileViewModel.editProfileInterestList.value?.toMutableList()
                        currentList?.remove(interestNum)
                        editProfileViewModel.editProfileInterestList.value = currentList
                    }
                })
            }
            // 마지막 칩은 칩을 추가하는 버튼으로 사용
            fragmentEditProfileBinding.chipGroupProfile.addView(Chip(context).apply {
                // chip 텍스트 설정
                text = "+"
                // 자동 padding 없애기
                setEnsureMinTouchTargetSize(false)
                // 배경 흰색으로 지정
                setChipBackgroundColorResource(android.R.color.white)
                // 클릭하면 바텀시트 올라옴
                setOnClickListener {
                    val bottomSheet = InterestBottomSheetFragment()
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                }
            })
        }

        // 링크 리스트
        editProfileViewModel.editProfileLinkList.observe(viewLifecycleOwner) { linkList ->
            linkAddAdapter.updateData(linkList)
        }
    }
}