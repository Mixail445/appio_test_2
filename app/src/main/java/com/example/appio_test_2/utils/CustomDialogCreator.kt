package com.example.appio_test_2.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.appio_test_2.R
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

class CustomDialogCreator @Inject constructor(private val fragment: Fragment) {

    fun showSettingsDialog(onPositiveClick: () -> Unit) {
        showDialog(
            title = fragment.getString(R.string.fragment_map_on_geo),
            message = fragment.getString(R.string.fragment_map_geo_text),
            positiveButtonText = fragment.getString(R.string.fragment_map_geo_settings),
            negativeButtonText = fragment.getString(R.string.fragment_map_geo_cancel),
            onPositiveClick = onPositiveClick
        )
    }

    @SuppressLint("StringFormatMatches")
    fun showRouteDialog(
        point: Point,
        namePoint: String,
        onPositiveClick: () -> Unit,
        onNeutralClick: () -> Unit
    ) {
        showDialog(
            title = fragment.getString(
                R.string.fragment_map_names,
                namePoint,
                point.latitude,
                point.longitude
            ),
            message = fragment.getString(R.string.fragment_map_question),
            positiveButtonText = fragment.getString(R.string.fragment_map_yes),
            negativeButtonText = fragment.getString(R.string.fragment_map_no),
            neutralButtonText = fragment.getString(R.string.map_fragment_delete_point),
            onPositiveClick = onPositiveClick,
            onNeutralClick = onNeutralClick
        )
    }

    fun showCreateNameDialog(onPositiveClick: (String) -> Unit) {
        val editText = EditText(fragment.requireActivity()).apply {
            hint = fragment.getString(R.string.fragment_map_create_name_hint)
        }

        AlertDialog.Builder(fragment.requireActivity())
            .setTitle(fragment.getString(R.string.fragment_map_name_point))
            .setMessage(fragment.getString(R.string.fragment_map_question_nme))
            .setView(editText)
            .setNegativeButton(R.string.fragment_map_no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.fragment_map_yes) { _, _ ->
                val pointName = editText.text.toString()
                onPositiveClick(pointName)
            }
            .create()
            .show()
    }

    private fun showDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        neutralButtonText: String? = null,
        onPositiveClick: (() -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null,
        onNeutralClick: (() -> Unit)? = null
    ) {
        val dialogBuilder = AlertDialog.Builder(fragment.requireActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> onPositiveClick?.invoke() }
            .setNegativeButton(negativeButtonText) { _, _ -> onNegativeClick?.invoke() }

        neutralButtonText?.let {
            dialogBuilder.setNeutralButton(it) { _, _ -> onNeutralClick?.invoke() }
        }

        dialogBuilder.create().show()
    }
}