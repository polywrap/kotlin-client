package client.wrapFeatures

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.core.resolution.Uri
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals

class SubinvokeTestCase {
    @Test
    fun testSubinvoke() {
        val subinvokeUri = "fs/$pathToTestWrappers/subinvoke/00-subinvoke/implementations/rs"
        val wrapperUri = "fs/$pathToTestWrappers/subinvoke/01-invoke/implementations/rs"

        val config = ClientConfigBuilder()
            .addDefaults()
            .addRedirect("ens/imported-subinvoke.eth" to subinvokeUri)
            .build()
        val client = PolywrapClient(config)

        val result = client.invoke<Int>(
            uri = Uri(wrapperUri),
            method = "addAndIncrement",
            args = mapOf("a" to 1, "b" to 1)
        )
        if (result.isFailure) {
            throw result.exceptionOrNull()!!
        }
        assertEquals(3, result.getOrThrow())
    }
}
