package io.polywrap.wasm

import io.polywrap.core.AbortHandler
import io.polywrap.core.Wrapper
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiWasmWrapper

/**
 * Represents a WebAssembly (Wasm) wrapper for executing Wasm code.
 *
 * @property wasmModule The WebAssembly module as a ByteArray.
 */
@OptIn(ExperimentalUnsignedTypes::class)
data class WasmWrapper(val wasmModule: ByteArray) : Wrapper, AutoCloseable {

    private val ffiWrapper = FfiWasmWrapper(wasmModule.toUByteArray().toList())

    /**
     * Invoke the Wrapper based on the provided options.
     *
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param invoker The invoker will be used for any sub-invocations that occur.
     * @param abortHandler An [AbortHandler] to be called when the invocation is aborted.
     * @return A list of MessagePack-encoded bytes representing the invocation result
     * @throws FfiException
     */
    override fun invoke(
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        invoker: FfiInvoker,
        abortHandler: AbortHandler?
    ): List<UByte> = ffiWrapper.invoke(method, args, env, invoker, abortHandler)

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
