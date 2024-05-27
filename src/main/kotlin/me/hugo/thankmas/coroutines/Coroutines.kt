package me.hugo.thankmas.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public class MinecraftRunBlockingContextElement internal constructor() :
    AbstractCoroutineContextElement(MinecraftRunBlockingContextElement) {

    public companion object Key : CoroutineContext.Key<CoroutineName>
    override fun toString(): String = "RunBlockingMine"

}

/** Tells minecraft to not freak out about coroutines. */
@OptIn(ExperimentalContracts::class)
public fun <T> runBlockingMine(
    context: CoroutineContext = EmptyCoroutineContext,
    coroutine: suspend CoroutineScope.() -> T
): T {
    contract { callsInPlace(coroutine, InvocationKind.EXACTLY_ONCE) }

    return runBlocking(context + MinecraftRunBlockingContextElement(), coroutine)
}