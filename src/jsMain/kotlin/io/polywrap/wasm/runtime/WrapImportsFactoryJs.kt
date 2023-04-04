package io.polywrap.wasm.runtime

import io.polywrap.externals.AsyncWasmInstance
import io.polywrap.externals.WebAssembly
import io.polywrap.util.jsObject
import kotlin.js.Json

/**
 * A class for wrapping imports for a WebAssembly module with the JS implementation.
 */
class WrapImportsFactoryJs {

    companion object {
        /**
         * Gets an object with WebAssembly imports for use with the asyncify package.
         * @property state The WebAssembly module state.
         * @property webAssemblyMemory The memory to be used by the WebAssembly module.
         * @return An object with WebAssembly imports for use with the asyncify package
         */
        fun get(state: WasmModuleState, webAssemblyMemory: WebAssembly.Memory): AsyncWasmInstance.Imports {
            val commonWrapImports = WrapImportsJs(state, webAssemblyMemory)
            val wrapImports = jsObject<Json> {
                this["__wrap_subinvoke"] = commonWrapImports::__wrap_subinvoke
                this["__wrap_subinvoke_result_len"] = commonWrapImports::__wrap_subinvoke_result_len
                this["__wrap_subinvoke_result"] = commonWrapImports::__wrap_subinvoke_result
                this["__wrap_subinvoke_error_len"] = commonWrapImports::__wrap_subinvoke_error_len
                this["__wrap_subinvoke_error"] = commonWrapImports::__wrap_subinvoke_error
                this["__wrap_invoke_args"] = commonWrapImports::__wrap_invoke_args
                this["__wrap_invoke_result"] = commonWrapImports::__wrap_invoke_result
                this["__wrap_invoke_error"] = commonWrapImports::__wrap_invoke_error
                this["__wrap_getImplementations"] = commonWrapImports::__wrap_getImplementations
                this["__wrap_getImplementations_result_len"] = commonWrapImports::__wrap_getImplementations_result_len
                this["__wrap_getImplementations_result"] = commonWrapImports::__wrap_getImplementations_result
                this["__wrap_abort"] = commonWrapImports::__wrap_abort
                this["__wrap_debug_log"] = commonWrapImports::__wrap_debug_log
                this["__wrap_load_env"] = commonWrapImports::__wrap_load_env
            }
            return jsObject {
                wrap = wrapImports
                env = jsObject<AsyncWasmInstance.Env> {
                    memory = webAssemblyMemory
                }
            }
        }
    }
}
