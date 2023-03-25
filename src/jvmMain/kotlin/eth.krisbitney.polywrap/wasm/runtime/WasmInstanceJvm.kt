package eth.krisbitney.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.*
import java.util.*

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJvm(module, state)
}

class WasmInstanceJvm(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val engine = Engine()
        val store: Store<WasmModuleState> = Store(state, engine)
        val memory: Memory = createMemory(store, module).getOrThrow()
        val wasmTimeModule: Module = Module.fromBinary(engine, module)
        val importsFactory = WrapImportsFactoryJvm(store.data(), memory.buffer(store).array())
        val imports = importsFactory.get(store)
        val instance = Instance(store, wasmTimeModule, imports)
        instance.use {
            val func = instance.getFunc(store, "_wrap_invoke").get()
            func.use {
                val fn = WasmFunctions.func(store, func, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32)
                val isSuccess = fn.call(method.length, args.size, env?.size ?: 0)
                return processResult(isSuccess == 1)
            }
        }
    }

    private fun createMemory(store: Store<WasmModuleState>, module: ByteArray): Result<Memory> {
        val idx = Collections.indexOfSubList(module.toList(), ENV_MEMORY_IMPORTS_SIGNATURE.toList())

        if (idx == -1) {
            val error = Error("Unable to find Wasm memory import section. " +
                    "Modules must import memory from the \"env\" module's " +
                    "\"memory\" field like so: " +
                    "(import \"env\" \"memory\" (memory (;0;) #))")
            return Result.failure(error)
        }

        val memoryInitialLimits = module[idx + ENV_MEMORY_IMPORTS_SIGNATURE.size + 1].toLong()
        val memory = Memory(store,
            MemoryType(memoryInitialLimits,false)
        )

        return Result.success(memory)
    }
}