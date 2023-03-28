package eth.krisbitney.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.*
import kotlinx.coroutines.runBlocking

/**
 * A class for wrapping imports for a WebAssembly module with the JVM implementation.
 */
class WrapImportsFactoryJvm {

    companion object {
        /**
         * Returns a collection of WebAssembly imports for use with the WasmTime package
         * @property store The WebAssembly module state.
         * @property memory The memory to be used by the WebAssembly module.
         * @return A collection of WasmTime [Extern] objects.
         */
        fun get(store: Store<WasmModuleState>, memory: ByteArray): Collection<Extern> {
            val wrapImports = CommonWrapImports(store.data(), memory)
            return listOf(
                WasmFunctions.wrap(
                    store,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32
                ) { uriPtr: Int, uriLen: Int, methodPtr: Int, methodLen: Int, argsPtr: Int, argsLen: Int ->
                    Int
                    runBlocking { wrapImports.__wrap_subinvoke(uriPtr, uriLen, methodPtr, methodLen, argsPtr, argsLen) }
                },
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_subinvoke_result_len),
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_subinvoke_result),
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_subinvoke_error_len),
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_subinvoke_error),
                WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, wrapImports::__wrap_invoke_args),
                WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, wrapImports::__wrap_invoke_result),
                WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, wrapImports::__wrap_invoke_error),
                WasmFunctions.wrap(
                    store,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32
                ) { uriPtr: Int, uriLen: Int ->
                    Int
                    runBlocking { wrapImports.__wrap_getImplementations(uriPtr, uriLen) }
                },
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_getImplementations_result_len),
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_getImplementations_result),
                WasmFunctions.wrap(
                    store,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    WasmValType.I32,
                    wrapImports::__wrap_abort
                ),
                WasmFunctions.wrap(store, WasmValType.I32, WasmValType.I32, wrapImports::__wrap_debug_log),
                WasmFunctions.wrap(store, WasmValType.I32, wrapImports::__wrap_load_env),
            ).map { Extern.fromFunc(it) }
        }
    }
}