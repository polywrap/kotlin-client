package eth.krisbitney.polywrap.wasm

import eth.krisbitney.polywrap.core.types.*
import eth.krisbitney.polywrap.wasm.runtime.WasmInstanceFactory
import eth.krisbitney.polywrap.wasm.runtime.WasmModuleState

data class WasmWrapper(private val wasmModule: ByteArray) : Wrapper {

    fun getWasmModule(): ByteArray = wasmModule

    override suspend fun invoke(options: InvokeOptions, invoker: Invoker): InvokeResult<ByteArray> {
        val (_, method, args, env, _) = options
        val (abortWithInvokeAborted, abortWithInternalError) = createAborts(options)
        val module = getWasmModule()
        val state = WasmModuleState(
            method = method,
            args = args ?: byteArrayOf(0),
            env = env ?: byteArrayOf(0),
            abortWithInvokeAborted = abortWithInvokeAborted,
            abortWithInternalError = abortWithInternalError,
            invoker = invoker
        )
        val instance = WasmInstanceFactory.createInstance(module, state)
        return instance.invoke(method, args ?: byteArrayOf(0), env ?: byteArrayOf(0))
    }

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
}