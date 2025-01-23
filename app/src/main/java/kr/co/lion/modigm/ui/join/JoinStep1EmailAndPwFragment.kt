package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.databinding.FragmentJoinStep1EmailAndPwBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.component.JoinStep1EmailAndPwScreen
import kr.co.lion.modigm.ui.join.vm.JoinStep1EmailAndPwViewModel

class JoinStep1EmailAndPwFragment : VBBaseFragment<FragmentJoinStep1EmailAndPwBinding>(FragmentJoinStep1EmailAndPwBinding::inflate) {

    private val joinStep1EmailAndPwViewModel: JoinStep1EmailAndPwViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        binding.composeViewJoinStep1EmailAndPw.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                JoinStep1EmailAndPwScreen(joinStep1EmailAndPwViewModel)
            }
        }
        return binding.root
    }



}