package com.vroomvroomrider.android.view.ui.home

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ncorti.slidetoact.SlideToActView
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.databinding.FragmentCollectBinding
import com.vroomvroomrider.android.utils.Utils.safeNavigate
import com.vroomvroomrider.android.view.resource.Resource
import com.vroomvroomrider.android.view.ui.base.BaseFragment
import com.vroomvroomrider.android.view.ui.common.ClickType
import com.vroomvroomrider.android.view.ui.home.adapter.OrderProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CollectFragment : BaseFragment<FragmentCollectBinding>(
    FragmentCollectBinding::inflate
) {

    private val adapter by lazy { OrderProductAdapter() }
    private val collected = mutableListOf<String>()
    private val args: CollectFragmentArgs by navArgs()

    private val onSlideCompleteListener = object: SlideToActView.OnSlideCompleteListener {
        override fun onSlideComplete(view: SlideToActView) {
            orderViewModel.purchasedOrder(args.order.id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.apply {
            setupToolbar()
            navigationIcon = null
        }
        binding.sliderLayout.pickOrderSlide.apply {
            text = getString(R.string.collected)
            isSliderEnabled(false)

        }
        binding.orderProductRv.adapter = adapter

        orderViewModel.pickedOrder = null
        observeOrder()
        bindData(args.order)

        adapter.onProductClicked = { id, direction ->
            if (direction == 1) {
                collected.add(id)
            } else {
                collected.remove(id)
            }
            val isCollected = collected.size == args.order.orderDetail.products.size
            binding.sliderLayout.pickOrderSlide.isSliderEnabled(isCollected)
        }

        binding.sliderLayout.pickOrderSlide.onSlideCompleteListener = onSlideCompleteListener

    }

    private fun bindData(order: Order) {
        binding.apply {
            this.order = order
            this.customerInfoLayout.order = order
            orderDetailLayout.visibility = View.VISIBLE
            subTotalTv.text =
                getString(R.string.peso, "%.2f".format(order.orderDetail.totalPrice))
            deliveryFee.text =
                getString(R.string.peso, "%.2f".format(order.orderDetail.deliveryFee))
            val total = order.orderDetail.totalPrice + order.orderDetail.deliveryFee
            totalTv.text = getString(R.string.peso, "%.2f".format(total))
            sliderLayout.root.visibility = View.VISIBLE
        }
        adapter.submitList(order.orderDetail.products)
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
                    navController.safeNavigate(
                        CollectFragmentDirections.actionCollectFragmentToNavigationFragment(order)
                    )
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.network_error),
                        response.exception.message ?: getString(R.string.general_error_message),
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

    private fun SlideToActView.isSliderEnabled(isEnabled: Boolean) {
        val sliderBg = if (isEnabled) R.drawable.bg_gradient_red
        else R.drawable.bg_gradient_gray
        val iconColor = if (isEnabled) R.color.red_a30
        else R.color.gray_7f7
        setBackgroundResource(sliderBg)
        this.iconColor = iconColor
        this.isEnabled = isEnabled
    }

}