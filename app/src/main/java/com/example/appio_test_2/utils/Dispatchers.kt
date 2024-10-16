package com.example.appio_test_2.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
    val io: CoroutineDispatcher
}

object DispatchersProviderImpl : DispatchersProvider {
    override val default = Dispatchers.Default
    override val main = Dispatchers.Main.immediate
    override val unconfined = Dispatchers.Unconfined
    override val io = Dispatchers.IO
}