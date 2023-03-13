package eth.krisbitney.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.*
import java.util.*

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJvm(module, state)
}

class WasmInstanceJvm(private val module: ByteArray, private val state: WasmModuleState) : WasmInstance {

    override fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val engine = Engine()
        val store: Store<WasmModuleState> = Store(state, engine)
        val memory: Memory = createMemory(store, module).getOrThrow()
        val wasmTimeModule: Module = Module.fromBinary(engine, module)
        val imports = WrapImportsJvm(store, memory).get()
        val instance = Instance(store, wasmTimeModule, imports)
        instance.use {
            val func = instance.getFunc(store, "_wrap_invoke").get()
            func.use {
                val fn = WasmFunctions.func(store, func, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32)
                val invoked = fn.call(method.length, args.size, env?.size ?: 0)
                if (invoked == 1) {
                    val result =
                        state.invoke.result ?: return Result.failure(IllegalStateException("Invoke result is missing."))
                    return Result.success(result)
                } else {
                    val exception =
                        state.invoke.error ?: return Result.failure(IllegalStateException("Invoke error is missing."))
                    return Result.failure(Exception(exception))
                }
            }
        }
    }

    private fun createMemory(store: Store<WasmModuleState>, module: ByteArray): Result<Memory> {
        val ENV_MEMORY_IMPORTS_SIGNATURE = byteArrayOf(
            0x65, 0x6e, 0x76, 0x06, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x02
        )

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