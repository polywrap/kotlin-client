package wasm

import emptyMockInvoker
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.msgpack.msgPackDecode
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import eth.krisbitney.polywrap.wasm.WasmWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import readTestResource
import kotlin.test.*

class WasmWrapperTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as"
    private val modulePath = "$wrapperPath/wrap.wasm"

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun canInvokeWrapper() = runTest {
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val wrapper = WasmWrapper(wasmModule)

        val invocation = InvokeOptions(
            uri = Uri("wrap://ens/WasmWrapperTest/canInvokeWrapper"),
            method = "i32Method",
            args = msgPackEncode(mapOf("first" to 1, "second" to 2)),
        )

        val result = wrapper.invoke(invocation, emptyMockInvoker).await()
        assertTrue(result.isSuccess)

        val data = msgPackDecode<Int>(result.getOrThrow()).getOrNull()
        assertEquals(3, data)
    }
}