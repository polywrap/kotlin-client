package io.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.*
import java.util.*

actual object WasmInstanceFactory {
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJvm(module, state)
}

class WasmInstanceJvm(module: ByteArray, state: WasmModuleState) : WasmInstance(module, state) {

    override suspend fun invoke(method: String, args: ByteArray, env: ByteArray?): Result<ByteArray> {
        val config = Config().maxWasmStack(1024 * 1024 * 2)
        val engine = Engine(config)
        val store: Store<WasmModuleState> = Store(state, engine)
        val memory: Memory = createMemory(store, module).getOrThrow()
        val wasmTimeModule: Module = Module.fromBinary(engine, module)

        val linker = Linker(engine)
        val importNames = wasmTimeModule.imports().map { it.name() }
        val imports = WrapImportsFactoryJvm.get(store, memory, importNames, linker)
        linker.module(store, "wrap", wasmTimeModule)

        val instance = Instance(store, wasmTimeModule, imports)
        val result: Result<ByteArray>
        try {
            val export = linker.get(store, "wrap", "_wrap_invoke").get().func()
            export.use {
                val isSuccess = export.call(
                    store,
                    Val.fromI32(method.length),
                    Val.fromI32(args.size),
                    Val.fromI32(env?.size ?: 0)
                )[0].i32()
                result = processResult(isSuccess == 1)
            }
        } finally {
            instance.close()
            config.close()
            engine.close()
            store.close()
            memory.close()
            wasmTimeModule.close()
            linker.close()
            imports.forEach {
                if (it.type() == Extern.Type.FUNC) {
                    it.func().close()
                } else {
                    it.memory().close()
                }
            }
        }
        return result
    }

    private fun createMemory(store: Store<WasmModuleState>, module: ByteArray): Result<Memory> {
        val idx = Collections.indexOfSubList(module.toList(), ENV_MEMORY_IMPORTS_SIGNATURE.toList())

        if (idx == -1) {
            val error = Error(
                "Unable to find Wasm memory import section. " +
                    "Modules must import memory from the \"env\" module's " +
                    "\"memory\" field like so: " +
                    "(import \"env\" \"memory\" (memory (;0;) #))"
            )
            return Result.failure(error)
        }

        val memoryInitialLimits = module[idx + ENV_MEMORY_IMPORTS_SIGNATURE.size + 1].toLong()
        val memory = Memory(
            store,
            MemoryType(memoryInitialLimits, false)
        )

        return Result.success(memory)
    }
}
