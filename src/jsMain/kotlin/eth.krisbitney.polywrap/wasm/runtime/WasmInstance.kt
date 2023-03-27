
package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.externals.AsyncWasmInstance
import kotlinx.coroutines.await
import kotlin.js.Json

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJs(module, state)
}

class WasmInstanceJs(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    val REQUIRED_EXPORT: String = "_wrap_invoke"

    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val memory = AsyncWasmInstance.createMemory(object : AsyncWasmInstance.createMemoryArgs {
            override var module = this@WasmInstanceJs.module
        })
        val wrapImports = WrapImportsFactoryJs.get(state, memory)
        val instance = AsyncWasmInstance.createInstance(object : AsyncWasmInstance.createInstanceArgs {
            override var module = this@WasmInstanceJs.module
            override var imports: Json = wrapImports
            override var requiredExports: List<String>? = listOf(REQUIRED_EXPORT)
        }).await()
        val isSuccess = instance.exports._wrap_invoke(method.length, args.size, env?.size ?: 0).await()
        return processResult(isSuccess == 1)
    }
}