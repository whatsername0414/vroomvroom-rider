package com.vroomvroomrider.android.view.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.databinding.FragmentLoginBinding
import com.vroomvroomrider.android.utils.Utils.hideSoftKeyboard
import com.vroomvroomrider.android.utils.Utils.isEmailValid
import com.vroomvroomrider.android.view.resource.Resource
import com.vroomvroomrider.android.view.ui.base.BaseFragment
import com.vroomvroomrider.android.view.ui.common.ClickType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.apply {
            setupToolbar()
            navigationIcon = null
        }

        observeNewLoggedInUser()
        observeToken()
        observeRegisterUser()

        val getSignInWithGoogle = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                authViewModel.googleSignIn(result.data)
            }
        }

        binding.registerTv.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnGoogle.setOnClickListener {
            getSignInWithGoogle.launch(authViewModel.signInIntent)
        }

        binding.loginBtn.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val emailAddress = binding.loginEmailInputEditText.text.toString()
            val password = binding.loginPasswordInputEditText.text.toString()
            if (emailAddress.isEmailValid()) {
                authViewModel.logInWithEmailAndPassword(emailAddress, password)
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_email_address)
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
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    authViewModel.logoutUser {
                        if (it) {
                            dialog.show(
                                getString(R.string.auth_failed),
                                response.exception.message.orEmpty(),
                                getString(R.string.cancel),
                                getString(R.string.ok),
                                isButtonLeftVisible = false,
                                isCancellable = false
                            ) { type ->
                                when (type) {
                                    ClickType.POSITIVE -> dialog.dismiss()
                                    ClickType.NEGATIVE -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeNewLoggedInUser() {
        authViewModel.newLoggedInUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                }
                is Resource.Success -> {
                    authViewModel.saveIdToken()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.auth_failed),
                        response.exception.message.orEmpty(),
                        getString(R.string.cancel),
                        getString(R.string.retry),
                        isButtonLeftVisible = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> dialog.dismiss()
                            ClickType.NEGATIVE -> dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

}