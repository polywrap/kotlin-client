package eth.krisbitney.polywrap.wasm.runtime

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceNative(module, state)
}

class WasmInstanceNative(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {
    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        return Result.success(ByteArray(0))
    }
}
