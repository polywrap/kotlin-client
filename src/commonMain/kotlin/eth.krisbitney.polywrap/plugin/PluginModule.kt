package eth.krisbitney.polywrap.plugin

import eth.krisbitney.polywrap.core.types.Invoker

abstract class PluginModule<TConfig>(val config: TConfig) {

    abstract val methods: Map<String, PluginMethod>

    suspend fun wrapInvoke(
        method: String,
        args: ByteArray?,
        invoker: Invoker,
        env: ByteArray?
    ): Result<ByteArray> {
        val fn = methods[method] ?: return Result.failure(Error("Plugin missing method \"$method\""))
        return kotlin.runCatching { fn(args, invoker, env) }
    }
}
