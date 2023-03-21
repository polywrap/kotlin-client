package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.ErrorSource
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import io.github.kawamuray.wasmtime.*
import kotlinx.coroutines.runBlocking

/**
 * A class for wrapping imports for a WebAssembly module with the JVM implementation.
 * @property store The store to be used to create the WebAssembly functions.
 * @property memory The memory to be used by the WebAssembly module.
 * @constructor Creates an instance of [WrapImportsJvm] with the specified [store] and [memory].
 */
class WrapImportsJvm(private val store: Store<WasmModuleState>, private val memory: Memory) : WrapImports {

    /**
     * Returns a collection of WebAssembly imports for use with the WasmTime package
     * @return A collection of WasmTime [Extern] objects.
     */
    fun get(): Collection<Extern> {
        return listOf(
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, __wrap_subinvoke()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_subinvoke_result_len()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_subinvoke_result()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_subinvoke_error_len()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_subinvoke_error()),
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, __wrap_invoke_args()),
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, __wrap_invoke_result()),
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, __wrap_invoke_error()),
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, WasmValType.I32, __wrap_getImplementations()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_getImplementations_result_len()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_getImplementations_result()),
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32, __wrap_abort()),
            WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, __wrap_debug_log()),
            WasmFunctions.wrap(store, WasmValType.I32, __wrap_load_env()),
        ).map { Extern.fromFunc(it) }
    }

    override fun __wrap_subinvoke(): (uriPtr: Int, uriLen: Int, methodPtr: Int, methodLen: Int, argsPtr: Int, argsLen: Int) -> Int {
        return { uriPtr: Int, uriLen: Int, methodPtr: Int, methodLen: Int, argsPtr: Int, argsLen: Int ->
            val state = store.data()
            state.subinvoke.result = null
            state.subinvoke.error = null

            val memoryBuffer = memory.buffer(store).array()
            val uri = readBytes(memoryBuffer, uriPtr, uriLen).decodeToString()
            val method = readBytes(memoryBuffer, methodPtr, methodLen).decodeToString()
            val args = readBytes(memoryBuffer, argsPtr, argsLen)

            val result = runBlocking { state.invoker.invoke<ByteArray>(InvokeOptions(Uri(uri), method, args,)).await() }

            if (result.isSuccess) {
                state.subinvoke.result = result.getOrThrow()
            } else {
                state.subinvoke.error = result.exceptionOrNull().toString()
            }

            if (result.isSuccess) 1 else 0
        }
    }

    override fun __wrap_subinvoke_result_len(): () -> Int {
        return {
            val state = store.data()
            state.subinvoke.result?.size
                ?: state.abortWithInternalError("__wrap_subinvoke_result_len: subinvoke.result is not set")
        }
    }

    override fun __wrap_subinvoke_result(): (ptr: Int) -> Unit {
        return {
            val state = store.data()
            val result = state.subinvoke.result
            if (result == null) {
                state.abortWithInternalError("__wrap_subinvoke_result: subinvoke.result is not set")
            } else {
                val memoryBuffer = memory.buffer(store).array()
                writeBytes(result, memoryBuffer, it)
            }
        }
    }

    override fun __wrap_subinvoke_error_len(): () -> Int {
        return {
            val state = store.data()
            state.subinvoke.error?.length
                ?: state.abortWithInternalError("__wrap_subinvoke_error_len: subinvoke.error is not set")
        }
    }

    override fun __wrap_subinvoke_error(): (ptr: Int) -> Unit {
        return {
            val state = store.data()
            val result = state.subinvoke.error
            if (result == null) {
                state.abortWithInternalError("__wrap_subinvoke_error: subinvoke.error is not set")
            } else {
                val memoryBuffer = memory.buffer(store).array()
                writeBytes(result.encodeToByteArray(), memoryBuffer, it)
            }
        }
    }

    override fun __wrap_invoke_args(): (methodPtr: Int, argsPtr: Int) -> Unit {
        return { methodPtr: Int, argsPtr: Int ->
            val state = store.data()
            val memoryBuffer = memory.buffer(store).array()
            if (state.method.isEmpty()) {
                state.abortWithInternalError("__wrap_invoke_args: method is not set")
            }
            if (state.args.isEmpty()) {
                state.abortWithInternalError("__wrap_invoke_args: args is not set")
            }
            writeBytes(state.method.encodeToByteArray(), memoryBuffer, methodPtr)
            writeBytes(state.args, memoryBuffer, argsPtr)
        }
    }

    override fun __wrap_invoke_result(): (ptr: Int, len: Int) -> Unit {
        return { ptr: Int, len: Int ->
            val memoryBuffer = memory.buffer(store).array()
            store.data().invoke.result = readBytes(memoryBuffer, ptr, len)
        }
    }

    override fun __wrap_invoke_error(): (ptr: Int, len: Int) -> Unit {
        return { ptr: Int, len: Int ->
            val memoryBuffer = memory.buffer(store).array()
            store.data().invoke.error = readBytes(memoryBuffer, ptr, len).decodeToString()
        }
    }

    override fun __wrap_getImplementations(): (uriPtr: Int, uriLen: Int) -> Int {
        return { uriPtr: Int, uriLen: Int ->
            val state = store.data()
            val memoryBuffer = memory.buffer(store).array()

            val uri = readBytes(memoryBuffer, uriPtr, uriLen).decodeToString()
            val result = runBlocking { state.invoker.getImplementations(Uri(uri)).await() }
            if (result.isFailure) {
                state.abortWithInternalError(result.exceptionOrNull().toString())
            }
            val implementations = result.getOrThrow().map { it.uri }
            state.getImplementationsResult = msgPackEncode(implementations)
            if (implementations.isNotEmpty()) 1 else 0
        }
    }

    override fun __wrap_getImplementations_result_len(): () -> Int {
        return {
            val state = store.data()
            state.getImplementationsResult?.size
                ?: state.abortWithInternalError("__wrap_getImplementations_result_len: result is not set")
        }
    }

    override fun __wrap_getImplementations_result(): (ptr: Int) -> Unit {
        return {
            val state = store.data()
            val result = state.getImplementationsResult
            if (result == null) {
                state.abortWithInternalError("__wrap_getImplementations_result: result is not set")
            } else {
                val memoryBuffer = memory.buffer(store).array()
                writeBytes(result, memoryBuffer, it)
            }
        }
    }

    override fun __wrap_abort(): (msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int) -> Unit {
        return { msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int ->
            val memoryBuffer = memory.buffer(store).array()
            val message = readBytes(memoryBuffer, msgPtr, msgLen).decodeToString()
            val file = readBytes(memoryBuffer, filePtr, fileLen).decodeToString()
            store.data().abortWithInvokeAborted("__wrap_abort: $message", ErrorSource(file, line, column))
        }
    }

    override fun __wrap_debug_log(): (ptr: Int, len: Int) -> Unit {
        return { ptr: Int, len: Int ->
            val memoryBuffer = memory.buffer(store).array()
            val message = readBytes(memoryBuffer, ptr, len).decodeToString()
            println("__wrap_debug_log: $message")
        }
    }

    override fun __wrap_load_env(): (ptr: Int) -> Unit {
        return {
            val state = store.data()
            val memoryBuffer = memory.buffer(store).array()
            writeBytes(state.env, memoryBuffer, it)
        }
    }
}