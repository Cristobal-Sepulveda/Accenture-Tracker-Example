package com.cristobal.accenturetrackerexample.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cristobal.accenturetrackerexample.R

fun <U : BaseUseCase<P, T, L>, P, T, L, > ViewModel.execute(
    params: P,
    useCase: U,
    liveData: L
) = useCase.execute(params, liveData, viewModelScope)

fun <T, L : LiveData<T>> FragmentActivity.observe(liveData: L, body: (T?) -> Unit) =
    liveData.observe(this, Observer(body))

fun <T, L : LiveData<T>> Fragment.observe(liveData: L, body: (T?) -> Unit) =
    liveData.observe(viewLifecycleOwner, Observer(body))

fun Fragment.showToastWithStringResource(message: Int){
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun Activity.showToastWithStringResource(message: Int){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.showToastWithString(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.showCustomAlertDialog(
    title: Int,
    message: Int,
    positiveButtonAction: () -> Unit
){
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.accept) { _, _ -> positiveButtonAction() }
        .show()
}
fun View.hideSoftKeyboard(inputMethodManager: InputMethodManager) {
    clearFocus()
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.visible() {
    visibility = View.VISIBLE
}
