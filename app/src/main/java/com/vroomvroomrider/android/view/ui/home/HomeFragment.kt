package com.vroomvroomrider.android.view.ui.home

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.data.model.order.StatusEnum
import com.vroomvroomrider.android.databinding.FragmentHomeBinding
import com.vroomvroomrider.android.utils.Utils.safeNavigate
import com.vroomvroomrider.android.view.resource.Resource
import com.vroomvroomrider.android.view.ui.base.BaseFragment
import com.vroomvroomrider.android.view.ui.common.ClickType
import com.vroomvroomrider.android.view.ui.home.adapter.OrderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {

    private val adapter by lazy { OrderAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            } else {
                if (user.pickedOrder != null) {
                    orderViewModel.getOrder(user.pickedOrder)
                } else {
                    orderViewModel.startJobs()
                    orderViewModel.getOrders()
                }
            }
        }

        binding.orderRv.adapter = adapter
        observeOrders()
        observeOrder()
        shouldBackToTopObserver()

        adapter.onOrderPicked = {
            if (orderViewModel.pickedOrder == null) {
                orderViewModel.pickedOrder = it
                orderViewModel.pickOrder(it)
            }
        }

        binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            mainViewModel.setIsHomeScroll(scrollY > SCROLL_THRESHOLD)
        }
    }

    private fun navigateToCurrentStatus(order: Order) {
        when (order.status.label) {
            StatusEnum.ACCEPTED.label -> {
                findNavController().safeNavigate(
                    HomeFragmentDirections.actionHomeFragmentToCollectFragment(order)
                )
            }
            StatusEnum.PURCHASED.label -> {
                findNavController().safeNavigate(
                    HomeFragmentDirections.actionHomeFragmentToNavigationFragment(order)
                )
            }
            StatusEnum.DELIVERED.label -> Unit
            else -> Unit
        }
    }

    private fun observeOrders() {
        orderViewModel.orders.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    adapter.submitList(response.data)
                }
                is Resource.Error -> {

                }
            }
        }
    }

    private fun observeOrder() {
        orderViewModel.order.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    val order = response.data
                    val userId = userViewModel.user.value?.id.orEmpty()
                    userViewModel.updatePickedOrder(userId, order.id)
                    navigateToCurrentStatus(order)
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    adapter.notifyDataSetChanged()
                    dialog.show(
                        getString(R.string.network_error),
                        response.exception.message ?: getString(R.string.general_error_message),
                        getString(R.string.cancel),
                        getString(R.string.ok),
                        isButtonLeftVisible = false,
                        isCancellable = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                orderViewModel.getOrders()
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun shouldBackToTopObserver() {
        mainViewModel.shouldBackToTop.observe(viewLifecycleOwner) { shouldBackToTop ->
            if (shouldBackToTop) {
                binding.nestedScrollView.apply {
                    scrollBy(0, 1)
                    ObjectAnimator.ofInt(
                        this,
                        "scrollY",
                        binding.root.top
                    ).setDuration(400).start()
                }
                mainViewModel.setShouldBackToTop(false)
            }
        }
    }

    override fun onDestroyView() {
        orderViewModel.cancelJobs()
        orderViewModel.order.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    companion object {
        const val SCROLL_THRESHOLD = 500
    }
}