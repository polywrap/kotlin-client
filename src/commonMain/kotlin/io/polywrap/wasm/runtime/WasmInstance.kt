package io.polywrap.wasm.runtime

/**
 * A factory object for creating WebAssembly instances.
 */
expect object WasmInstanceFactory {

    /**
     * Creates a new WebAssembly instance using the provided module and module state.
     * @param module an array of bytes representing the WebAssembly module.
     * @param state a WasmModuleState object representing the initial state of the WebAssembly module.
     * @return a WasmInstance object representing the newly created WebAssembly instance.
     */
    fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance
}

/**
 * An interface representing a WebAssembly instance that can be invoked using a specific method,
 * arguments, and environment.
 */
abstract class WasmInstance(protected val module: ByteArray, protected val state: WasmModuleState) {

    /**
     * Invokes a specific method of the WebAssembly instance with the provided arguments and environment.
     * @param method the name of the method to be invoked.
     * @param args an array of bytes representing the arguments to be passed to the method.
     * @param env an optional array of bytes representing the environment to be used during method invocation.
     * @return a Result object containing an array of bytes representing the result of the method invocation.
     */
    abstract suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray>

    protected fun processResult(isSuccess: Boolean): Result<ByteArray> {
        if (isSuccess) {
            val result =
                state.invoke.result ?: return Result.failure(IllegalStateException("Invoke result is missing."))
            return Result.success(result)
        } else {
            val exception =
                state.invoke.error ?: return Result.failure(IllegalStateException("Invoke error is missing."))
            return Result.failure(Exception(exception))
        }
    }

    protected val ENV_MEMORY_IMPORTS_SIGNATURE = byteArrayOf(
        0x65, 0x6e, 0x76, 0x06, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x02
    )
}
