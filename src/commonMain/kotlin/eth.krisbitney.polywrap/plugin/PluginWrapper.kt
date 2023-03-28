package eth.krisbitney.polywrap.plugin

import eth.krisbitney.polywrap.core.types.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PluginWrapper<TConfig>(val module: PluginModule<TConfig>) : Wrapper {

    override suspend fun invoke(options: InvokeOptions, invoker: Invoker): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val (uri, method, args, env, _) = options
            if (module.methods[method] == null) {
                val error = WrapError(
                    reason = "Plugin missing method \"$method\"",
                    code = WrapErrorCode.WRAPPER_METHOD_NOT_FOUND,
                    uri = uri.uri,
                    method = method
                )
                Result.failure<ByteArray>(error)
            }

            // Invoke the function
            val result = module.wrapInvoke(method, args, invoker, env)

            if (result.isSuccess) {
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
                    args = args.contentToString(),
                )
                Result.failure(error)
            }
        }
    }
}
