package io.polywrap.wasm.runtime

/**
 * A platform-specific object for creating [WasmInstance] objects.
 */
actual object WasmInstanceFactory {

    /**
     * Creates a [WasmInstance] using the provided module and state.
     *
     * @param module A [ByteArray] representing the WebAssembly module.
     * @param state A [WasmModuleState] object representing the module's state.
     * @return A [WasmInstance] object for the provided module and state.
     */
    actual fun createInstance(
        module: ByteArray,
        state: WasmModuleState
    ): WasmInstance = WasmInstanceNative(module, state)
}

/**
 * A platform-specific [WasmInstance] implementation for native environments.
 *
 * @property module A [ByteArray] representing the WebAssembly module.
 * @property state A [WasmModuleState] object representing the module's state.
 */
class WasmInstanceNative(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    /**
     * Invokes a specified method in the WebAssembly instance.
     *
     * @param method A [String] representing the method name to be invoked.
     * @param args A [ByteArray] containing the arguments to be passed to the method.
     * @param env A [ByteArray] containing the environment data, or `null` if not provided.
     * @return A [Result] containing a [ByteArray] with the method invocation result or an exception if the invocation fails.
     */
    override fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        return Result.success(ByteArray(0))
    }
}
