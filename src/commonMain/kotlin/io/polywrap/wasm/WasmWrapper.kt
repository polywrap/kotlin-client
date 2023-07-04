package io.polywrap.wasm

import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiWasmWrapper

/**
 * Represents a WebAssembly (Wasm) wrapper for executing Wasm code.
 *
 * @property wasmModule The WebAssembly module as a ByteArray.
 */
@OptIn(ExperimentalUnsignedTypes::class)
data class WasmWrapper(val wasmModule: ByteArray) : Wrapper, AutoCloseable {

    private val ffiWrapper = FfiWasmWrapper(wasmModule.asUByteArray().toList())

    override fun invoke(
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        invoker: FfiInvoker
    ): List<UByte> = ffiWrapper.invoke(method, args, env, invoker)

    override fun invoke(
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        invoker: Invoker
    ): Result<ByteArray> = runCatching {
        ffiWrapper.invoke(
            method = method,
            args = args?.asUByteArray()?.toList(),
            env = env?.asUByteArray()?.toList(),
            invoker = invoker.ffiInvoker
        ).toUByteArray().asByteArray()
    }

    override fun close() = ffiWrapper.close()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WasmWrapper

        if (!wasmModule.contentEquals(other.wasmModule)) return false
        return ffiWrapper == other.ffiWrapper
    }

    override fun hashCode(): Int {
        var result = wasmModule.contentHashCode()
        result = 31 * result + ffiWrapper.hashCode()
        return result
    }
}
