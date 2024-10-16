package com.example.appio_test_2.utils

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import javax.inject.Inject

class ResultWrapper @Inject constructor() {
    @SuppressWarnings("TooGenericExceptionCaught")
    suspend fun <R> wrap(request: suspend () -> R): AppResult<R, Throwable> {
        val result = try {
            AppResult.Success(request())
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
        return result
    }
}

/**
 * Default minimal [DiffUtil.ItemCallback] implementation.
 * Compares items in [areContentsTheSame] and returns null as change payload.
 */
fun <T : Any> itemCallback(
    areItemsTheSame: (oldItem: T, newItem: T) -> Boolean,
    areContentsTheSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem ->
        oldItem == newItem
    },
    getChangePayload: (oldItem: T, newItem: T) -> Any? = { _, _ ->
        null
    }
) = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        areItemsTheSame(oldItem, newItem)

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        areContentsTheSame(oldItem, newItem)

    override fun getChangePayload(oldItem: T, newItem: T): Any? = getChangePayload(oldItem, newItem)
}