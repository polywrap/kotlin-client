package plugin

import emptyMockInvoker
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.msgpack.msgPackDecode
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginPackageTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createWrapper() = runTest {
        val pluginPackage = mockPlugin(null)
        val result = pluginPackage.createWrapper()
        assertTrue(result.isSuccess)
    }
}

class PluginWrapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun canInvokeWrapper() = runTest {
        val wrapper = mockPlugin(null).createWrapper().getOrThrow()

        val invocation = InvokeOptions(
            uri = Uri("wrap://plugin/mock"),
            method = "add",
            args = msgPackEncode(mapOf("num" to 1, "ber" to 2)),
        )

        val result = wrapper.invoke(invocation, emptyMockInvoker).await()
        assertEquals(result.exceptionOrNull(), null)

        val data = msgPackDecode<Int>(result.getOrThrow()).getOrNull()
        assertEquals(3, data)
    }
}