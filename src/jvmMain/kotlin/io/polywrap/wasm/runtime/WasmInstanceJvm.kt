package io.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.*
import io.polywrap.util.indexOfSubList

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
    actual fun createInstance(module: ByteArray, state: WasmModuleState): WasmInstance = WasmInstanceJvm(module, state)
}

/**
 * A platform-specific [WasmInstance] implementation for the JVM.
 *
 * @property wasmModule A [ByteArray] representing the WebAssembly module.
 * @property state A [WasmModuleState] object representing the module's state.
 */
class WasmInstanceJvm(wasmModule: ByteArray, state: WasmModuleState) : WasmInstance(wasmModule, state) {

    /**
     * Invokes a specified method in the WebAssembly instance.
     *
     * @param method A [String] representing the method name to be invoked.
     * @param args A [ByteArray] containing the arguments to be passed to the method.
     * @param env A [ByteArray] containing the environment data, or `null` if not provided.
     * @return A [Result] containing a [ByteArray] with the method invocation result or an exception if the invocation fails.
     */
    override fun invoke(method: String, args: ByteArray?, env: ByteArray?): Result<ByteArray> {
        val config = Config().maxWasmStack(1024 * 1024 * 2)
        val engine = Engine(config)
        val store: Store<WasmModuleState> = Store(state, engine)
        val memory: Memory = createMemory(store, wasmModule).getOrThrow()
        val wasmTimeModule: Module = Module.fromBinary(engine, wasmModule)

        val linker = Linker(engine)
        WrapImportsFactoryJvm.define(store, memory, linker)
        linker.module(store, "wrap", wasmTimeModule)

        val result: Result<ByteArray>
        try {
            val export = linker.get(store, "wrap", "_wrap_invoke").get().func()
            export.use {
                val fn = WasmFunctions.func(store, export, WasmValType.I32, WasmValType.I32, WasmValType.I32, WasmValType.I32)
                val isSuccess = fn.call(method.length, args?.size ?: 0, env?.size ?: 0)
                result = processResult(isSuccess == 1)
            }
        } finally {
            wasmTimeModule.dispose()
            linker.externs(store).forEach {
                val extern = it.extern()
                if (extern.type() == Extern.Type.FUNC) {
                    extern.func().dispose()
                } else {
                    extern.memory().dispose()
                }
            }
            linker.dispose()
            store.dispose()
            engine.dispose()
        }
        return result
    }

    private fun createMemory(store: Store<WasmModuleState>, module: ByteArray): Result<Memory> {
        val idx = module.toList().indexOfSubList(ENV_MEMORY_IMPORTS_SIGNATURE.toList())

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
        val memory = Memory(store, MemoryType(0, false))
        memory.grow(store, memoryInitialLimits)

        return Result.success(memory)
    }
}
