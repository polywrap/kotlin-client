package plugin

import emptyMockInvoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import mockPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PluginPackageTest {
    @Test
    fun createWrapper() {
        val pluginPackage = mockPlugin(null)
        val result = pluginPackage.createWrapper()
        assertNotNull(result)
    }
}

class PluginWrapperTest {

    @Test
    fun canInvokeWrapper() {
        val wrapper = mockPlugin(null).createWrapper()

        val result = wrapper.invoke(
            method = "add",
            args = msgPackEncode(mapOf("num" to 1, "ber" to 2)),
            env = null,
            invoker = emptyMockInvoker,
            abortHandler = null
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val data = msgPackDecode<Int>(result.getOrThrow()).getOrNull()
        assertEquals(3, data)
    }
}
