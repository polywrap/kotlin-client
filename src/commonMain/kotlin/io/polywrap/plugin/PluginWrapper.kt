package io.polywrap.plugin

import io.polywrap.core.types.*
import kotlinx.coroutines.runBlocking

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
     * @param options The options for invoking the method.
     * @param invoker The invoker instance.
     * @return A [Result] containing the result as a [ByteArray] on success, or an error if one occurs.
     */
    override fun invoke(options: InvokeOptions, invoker: Invoker): Result<ByteArray> {
        val (uri, method, args, env, _) = options
        if (module.methods[method] == null) {
            val error = WrapError(
                reason = "Plugin missing method \"$method\"",
                code = WrapErrorCode.WRAPPER_METHOD_NOT_FOUND,
                uri = uri.uri,
                method = method
            )
            return Result.failure(error)
        }

        // Invoke the function
        val result = runBlocking { module.wrapInvoke(method, args, invoker, env) }

        return if (result.isSuccess) {
            result
        } else {
            val exception = result.exceptionOrNull()!!
            val reason = exception.message ?: "Failed to invoke method \"$method\""
            // TODO: add error source
            val error = WrapError(
                reason = reason,
                code = WrapErrorCode.WRAPPER_INVOKE_ABORTED,
                uri = options.uri.toString(),
                method = method,
                args = args.contentToString()
            )
            Result.failure(error)
        }
    }
}
