package io.polywrap.wasm.runtime

import io.github.kawamuray.wasmtime.Memory
import io.github.kawamuray.wasmtime.Store

class WrapImportsJvm(private val store: Store<WasmModuleState>, memory: Memory) : CommonWrapImports<Memory>(store.data(), memory) {
    override fun readBytes(source: Memory, srcOffset: Int, length: Int): ByteArray {
        val src: ByteArray = source.buffer(store).array()
        val destination = ByteArray(length)
        src.copyInto(destination, 0, srcOffset, srcOffset + length)
        return destination
    }

    override fun writeBytes(source: ByteArray, destination: Memory, dstOffset: Int): Memory {
        val dst: ByteArray = destination.buffer(store).array()
        source.copyInto(dst, dstOffset, 0, source.size)
        return destination
    }
}
