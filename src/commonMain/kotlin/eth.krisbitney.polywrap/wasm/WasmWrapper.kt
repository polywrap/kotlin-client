package eth.krisbitney.polywrap.wasm

import eth.krisbitney.polywrap.core.types.*
import eth.krisbitney.polywrap.wasm.runtime.WasmInstanceFactory
import eth.krisbitney.polywrap.wasm.runtime.WasmModuleState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Represents a WebAssembly (Wasm) wrapper for executing Wasm code.
 *
 * @property wasmModule The WebAssembly module as a ByteArray.
 */
data class WasmWrapper(val wasmModule: ByteArray) : Wrapper {

    /**
     * Invokes a method in the WebAssembly module with the specified options and invoker.
     *
     * @param options The options for invoking the method.
     * @param invoker The invoker instance.
     * @return A [Deferred] [Result] containing the result as a [ByteArray] on success, or an error if one occurs.
     */
    override suspend fun invoke(options: InvokeOptions, invoker: Invoker): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val (_, method, args, env, _) = options
            val (abortWithInvokeAborted, abortWithInternalError) = createAborts(options)
            val state = WasmModuleState(
                method = method,
                args = args ?: byteArrayOf(0),
                env = env ?: byteArrayOf(0),
                abortWithInvokeAborted = abortWithInvokeAborted,
                abortWithInternalError = abortWithInternalError,
                invoker = invoker
            )
            val instance = WasmInstanceFactory.createInstance(wasmModule, state)
            instance.invoke(method, args ?: byteArrayOf(0), env ?: byteArrayOf(0))
        }
    }

    /**
     * Creates abort functions for handling errors during method invocation.
     *
     * @param options The options for invoking the method.
     * @return A pair of abort functions for handling invocation-aborted errors and internal errors.
     */
    private fun createAborts(options: InvokeOptions): Pair<(String, ErrorSource?) -> Nothing, (String) -> Nothing> {
        val abortWithInvokeAborted: (String, ErrorSource?) -> Nothing = { message, source ->
            val prev = WrapError.parse(message)
            val text = prev?.let { "SubInvocation exception encountered" } ?: message
            throw WrapError(
                reason = text,
                code = WrapErrorCode.WRAPPER_INVOKE_ABORTED,
                uri = options.uri.uri,
                method = options.method,
                args = options.args.contentToString(),
                source = source,
                innerError = prev
            )
        }

        val abortWithInternalError: (String) -> Nothing = { message ->
            throw WrapError(
                reason = message,
                code = WrapErrorCode.WRAPPER_INTERNAL_ERROR,
                uri = options.uri.uri,
                method = options.method,
                args = options.args.contentToString()
            )
        }

        return Pair(abortWithInvokeAborted, abortWithInternalError)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as WasmWrapper

        if (!wasmModule.contentEquals(other.wasmModule)) return false

        return true
    }

    override fun hashCode(): Int {
        return wasmModule.contentHashCode()
    }
}