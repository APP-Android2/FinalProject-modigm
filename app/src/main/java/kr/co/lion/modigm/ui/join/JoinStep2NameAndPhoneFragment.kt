package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                    inputName = joinStep2NameAndPhoneViewModel.userInputName.collectAsStateWithLifecycle().value,
                    nameValidationMessage = joinStep2NameAndPhoneViewModel.userInputNameValidation.collectAsStateWithLifecycle().value,
                    inputPhoneNumber = joinStep2NameAndPhoneViewModel.userInputPhone.collectAsStateWithLifecycle().value,
                    phoneValidationMessage = joinStep2NameAndPhoneViewModel.userInputPhoneValidation.collectAsStateWithLifecycle().value,
                    inputPhoneAuthCode = joinStep2NameAndPhoneViewModel.userInputSmsCode.collectAsStateWithLifecycle().value,
                    phoneAuthCodeValidationMessage = joinStep2NameAndPhoneViewModel.userInputSmsCodeValidation.collectAsStateWithLifecycle().value,
                    phoneAuthButtonText = joinStep2NameAndPhoneViewModel.phoneAuthButtonText.collectAsStateWithLifecycle().value,
                    isPhoneAuthCodeSent = joinStep2NameAndPhoneViewModel.isPhoneAuthCodeSent.collectAsStateWithLifecycle().value,
                    isPhoneAuthExpired = joinStep2NameAndPhoneViewModel.isPhoneAuthExpired.collectAsStateWithLifecycle().value,
                    setUserInputName = {
                        joinStep2NameAndPhoneViewModel.setUserInputName(it)
                    },
                    setUserInputPhone = {
                        joinStep2NameAndPhoneViewModel.setUserInputPhone(it)
                    },
                    phoneAuthButtonClickEvent = {
                        joinStep2NameAndPhoneViewModel.phoneAuthButtonClickEvent(requireActivity())
                    },
                    setUserInputSmsCode = {
                        joinStep2NameAndPhoneViewModel.setUserInputSmsCode(it)
                    },
                    cancelPhoneAuth = {
                        joinStep2NameAndPhoneViewModel.cancelPhoneAuthTimer()
                        joinStep2NameAndPhoneViewModel.stopSmsReceiver(requireActivity())
                    },
                )
            }
        }
    }

}