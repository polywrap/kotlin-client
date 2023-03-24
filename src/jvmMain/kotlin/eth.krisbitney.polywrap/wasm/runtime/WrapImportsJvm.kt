package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import io.github.kawamuray.wasmtime.*
import kotlinx.coroutines.runBlocking

/**
 * A class for wrapping imports for a WebAssembly module with the JVM implementation.
 * @property state The WebAssembly module state.
 * @property memory The memory to be used by the WebAssembly module.
 * @constructor Creates an instance of [WrapImportsJvm] with the specified [state] and [memory].
 */
class WrapImportsJvm(private val state: WasmModuleState, private val memory: ByteArray) : WrapImports(state, memory), IWrapImports {

    /**
     * Returns a collection of WebAssembly imports for use with the WasmTime package
     * @return A collection of WasmTime [Extern] objects.
     */
    fun get(store: Store<WasmModuleState>): Collection<Extern> {
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
            state.subinvoke.result = null
            state.subinvoke.error = null
            
            val uri = readBytes(memory, uriPtr, uriLen).decodeToString()
            val method = readBytes(memory, methodPtr, methodLen).decodeToString()
            val args = readBytes(memory, argsPtr, argsLen)

            val result = runBlocking { state.invoker.invoke<ByteArray>(InvokeOptions(Uri(uri), method, args,)).await() }

            if (result.isSuccess) {
                state.subinvoke.result = result.getOrThrow()
            } else {
                state.subinvoke.error = result.exceptionOrNull().toString()
            }

            if (result.isSuccess) 1 else 0
        }
    }

    override fun __wrap_getImplementations(): (uriPtr: Int, uriLen: Int) -> Int {
        return { uriPtr: Int, uriLen: Int ->
            val uri = readBytes(memory, uriPtr, uriLen).decodeToString()
            val result = runBlocking { state.invoker.getImplementations(Uri(uri)).await() }
            if (result.isFailure) {
                state.abortWithInternalError(result.exceptionOrNull().toString())
            }
            val implementations = result.getOrThrow().map { it.uri }
            state.getImplementationsResult = msgPackEncode(implementations)
            if (implementations.isNotEmpty()) 1 else 0
        }
    }
}