package wasm

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.core.types.Wrapper
import eth.krisbitney.polywrap.msgpack.msgPackDecode
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import eth.krisbitney.polywrap.wasm.WasmWrapper
import kotlinx.coroutines.Deferred
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

        val invoker = object : Invoker {
            override suspend fun invokeWrapper(wrapper: Wrapper, options: InvokeOptions): Deferred<Result<ByteArray>> {
                TODO("Not yet implemented")
            }

            override suspend fun invoke(options: InvokeOptions): Deferred<Result<ByteArray>> {
                TODO("Not yet implemented")
            }

            override suspend fun getImplementations(
                uri: Uri,
                applyResolution: Boolean,
                resolutionContext: UriResolutionContext?
            ): Deferred<Result<List<Uri>>> {
                TODO("Not yet implemented")
            }
        }

        val result = wrapper.invoke(invocation, invoker).await()
        assertTrue(result.isSuccess)

        val data = msgPackDecode<Int>(result.getOrThrow()).getOrNull()
        assertEquals(3, data)
    }
}