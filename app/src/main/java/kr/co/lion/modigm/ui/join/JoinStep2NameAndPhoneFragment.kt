package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.lion.modigm.ui.join.vm.JoinStep2NameAndPhoneViewModel

@AndroidEntryPoint
class JoinStep2NameAndPhoneFragment : Fragment() {

    private val joinStep2NameAndPhoneViewModel: JoinStep2NameAndPhoneViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                JoinStep2NameAndPhoneScreen(
                    joinStep2NameAndPhoneViewModel,
                    requireActivity()
                )
            }
        }
    }

}