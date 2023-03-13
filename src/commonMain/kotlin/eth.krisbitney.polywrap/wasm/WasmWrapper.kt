package eth.krisbitney.polywrap.wasm

import eth.krisbitney.polywrap.core.types.*

data class WasmWrapper(private val wasmModule: ByteArray) : Wrapper {

    fun getWasmModule(): ByteArray = wasmModule

    override suspend fun invoke(options: InvokeOptions, invoker: Invoker): InvokeResult<ByteArray> {
        val (uri, method, args, env, resolutionContext) = options
        val wasmModule = getWasmModule()

        fun abortWithInvokeAborted(message: String, source: ErrorSource? = null) {
            val prev = WrapError.parse(message)
            val text = prev?.let { "SubInvocation exception encountered" } ?: message
            throw WrapError(
                reason = text,
                code = WrapErrorCode.WRAPPER_INVOKE_ABORTED,
                uri = uri.uri,
                method = method,
                args = args.contentToString(),
                source = source,
                innerError = prev
            )
        }

        fun abortWithInternalError(message: String) {
            throw WrapError(
                reason = message,
                code = WrapErrorCode.WRAPPER_INTERNAL_ERROR,
                uri = options.uri.uri,
                method = method,
                args = args.contentToString()
            )
        }

        return Result.success(byteArrayOf(0))
    }
}