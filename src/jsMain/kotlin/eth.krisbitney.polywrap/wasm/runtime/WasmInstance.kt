
package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.externals.AsyncWasmInstance
import kotlinx.coroutines.await

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJs(module, state)
}

const val REQUIRED_EXPORT: String = "_wrap_invoke"

class WasmInstanceJs(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val memory = AsyncWasmInstance.createMemory(object {
            val module: ByteArray = this@WasmInstanceJs.module
        })
        val importsFactory = WrapImportsFactoryJs(state, memory.buffer.unsafeCast<ByteArray>())
        val imports = importsFactory.get()
        val instance = AsyncWasmInstance.createInstance(object {
            val module: ByteArray = this@WasmInstanceJs.module
            val imports = imports
            val requiredExports = arrayOf(REQUIRED_EXPORT)
        }).await()
        val exports = instance.exports as WrapExports
        val isSuccess = exports._wrap_invoke(method.length, args.size, env?.size ?: 0)
        return processResult(isSuccess == 1)
    }
}