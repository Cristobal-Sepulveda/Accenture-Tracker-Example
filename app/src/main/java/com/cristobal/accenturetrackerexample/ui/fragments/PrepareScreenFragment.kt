package com.cristobal.accenturetrackerexample.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cristobal.accenturetrackerexample.R
import com.cristobal.accenturetrackerexample.databinding.FragmentPrepareScreenBinding
import com.cristobal.accenturetrackerexample.domain.domainobjects.UserData
import com.cristobal.accenturetrackerexample.ui.viewmodels.HomeViewModel
import com.cristobal.accenturetrackerexample.utils.observe
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.cristobal.accenturetrackerexample.domain.domainobjects.Result
import com.cristobal.accenturetrackerexample.utils.DRIVER
import com.cristobal.accenturetrackerexample.utils.SUPERVISOR
import com.cristobal.accenturetrackerexample.utils.visible
import com.google.android.material.appbar.MaterialToolbar

class PrepareScreenFragment : Fragment() {

    private lateinit var binding: FragmentPrepareScreenBinding
    private lateinit var viewFlipper: ViewFlipper
    private val viewModel by sharedViewModel<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrepareScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        initObserver()
    }

    private fun initViews() {
        viewFlipper = binding.prepareScreenViewFlipper
    }

    private fun initObserver() {
        with(viewModel) {
            observe(currentUserEmailAndProfileType, ::handleGetUserEmailAndProfileType)
            getUserEmailAndProfileType()
        }
    }

    private fun handleGetUserEmailAndProfileType(result: Result<UserData>?) {
        when (result) {
            is Result.OnLoading -> {
                viewFlipper.displayedChild = Status.LOADING.status
            }

            is Result.OnError -> {
                viewFlipper.displayedChild = Status.ERROR.status
            }

            is Result.OnSuccess -> {
                if (result.value.isUserSupervisor) {
                    requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).apply{
                        title = getString(R.string.toolbar_title, result.value.email, SUPERVISOR)
                        visible()
                    }
                    findNavController().navigate(
                        R.id.action_prepareScreenFragment_to_supervisorFragment
                    )
                } else {
                    requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).apply{
                        title = getString(R.string.toolbar_title, result.value.email, DRIVER)
                        visible()
                    }
                    findNavController().navigate(
                        R.id.action_prepareScreenFragment_to_driverFragment
                    )
                }

            }
            else -> {}
        }
    }

    enum class Status(val status: Int) {
        LOADING(0),
        ERROR(1)
    }
}