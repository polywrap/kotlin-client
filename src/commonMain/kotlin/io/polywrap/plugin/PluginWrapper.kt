package io.polywrap.plugin

import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import kotlinx.coroutines.runBlocking
import uniffi.polywrap_native.FfiInvoker

/**
 * Represents a plugin wrapper, allowing the plugin module to be invoked as a [Wrapper].
 *
 * @param TConfig The type of the configuration object used by the plugin module.
 * @property module The plugin module instance associated with the wrapper.
 */
@OptIn(ExperimentalUnsignedTypes::class)
data class PluginWrapper<TConfig>(val module: PluginModule<TConfig>) : Wrapper {

    override fun invoke(
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        invoker: FfiInvoker
    ): List<UByte> = this.invoke(
        method = method,
        args = args?.toUByteArray()?.asByteArray(),
        env = env?.toUByteArray()?.asByteArray(),
        invoker = Invoker(invoker)
    ).getOrThrow().asUByteArray().toList()

    override fun invoke(
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        invoker: Invoker
    ): Result<ByteArray> = runCatching {
        return runBlocking { module.wrapInvoke(method, args, env, invoker) }
    }
}
