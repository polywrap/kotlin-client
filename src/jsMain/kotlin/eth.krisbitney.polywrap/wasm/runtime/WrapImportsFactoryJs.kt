package eth.krisbitney.polywrap.wasm.runtime

/**
 * A class for wrapping imports for a WebAssembly module with the JS implementation.
 * @property state The WebAssembly module state.
 * @property memory The memory to be used by the WebAssembly module.
 * @constructor Creates an instance of [WrapImportsFactoryJs] with the specified [state] and [memory].
 */
class WrapImportsFactoryJs(private val state: WasmModuleState, private val memory: ByteArray) {

    interface WrapImportsContainerJs {
        val wrap: WrapImports
        val env: WrapEnvContainerJs
    }

    interface WrapEnvContainerJs {
        val memory: ByteArray
    }

    /**
     * Gets an object with WebAssembly imports for use with the asyncify package.
     * @return An object with WebAssembly imports for use with the asyncify package
     */
    fun get(): WrapImportsContainerJs {
        return object : WrapImportsContainerJs {
            override val wrap: WrapImports = CommonWrapImports(state, memory)
            override val env: WrapEnvContainerJs = object : WrapEnvContainerJs {
                override val memory: ByteArray = this@WrapImportsFactoryJs.memory
            }
        }
    }
//    fun get(): WrapImports {
//        return object : WrapImports {
//            override suspend fun __wrap_subinvoke(
//                uriPtr: Int,
//                uriLen: Int,
//                methodPtr: Int,
//                methodLen: Int,
//                argsPtr: Int,
//                argsLen: Int
//            ): Int = __wrap_subinvoke(uriPtr, uriLen, methodPtr, methodLen, argsPtr, argsLen)
//            override fun __wrap_subinvoke_result_len(): Int = __wrap_subinvoke_result_len()
//            override fun __wrap_subinvoke_result(ptr: Int): Unit = __wrap_subinvoke_result(ptr)
//            override fun __wrap_subinvoke_error_len(): Int = __wrap_subinvoke_error_len()
//            override fun __wrap_subinvoke_error(ptr: Int): Unit = __wrap_subinvoke_error(ptr)
//            override fun __wrap_invoke_args(methodPtr: Int, argsPtr: Int): Unit = __wrap_invoke_args(methodPtr, argsPtr)
//            override fun __wrap_invoke_result(ptr: Int, len: Int): Unit = __wrap_invoke_result(ptr, len)
//            override fun __wrap_invoke_error(ptr: Int, len: Int): Unit = __wrap_invoke_error(ptr, len)
//            override suspend fun __wrap_getImplementations(uriPtr: Int, uriLen: Int): Int = __wrap_getImplementations(uriPtr, uriLen)
//            override fun __wrap_getImplementations_result_len(): Int = __wrap_getImplementations_result_len()
//            override fun __wrap_getImplementations_result(ptr: Int): Unit = __wrap_getImplementations_result(ptr)
//            override fun __wrap_load_env(ptr: Int): Unit = __wrap_load_env(ptr)
//            override fun __wrap_abort(msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int): Unit = __wrap_abort(msgPtr, msgLen, filePtr, fileLen, line, column)
//            override fun __wrap_debug_log(ptr: Int, len: Int): Unit = __wrap_debug_log(ptr, len)
//        }
//    }
}