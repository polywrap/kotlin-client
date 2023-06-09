package io.polywrap.plugin

import io.polywrap.core.AbortHandler
import io.polywrap.core.DefaultAbortHandler
import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import kotlinx.coroutines.runBlocking
import uniffi.main.FfiInvoker

/**
 * Represents a plugin wrapper, allowing the plugin module to be invoked as a [Wrapper].
 *
 * @param TConfig The type of the configuration object used by the plugin module.
 * @property module The plugin module instance associated with the wrapper.
 */
data class PluginWrapper<TConfig>(val module: PluginModule<TConfig>) : Wrapper {

    /**
     * Invokes a method in the plugin module with the specified options and invoker.
     *
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param invoker The [Invoker] instance.
     * @param abortHandler An [AbortHandler] to be called when the invocation is aborted.
     * @return A list of MessagePack-encoded bytes representing the invocation result
     */
    override fun invoke(
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        invoker: FfiInvoker,
        abortHandler: AbortHandler?
    ): List<UByte> {
        val result = runBlocking {
            module.wrapInvoke(
                method,
                args?.toUByteArray()?.toByteArray(),
                env?.toUByteArray()?.toByteArray(),
                invoker as Invoker
            )
        }

        if (result.isFailure) {
            val exception = result.exceptionOrNull()!!
            val handler = abortHandler ?: DefaultAbortHandler()
            handler.abort(exception.message ?: "Failed to invoke method \"$method\"")
        }

        return result.getOrThrow().toUByteArray().toList()
    }
}
