package io.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.Memory
import io.github.kawamuray.wasmtime.Store

/**
 * A concrete implementation of [CommonWrapImports] for JVM environments.
 * @param store The [Store] instance associated with the current instance of the [WasmInstance].
 * @param memory The [Memory] instance to read and write from.
 */
class WrapImportsJvm(private val store: Store<WasmModuleState>, memory: Memory) : CommonWrapImports<Memory>(store.data(), memory) {

    /**
     * Reads a specified number of bytes from the source memory buffer, starting at
     * the specified offset, into a new byte array. The resulting byte array contains
     * the bytes read from the source memory buffer, starting at the specified offset and
     * continuing for the specified length.
     *
     * @param source the source buffer to read from
     * @param srcOffset the offset in the source array at which to start reading
     * @param length the number of bytes to read from the source array
     * @return a new byte array containing the bytes read from the source array
     */
    override fun readBytes(source: Memory, srcOffset: Int, length: Int): ByteArray {
        val src: ByteArray = source.buffer(store).array()
        val destination = ByteArray(length)
        src.copyInto(destination, 0, srcOffset, srcOffset + length)
        return destination
    }

    /**
     * Writes the contents of the source byte array to the destination memory buffer,
     * starting at the specified destination offset. The source byte array is copied
     * into the destination buffer, overwriting any existing data in the destination
     * array at and after the specified offset.
     *
     * @param source the source byte array to be copied
     * @param destination the destination buffer to copy the source array to
     * @param dstOffset the offset in the destination array at which to start writing
     * @return the destination buffer
     */
    override fun writeBytes(source: ByteArray, destination: Memory, dstOffset: Int): Memory {
        val dst: ByteArray = destination.buffer(store).array()
        source.copyInto(dst, dstOffset, 0, source.size)
        return destination
    }
}
