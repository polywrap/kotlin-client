package io.polywrap.wasm.runtime

/**
 * A factory object for creating WebAssembly instances.
 */
actual object WasmInstanceFactory {
    /**
     * Creates a new WebAssembly instance using the provided module and module state.
     * @param module an array of bytes representing the WebAssembly module.
     * @param state a WasmModuleState object representing the initial state of the WebAssembly module.
     * @return a WasmInstance object representing the newly created WebAssembly instance.
     */
    actual fun createInstance(
        module: ByteArray,
        state: WasmModuleState
    ): WasmInstance {
        TODO("Not yet implemented")
    }
}
