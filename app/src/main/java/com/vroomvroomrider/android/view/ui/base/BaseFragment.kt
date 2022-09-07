package com.vroomvroomrider.android.view.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.databinding.CommonNoticeLayoutBinding
import com.vroomvroomrider.android.view.ui.common.CommonAlertDialog
import com.vroomvroomrider.android.view.ui.common.LoadingDialog
import com.vroomvroomrider.android.view.viewmodel.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
abstract class BaseFragment<VB: ViewBinding> (
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : Fragment() {

    val mainViewModel by activityViewModels<MainViewModel>()
    val orderViewModel by viewModels<OrderViewModel>()
    val authViewModel by viewModels<AuthViewModel>()
    val userViewModel by viewModels<UserViewModel>()

    val dialog by lazy { CommonAlertDialog(requireActivity()) }
    val loadingDialog by lazy { LoadingDialog(requireActivity()) }

    var onBackPressed: (() -> Unit)? = null

    private var _binding: VB? = null
    val binding: VB
        get() = _binding as VB

    lateinit var navController: NavController
    var prevDestinationId: Int = -1
    private lateinit var snackBar: Snackbar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater.invoke(inflater)
        if (_binding == null)
            throw IllegalArgumentException("Binding cannot be null")
        return binding.root
    }

    fun Toolbar.setupToolbar() {
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        this.setupWithNavController(navController, appBarConfiguration)
        this.setNavigationIcon(R.drawable.ic_arrow_left)
    }

    fun showShortToast(message: Int) {
        Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
    }

    fun showShortToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun CommonNoticeLayoutBinding.hideNotice() {
        this.root.visibility = View.GONE
    }

    fun CommonNoticeLayoutBinding.showNotice(
        image: Int,
        title: Int,
        message: Int,
        messageParam: String?,
        btnTitle: Int?,
        isButtonVisible: Boolean = true,
        listener: () -> Unit
    ) {
        this.apply {
            this.root.visibility = View.VISIBLE
            noticeImage.setImageResource(image)
            noticeTitle.text = getString(title)
            noticeMessage.text = getString(message, messageParam)
            if (isButtonVisible) {
                btnNotice.apply {
                    text = getString(btnTitle ?: -1)
                    setOnClickListener {
                        listener.invoke()
                    }
                }
            } else {
                btnNotice.visibility = View.GONE
            }

        }
    }

    fun CommonNoticeLayoutBinding.showNetworkError(listener: () -> Unit) {
        showNotice(
            R.drawable.ic_no_wifi,
            R.string.network_error,
            R.string.network_error_message,
            null,
            R.string.retry,
            true,
            listener
        )
    }

    fun CommonNoticeLayoutBinding.showEmptyOrder(listener: () -> Unit) {
        showNotice(
            R.drawable.ic_invoice,
            R.string.empty_order,
            R.string.empty_order_message,
            null,
            R.string.refresh,
            true,
            listener
        )
    }

    override fun onResume() {
        super.onResume()
        if (this::navController.isInitialized) {
            prevDestinationId = navController.previousBackStackEntry?.destination?.id ?: -1
        }
    }
}