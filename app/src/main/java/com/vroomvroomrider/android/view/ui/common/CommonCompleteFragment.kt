package com.vroomvroomrider.android.view.ui.common

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.databinding.FragmentCommonCompleteBinding
import com.vroomvroomrider.android.utils.Utils.safeNavigate
import com.vroomvroomrider.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CommonCompleteFragment : BaseFragment<FragmentCommonCompleteBinding>(
    FragmentCommonCompleteBinding::inflate
) {

    private val args: CommonCompleteFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.apply {
            setupToolbar()
            title = args.toolbarTitle
            navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_maroon)
            setNavigationOnClickListener {
                navController.safeNavigate(R.id.action_commonCompleteFragment_to_homeFragment)
            }
        }
        initView()
        onBackPressed()
    }

    private fun initView() {
        binding.apply {
            icon.setImageResource(args.icon)
            titleTv.text = args.title
            descriptionTv.text = args.description
            btnProceed.text = args.buttonTitle
        }
        when (args.type) {
            CompleteType.DELIVERED -> {
                binding.btnProceed.setOnClickListener {
                    navController.safeNavigate(R.id.action_commonCompleteFragment_to_homeFragment)
                }
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (prevDestinationId == R.id.navigationFragment) {
                        navController.safeNavigate(R.id.action_commonCompleteFragment_to_homeFragment)
                    } else {
                        navController.popBackStack()
                    }
                }
            })
    }

}