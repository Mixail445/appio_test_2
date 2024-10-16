package com.example.appio_test_2.utils


import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [R]
 * or a [E] error.
 */
sealed class AppResult<out R, out E> {
    data class Success<out R>(val result: R) : AppResult<R, Nothing>()
    data class Error<out E>(val error: E) : AppResult<Nothing, E>()

    companion object {
        fun <S> success(value: S): Success<S> = Success(value)
        fun <E> error(error: E): Error<E> = Error(error)
    }
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the block function execution
 * and encapsulating it as a failure.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <R> runCatching(block: () -> R): AppResult<R, Throwable> {
    return try {
        AppResult.success(block())
    } catch (e: Throwable) {
        AppResult.error(e)
    }
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any Throwable exception that was thrown from the block function execution, then mapping it
 * with [errorMapper] and encapsulating it as a failure.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <R, E> runCatching(
    errorMapper: (Throwable) -> E,
    block: () -> R
): AppResult<R, E> {
    return try {
        AppResult.success(block())
    } catch (e: Throwable) {
        AppResult.error(errorMapper(e))
    }
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents success.
 * Returns the original [Result] unchanged.
 */
@OptIn(ExperimentalContracts::class)
inline fun <R, E> AppResult<R, E>.onSuccess(action: (value: R) -> Unit): AppResult<R, E> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is AppResult.Success) {
        action(result)
    }
    return this
}

/**
 * Performs the given [action] on the encapsulated [E] error if this instance represents failure.
 * Returns the original [AppResult] unchanged.
 */
@OptIn(ExperimentalContracts::class)
inline fun <R, E> AppResult<R, E>.onError(action: (error: E) -> Unit): AppResult<R, E> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is AppResult.Error) {
        action(error)
    }
    return this
}