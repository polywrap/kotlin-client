package eth.krisbitney.polywrap.plugin

import eth.krisbitney.polywrap.core.types.Invoker

abstract class PluginModule<TConfig>(
    private val config: TConfig,
    private val methods: Map<String, PluginMethod>? = null
) {

    suspend fun wrapInvoke(
        method: String,
        args: ByteArray?,
        invoker: Invoker,
        env: ByteArray?
    ): Result<ByteArray> {
        val fn = getMethod(method) ?: return Result.failure(Error("Plugin missing method \"$method\""))
        return kotlin.runCatching { fn(args, invoker, env) }
    }

    abstract fun getMethod(method: String): PluginMethod?
}
