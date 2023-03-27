package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.ErrorSource
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.msgpack.msgPackEncode

/**
 * A class for wrapping imports for a WebAssembly module.
 * @property state The WebAssembly module state.
 * @property memory The memory to be used by the WebAssembly module.
 * @constructor Creates an instance of [WrapImports] with the specified [state] and [memory].
 */
class CommonWrapImports(private val state: WasmModuleState, private val memory: ByteArray) : WrapImports {

    override suspend fun __wrap_subinvoke(uriPtr: Int, uriLen: Int, methodPtr: Int, methodLen: Int, argsPtr: Int, argsLen: Int): Int {
        state.subinvoke.result = null
        state.subinvoke.error = null

        val uri = readBytes(memory, uriPtr, uriLen).decodeToString()
        val method = readBytes(memory, methodPtr, methodLen).decodeToString()
        val args = readBytes(memory, argsPtr, argsLen)

        val result =  state.invoker.invoke(InvokeOptions(Uri(uri), method, args)).await()

        if (result.isSuccess) {
            state.subinvoke.result = result.getOrThrow()
        } else {
            state.subinvoke.error = result.exceptionOrNull().toString()
        }

        return if (result.isSuccess) 1 else 0
    }

    override fun __wrap_subinvoke_result_len(): Int {
        return state.subinvoke.result?.size
            ?: state.abortWithInternalError("__wrap_subinvoke_result_len: subinvoke.result is not set")
    }

    override fun __wrap_subinvoke_result(ptr: Int) {
        val result = state.subinvoke.result
        if (result == null) {
            state.abortWithInternalError("__wrap_subinvoke_result: subinvoke.result is not set")
        } else {
            writeBytes(result, memory, ptr)
        }
    }

    override fun __wrap_subinvoke_error_len(): Int {
       return state.subinvoke.error?.length
            ?: state.abortWithInternalError("__wrap_subinvoke_error_len: subinvoke.error is not set")
    }

    override fun __wrap_subinvoke_error(ptr: Int) {
        val result = state.subinvoke.error
        if (result == null) {
            state.abortWithInternalError("__wrap_subinvoke_error: subinvoke.error is not set")
        } else {
            writeBytes(result.encodeToByteArray(), memory, ptr)
        }
    }

    override fun __wrap_invoke_args(methodPtr: Int, argsPtr: Int) {
        if (state.method.isEmpty()) {
            state.abortWithInternalError("__wrap_invoke_args: method is not set")
        }
        if (state.args.isEmpty()) {
            state.abortWithInternalError("__wrap_invoke_args: args is not set")
        }
        writeBytes(state.method.encodeToByteArray(), memory, methodPtr)
        writeBytes(state.args, memory, argsPtr)
    }

    override fun __wrap_invoke_result(ptr: Int, len: Int) {
        state.invoke.result = readBytes(memory, ptr, len)
    }

    override fun __wrap_invoke_error(ptr: Int, len: Int) {
        state.invoke.error = readBytes(memory, ptr, len).decodeToString()
    }

    override suspend fun __wrap_getImplementations(uriPtr: Int, uriLen: Int): Int {
        val uri = readBytes(memory, uriPtr, uriLen).decodeToString()
        val result = state.invoker.getImplementations(Uri(uri)).await()
        if (result.isFailure) {
            state.abortWithInternalError(result.exceptionOrNull().toString())
        }
        val implementations = result.getOrThrow().map { it.uri }
        state.getImplementationsResult = msgPackEncode(implementations)
        return if (implementations.isNotEmpty()) 1 else 0
    }

    override fun __wrap_getImplementations_result_len(): Int {
        return state.getImplementationsResult?.size
            ?: state.abortWithInternalError("__wrap_getImplementations_result_len: result is not set")
    }

    override fun __wrap_getImplementations_result(ptr: Int) {
        val result = state.getImplementationsResult
        if (result == null) {
            state.abortWithInternalError("__wrap_getImplementations_result: result is not set")
        } else {
            writeBytes(result, memory, ptr)
        }
    }

    override fun __wrap_abort(msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int) {
        val message = readBytes(memory, msgPtr, msgLen).decodeToString()
        val file = readBytes(memory, filePtr, fileLen).decodeToString()
        state.abortWithInvokeAborted("__wrap_abort: $message", ErrorSource(file, line, column))
    }

    override fun __wrap_debug_log(ptr: Int, len: Int) {
        val message = readBytes(memory, ptr, len).decodeToString()
        println("__wrap_debug_log: $message")
    }

    override fun __wrap_load_env(ptr: Int) {
        writeBytes(state.env, memory, ptr)
    }
}