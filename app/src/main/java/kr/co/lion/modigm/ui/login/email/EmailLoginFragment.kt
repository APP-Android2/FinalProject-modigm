package kr.co.lion.modigm.ui.login.email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.FindEmailFragment
import kr.co.lion.modigm.ui.login.FindPasswordFragment
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType

class EmailLoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    EmailLoginScreen(
                        navigateToBottomNavi = { navigateToBottomNaviFragment() },
                        navigateToFindEmail = { navigateToFindEmailFragment() },
                        navigateToFindPassword = { navigateToFindPasswordFragment() },
                        navigateToSocialLogin = { navigateToSocialLoginFragment() },
                        navigateToJoin = {navigateToJoinFragment() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    private fun navigateToFindEmailFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, FindEmailFragment())
            addToBackStack(FragmentName.FIND_EMAIL.str)
        }
    }

    private fun navigateToFindPasswordFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, FindPasswordFragment())
            addToBackStack(FragmentName.FIND_PASSWORD.str)
        }
    }

    private fun navigateToSocialLoginFragment() {
        parentFragmentManager.popBackStack()
    }

    private fun navigateToJoinFragment() {
        val bundle = Bundle().apply {
            putString("joinType", JoinType.EMAIL.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

    private fun navigateToBottomNaviFragment() {
        val bundle = Bundle().apply {
            putString("joinType", JoinType.EMAIL.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }
}
