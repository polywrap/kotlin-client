package eth.krisbitney.polywrap.wasm.runtime

/**
 * An interface representing a WebAssembly instance that can be invoked using a specific method,
 * arguments, and environment.
 */
interface WasmInstance {

    /**
     * Invokes a specific method of the WebAssembly instance with the provided arguments and environment.
     * @param method the name of the method to be invoked.
     * @param args an array of bytes representing the arguments to be passed to the method.
     * @param env an optional array of bytes representing the environment to be used during method invocation.
     * @return a Result object containing an array of bytes representing the result of the method invocation.
     */
    fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray>
}

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