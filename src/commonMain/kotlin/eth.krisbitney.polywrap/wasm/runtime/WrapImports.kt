package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.core.types.ErrorSource

/**
 * A class for wrapping imports for a WebAssembly module.
 * @property state The WebAssembly module state.
 * @property memory The memory to be used by the WebAssembly module.
 * @constructor Creates an instance of [WrapImports] with the specified [state] and [memory].
 */
abstract class WrapImports(private val state: WasmModuleState, private val memory: ByteArray) : IWrapImports {

    abstract override fun __wrap_subinvoke(): (uriPtr: Int, uriLen: Int, methodPtr: Int, methodLen: Int, argsPtr: Int, argsLen: Int) -> Int

    abstract override fun __wrap_getImplementations(): (uriPtr: Int, uriLen: Int) -> Int

    override fun __wrap_subinvoke_result_len(): () -> Int {
        return {
            state.subinvoke.result?.size
                ?: state.abortWithInternalError("__wrap_subinvoke_result_len: subinvoke.result is not set")
        }
    }

    override fun __wrap_subinvoke_result(): (ptr: Int) -> Unit {
        return {
            val result = state.subinvoke.result
            if (result == null) {
                state.abortWithInternalError("__wrap_subinvoke_result: subinvoke.result is not set")
            } else {
                writeBytes(result, memory, it)
            }
        }
    }

    override fun __wrap_subinvoke_error_len(): () -> Int {
        return {
            state.subinvoke.error?.length
                ?: state.abortWithInternalError("__wrap_subinvoke_error_len: subinvoke.error is not set")
        }
    }

    override fun __wrap_subinvoke_error(): (ptr: Int) -> Unit {
        return {
            val result = state.subinvoke.error
            if (result == null) {
                state.abortWithInternalError("__wrap_subinvoke_error: subinvoke.error is not set")
            } else {
                writeBytes(result.encodeToByteArray(), memory, it)
            }
        }
    }

    override fun __wrap_invoke_args(): (methodPtr: Int, argsPtr: Int) -> Unit {
        return { methodPtr: Int, argsPtr: Int ->
            if (state.method.isEmpty()) {
                state.abortWithInternalError("__wrap_invoke_args: method is not set")
            }
            if (state.args.isEmpty()) {
                state.abortWithInternalError("__wrap_invoke_args: args is not set")
            }
            writeBytes(state.method.encodeToByteArray(), memory, methodPtr)
            writeBytes(state.args, memory, argsPtr)
        }
    }

    override fun __wrap_invoke_result(): (ptr: Int, len: Int) -> Unit {
        return { ptr: Int, len: Int ->
            state.invoke.result = readBytes(memory, ptr, len)
        }
    }

    override fun __wrap_invoke_error(): (ptr: Int, len: Int) -> Unit {
        return { ptr: Int, len: Int ->
            state.invoke.error = readBytes(memory, ptr, len).decodeToString()
        }
    }

    override fun __wrap_getImplementations_result_len(): () -> Int {
        return {
            state.getImplementationsResult?.size
                ?: state.abortWithInternalError("__wrap_getImplementations_result_len: result is not set")
        }
    }

    override fun __wrap_getImplementations_result(): (ptr: Int) -> Unit {
        return {
            val result = state.getImplementationsResult
            if (result == null) {
                state.abortWithInternalError("__wrap_getImplementations_result: result is not set")
            } else {
                writeBytes(result, memory, it)
            }
        }
    }

    override fun __wrap_abort(): (msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int) -> Unit {
        return { msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int ->
            val message = readBytes(memory, msgPtr, msgLen).decodeToString()
            val file = readBytes(memory, filePtr, fileLen).decodeToString()
            state.abortWithInvokeAborted("__wrap_abort: $message", ErrorSource(file, line, column))
        }
    }

    override fun __wrap_debug_log(): (ptr: Int, len: Int) -> Unit {
        return { ptr: Int, len: Int ->
            val message = readBytes(memory, ptr, len).decodeToString()
            println("__wrap_debug_log: $message")
        }
    }

    override fun __wrap_load_env(): (ptr: Int) -> Unit {
        return {
            writeBytes(state.env, memory, it)
        }
    }
}