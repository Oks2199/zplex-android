package zechs.zplex.ui.signin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentSignInBinding
import zechs.zplex.utils.ext.focusTelevisionBackButton
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.util.Keyboard

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private val viewModel by viewModels<SignInViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSignInBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.driveSignInConnect.setOnClickListener {
            Keyboard.hide(view)
            viewModel.connect(
                binding.driveSignInClientId.text.toString(),
                binding.driveSignInClientSecret.text.toString()
            )
        }
        binding.driveSignInCancel.setOnClickListener { viewModel.cancel() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.combine(viewModel.isLoggedIn) { state, loggedIn ->
                    state to loggedIn
                }.collect { (state, loggedIn) ->
                    if (state == DriveSignInState.Connected) {
                        findNavController().navigateUp()
                        return@collect
                    }

                    render(binding, state, loggedIn)

                    if (state is DriveSignInState.OpenBrowser) {
                        viewModel.openBrowser { url ->
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, url))
                            } catch (_: ActivityNotFoundException) {
                                viewModel.browserUnavailable()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(binding: FragmentSignInBinding, state: DriveSignInState, loggedIn: Boolean) {
        val canConnect = !loggedIn &&
            (state == DriveSignInState.Ready || state is DriveSignInState.Error)
        val canCancel = state == DriveSignInState.Preparing ||
            state is DriveSignInState.OpenBrowser || state == DriveSignInState.WaitingForGoogle
        val hadConnect = binding.driveSignInConnect.isVisible
        val hadCancel = binding.driveSignInCancel.isVisible

        binding.driveSignInConfiguration.isVisible = canConnect && !viewModel.hasBuiltInClient
        binding.driveSignInConnect.isVisible = canConnect
        binding.driveSignInCancel.isVisible = canCancel
        binding.driveSignInProgress.isVisible = canCancel || state == DriveSignInState.ExchangingCode
        binding.driveSignInStatus.setText(
            when {
                loggedIn -> R.string.drive_sign_in_already_connected
                state is DriveSignInState.Error -> state.message
                state == DriveSignInState.ExchangingCode -> R.string.drive_sign_in_exchanging
                canCancel -> R.string.drive_sign_in_waiting
                else -> R.string.drive_sign_in_instruction
            }
        )

        if (binding.root.context.isTelevision) {
            when {
                canConnect && !hadConnect -> binding.driveSignInConnect.requestFocus()
                canCancel && !hadCancel -> binding.driveSignInCancel.requestFocus()
                loggedIn -> binding.toolbar.doOnNextLayout { binding.toolbar.focusTelevisionBackButton() }
            }
        }
    }
}
