
package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.externals.AsyncWasmInstance
import eth.krisbitney.polywrap.util.jsObject
import kotlinx.coroutines.await

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJs(module, state)
}

class WasmInstanceJs(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    val REQUIRED_EXPORT: String = "_wrap_invoke"

    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val memory = AsyncWasmInstance.createMemory(jsObject {
            module = this@WasmInstanceJs.module
        })
        val wrapImports = WrapImportsFactoryJs.get(state, memory)
        val instance = AsyncWasmInstance.createInstance(jsObject {
            module = this@WasmInstanceJs.module
            imports = wrapImports
            requiredExports = listOf(REQUIRED_EXPORT)
        }).await()
        val isSuccess = instance.exports._wrap_invoke(method.length, args.size, env?.size ?: 0).await()
        return processResult(isSuccess == 1)
    }
}