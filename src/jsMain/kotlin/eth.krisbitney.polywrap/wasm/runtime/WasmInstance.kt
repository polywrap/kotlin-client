package eth.krisbitney.polywrap.wasm.runtime

class WasmInstanceJs(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {
    override fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        return Result.success(ByteArray(0))
    }
}

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJs(module, state)
}