package com.cristobal.accenturetrackerexample.utils

import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.hideSoftKeyboard(inputMethodManager: InputMethodManager) {
    val view = when (this) {
        is Fragment -> requireView().rootView
        is FragmentActivity -> currentFocus
        else -> throw NoWhenBranchMatchedException()
    }

    view?.hideSoftKeyboard(inputMethodManager)
}