
package io.polywrap.wasm.runtime

import io.polywrap.externals.AsyncWasmInstance
import io.polywrap.util.jsObject
import kotlinx.coroutines.await

/**
 * A platform-specific object for creating [WasmInstance] objects in JavaScript.
 */
actual object WasmInstanceFactory {
    /**
     * Creates a [WasmInstance] using the provided module and state.
     *
     * @param module A [ByteArray] representing the WebAssembly module.
     * @param state A [WasmModuleState] object representing the module's state.
     * @return A [WasmInstance] object for the provided module and state.
     */
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJs(module, state)
}

/**
 * A platform-specific [WasmInstance] implementation for JavaScript.
 *
 * @property module A [ByteArray] representing the WebAssembly module.
 * @property state A [WasmModuleState] object representing the module's state.
 */
class WasmInstanceJs(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    val REQUIRED_EXPORT: String = "_wrap_invoke"

    /**
     * Invokes a specified method in the WebAssembly instance.
     *
     * @param method A [String] representing the method name to be invoked.
     * @param args A [ByteArray] containing the arguments to be passed to the method.
     * @param env A [ByteArray] containing the environment data, or `null` if not provided.
     * @return A [Result] containing a [ByteArray] with the method invocation result or an exception if the invocation fails.
     */
    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val memory = AsyncWasmInstance.createMemory(
            jsObject {
                module = this@WasmInstanceJs.module
            }
        )
        val wrapImports = WrapImportsFactoryJs.get(state, memory)
        val instance = AsyncWasmInstance.createInstance(
            jsObject {
                module = this@WasmInstanceJs.module
                imports = wrapImports
                requiredExports = listOf(REQUIRED_EXPORT)
            }
        ).await()
        val isSuccess = instance.exports._wrap_invoke(method.length, args.size, env?.size ?: 0).await()
        return processResult(isSuccess == 1)
    }
}
