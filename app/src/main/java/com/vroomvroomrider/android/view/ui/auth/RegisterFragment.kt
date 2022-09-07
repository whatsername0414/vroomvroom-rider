package com.vroomvroomrider.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.databinding.FragmentRegisterBinding
import com.vroomvroomrider.android.utils.Utils.hideSoftKeyboard
import com.vroomvroomrider.android.utils.Utils.isEmailValid
import com.vroomvroomrider.android.utils.Utils.setSafeOnClickListener
import com.vroomvroomrider.android.view.resource.Resource
import com.vroomvroomrider.android.view.ui.base.BaseFragment
import com.vroomvroomrider.android.view.ui.common.ClickType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    FragmentRegisterBinding::inflate
) {

    private var emailAddress: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.apply {
            setupToolbar()
            setNavigationIcon(R.drawable.ic_arrow_left)
        }

        observeOtpSent()
        observeVerified()
        observeNewLogInUser()
        observeToken()
        observeRegisterUser()

        binding.loginTv.setOnClickListener {
            navController.popBackStack()
        }

        binding.btnSendOtp.setSafeOnClickListener {
            requireActivity().hideSoftKeyboard()
            emailAddress = binding.registerEmailInputEditText.text?.toString().orEmpty()
            if (emailAddress.isEmailValid()) {
                loadingDialog.show(getString(R.string.sending))
                authViewModel.generateEmailOtp(emailAddress)
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_email_address)
            }
        }

        binding.apply {
            registerEmailInputEditText.doAfterTextChanged {
                registerOtpInputLayout.visibility = View.GONE
                resendTv.visibility = View.GONE
                btnVerifyOtp.visibility = View.GONE
                btnSendOtp.visibility = View.VISIBLE
            }
        }
        binding.btnVerifyOtp.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val otp = binding.registerOtpInputEditText.text?.toString()
            if (otp.isNullOrBlank()) {
                binding.errorTv.text = getString(R.string.otp_invalid)
                return@setOnClickListener
            }
            authViewModel.verifyEmailOtp(emailAddress, otp)
        }

        binding.registerBtn.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val password = binding.registerPasswordInputEditText.text?.toString().orEmpty()
            val confirmPassword = binding.registerConfirmPasswordInputEditText.text?.toString()
            if (password == confirmPassword) {
                loadingDialog.show(getString(R.string.loading))
                binding.errorTv.visibility = View.GONE
                authViewModel.registerWithEmailAndPassword(emailAddress, password)
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_password)
            }
        }
    }

    private fun observeOtpSent() {
        authViewModel.isOtpSent.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> binding.errorTv.visibility = View.GONE
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    binding.apply {
                        registerOtpInputLayout.isVisible = response.data
                        resendTv.isVisible = response.data
                        btnVerifyOtp.isVisible = response.data
                        btnSendOtp.isVisible = !response.data
                    }

                }
                is Resource.Error -> {
                    binding.errorTv.apply {
                        text = response.exception.message
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun observeVerified() {
        authViewModel.isVerified.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    binding.apply {
                        registerOtpInputLayout.isVisible = !response.data
                        resendTv.isVisible = !response.data
                        btnVerifyOtp.isVisible = !response.data
                        btnSendOtp.isVisible = !response.data
                        registerBtn.isEnabled = response.data
                        registerEmailInputLayout.error = "Verified"
                    }
                }
                is Resource.Error -> {
                    binding.errorTv.text = response.exception.message
                }
            }
        }
    }

    private fun observeToken() {
        authViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                authViewModel.register()
            }
        }
    }

    private fun observeRegisterUser() {
        authViewModel.isRegistered.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.network_error),
                        getString(R.string.unsaved_error),
                        getString(R.string.cancel),
                        getString(R.string.retry),
                        isButtonLeftVisible = false,
                        isCancellable = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                loadingDialog.show(getString(R.string.loading))
                                authViewModel.register()
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun observeNewLogInUser() {
        authViewModel.newLoggedInUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    authViewModel.saveIdToken()
                    binding.errorTv.visibility = View.GONE
                    requireActivity().hideSoftKeyboard()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    binding.errorTv.visibility = View.VISIBLE
                    binding.errorTv.text = result.exception.message
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.resetOtpLiveData()
    }

}