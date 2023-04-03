package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.externals.WebAssembly
import org.khronos.webgl.Int8Array

class WrapImportsJs(
    state: WasmModuleState,
    memory: WebAssembly.Memory
) : CommonWrapImports<WebAssembly.Memory>(state, memory) {
    override fun readBytes(source: WebAssembly.Memory, srcOffset: Int, length: Int): ByteArray {
        val src: ByteArray = Int8Array(source.buffer).unsafeCast<ByteArray>()
        val destination = ByteArray(length)
        src.copyInto(destination, 0, srcOffset, srcOffset + length)
        return destination
    }

    override fun writeBytes(source: ByteArray, destination: WebAssembly.Memory, dstOffset: Int): WebAssembly.Memory {
        val dst: ByteArray = Int8Array(destination.buffer).unsafeCast<ByteArray>()
        source.copyInto(dst, dstOffset, 0, source.size)
        return destination
    }
}
