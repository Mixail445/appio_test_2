package com.example.appio_test_2.utils

import android.app.AlertDialog
import androidx.fragment.app.Fragment


fun Fragment.showDialog(
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    onNegativeClick: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(requireContext())
    builder.setTitle(title)
    builder.setMessage(message)

    builder.setPositiveButton(positiveButtonText) { _, _ ->
        onPositiveClick?.invoke()
    }

    negativeButtonText?.let {
        builder.setNegativeButton(it) { dialog, _ ->
            onNegativeClick?.invoke()
            dialog.dismiss()
        }
    }

    builder.create().show()
}