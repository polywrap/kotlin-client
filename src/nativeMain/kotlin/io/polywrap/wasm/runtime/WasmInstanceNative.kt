package io.polywrap.wasm.runtime

import io.github.krisbitney.wasmtime.*
import io.github.krisbitney.wasmtime.util.FuncFactory
import io.github.krisbitney.wasmtime.wasm.ValType
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
    actual fun createInstance(
        module: ByteArray,
        state: WasmModuleState
    ): WasmInstance = WasmInstanceNative(module, state)
}

/**
 * A platform-specific [WasmInstance] implementation for native environments.
 *
 * @property wasmModule A [ByteArray] representing the WebAssembly module.
 * @property state A [WasmModuleState] object representing the module's state.
 */
class WasmInstanceNative(wasmModule: ByteArray, state: WasmModuleState) : WasmInstance(wasmModule, state) {

    /**
     * Invokes a specified method in the WebAssembly instance.
     *
     * @param method A [String] representing the method name to be invoked.
     * @param args A [ByteArray] containing the arguments to be passed to the method.
     * @param env A [ByteArray] containing the environment data, or `null` if not provided.
     * @return A [Result] containing a [ByteArray] with the method invocation result or an exception if the invocation fails.
     */
    @OptIn(ExperimentalStdlibApi::class)
    override fun invoke(method: String, args: ByteArray?, env: ByteArray?): Result<ByteArray> {
        return Engine() {
            setMaxWasmStack(4096u)
        }.use { engine ->
            Store(engine, state).use { store ->
                Module(engine, wasmModule).use { wasmTimeModule ->
                    Linker(engine).use { linker ->
                        val memory: Memory = createMemory(store, wasmModule).getOrThrow()
                        WrapImportsFactoryNative.define(store, memory, linker)
                        val instance = linker.instantiate(store, wasmTimeModule)

                        val export = instance.getExport("_wrap_invoke") as? Func
                            ?: throw Exception("_wrap_invoke export not found")
                        val fn = FuncFactory.producer(export, ValType.I32(), ValType.I32(), ValType.I32(), ValType.I32())
                        val isSuccess = fn(method.length, args?.size ?: 0, env?.size ?: 0)
                        processResult(isSuccess == 1)
                    }
                }
            }
        }
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

        val memoryInitialLimits = module[idx + ENV_MEMORY_IMPORTS_SIGNATURE.size + 1].toUInt()
        val memory = Memory(store)
        memory.grow(memoryInitialLimits)

        return Result.success(memory)
    }
}
