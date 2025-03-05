package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.ui.common.ModigmTheme
import kr.co.lion.modigm.util.FragmentName

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    // onCreateView에서 초기화
    var userIdx: Int? = null
    var isBottomNavi: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        userIdx = arguments?.getInt("userIdx")
        isBottomNavi = arguments?.getBoolean("isBottomNavi")

        return ComposeView(requireContext()).apply {
            setContent {
                ModigmTheme {
                    ProfileScreen(
                        viewModel = viewModel,
                        changeToSettingsFragment = { changeToSettingsFragment() },
                        changeToLinkWebView = { link -> changeToLinkWebView(link) },
                        changeToDetailFragment = { studyIdx -> changeToDetailFragment(studyIdx) }

                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupUserInfo()
    }

    private fun setupUserInfo() {
        viewModel.profileUserIdx.value = userIdx
        viewModel.loadUserData()
        viewModel.loadUserLinkListData()
        viewModel.loadHostStudyList(userIdx!!)
        viewModel.loadPartStudyList(userIdx!!)
    }

    private fun changeToSettingsFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            val settingsFragment = SettingsFragment()

            // Fragment 교체
            requireActivity().supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                replace(R.id.containerMain, settingsFragment)
                addToBackStack(FragmentName.SETTINGS.str)
            }
        }
    }

    private fun changeToLinkWebView(link: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // bundle 에 필요한 정보를 담는다
            val bundle = Bundle()
            bundle.putString("link", link)

            // 이동할 프래그먼트로 bundle을 넘긴다
            val profileWebFragment = ProfileWebFragment()
            profileWebFragment.arguments = bundle

            // Fragment 교체
            requireActivity().supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                replace(R.id.containerMain, profileWebFragment)
                addToBackStack(FragmentName.PROFILE_WEB.str)
            }
        }
    }

    private fun changeToDetailFragment(studyIdx: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val detailFragment = DetailFragment()

            // Bundle 생성 및 현재 사용자 uid 담기
            val bundle = Bundle()
            bundle.putInt("studyIdx", studyIdx)

            // Bundle을 ProfileFragment에 설정
            detailFragment.arguments = bundle

            requireActivity().supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                replace(R.id.containerMain, detailFragment)
                addToBackStack(FragmentName.DETAIL.str)
            }
        }
    }
}

