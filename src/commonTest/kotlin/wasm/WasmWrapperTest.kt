package wasm

import emptyMockInvoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.wasm.WasmWrapper
import readTestResource
import kotlin.test.*

class WasmWrapperTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as"
    private val modulePath = "$wrapperPath/wrap.wasm"

    @Test
    fun canInvokeWrapper() {
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val wrapper = WasmWrapper(wasmModule)

        val result = wrapper.invoke(
            method = "i32Method",
            args = msgPackEncode(mapOf("first" to 1, "second" to 2)),
            env = null,
            invoker = emptyMockInvoker
        )
        assertNull(result.exceptionOrNull())

        val data = msgPackDecode<Int>(result.getOrThrow()).getOrNull()
        assertEquals(3, data)
    }
}
