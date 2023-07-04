package client.typeTestCases

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.msgpack.GenericMap
import io.polywrap.core.msgpack.toGenericMap
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MapTestCase {

    private val uri = Uri("fs/$pathToTestWrappers/map-type/implementations/rs")
    private val client = ConfigBuilder().addDefaults().build()

    @Serializable
    private data class CustomMap(
        val map: GenericMap<String, Int>,
        val nestedMap: GenericMap<String, GenericMap<String, Int>>
    )

    @Test
    fun testReturnMap() = runTest {
        @Serializable
        data class ArgsReturnMap(val map: GenericMap<String, Int>)

        val mapClass = mapOf("Hello" to 1, "Heyo" to 50).toGenericMap()
        val result = client.invoke<ArgsReturnMap, GenericMap<String, Int>>(
            uri = uri,
            method = "returnMap",
            args = ArgsReturnMap(mapClass)
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertEquals(mapClass, result.getOrThrow())
    }

    @Test
    fun testGetKey() = runTest {
        val mapClass = mapOf("Hello" to 1, "Heyo" to 50).toGenericMap()
        val nestedMapClass = mapOf("Nested" to mapClass).toGenericMap()
        val result = client.invoke<Int>(
            uri = uri,
            method = "getKey",
            args = mapOf(
                "foo" to CustomMap(mapClass, nestedMapClass),
                "key" to "Hello"
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertEquals(mapClass.map["Hello"], result.getOrThrow())
    }

    @Test
    fun testReturnCustomMap() = runTest {
        val mapClass = mapOf("Hello" to 1, "Heyo" to 50).toGenericMap()
        val nestedMapClass = mapOf("Nested" to mapClass).toGenericMap()
        val result = client.invoke<CustomMap>(
            uri = uri,
            method = "returnCustomMap",
            args = mapOf("foo" to CustomMap(mapClass, nestedMapClass))
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertEquals(CustomMap(mapClass, nestedMapClass), result.getOrThrow())
    }

    @Test
    fun testReturnNestedMap() = runTest {
        @Serializable
        data class ArgsReturnNestedMap(val foo: GenericMap<String, GenericMap<String, Int>>)

        val mapClass = mapOf("Hello" to 1, "Heyo" to 50).toGenericMap()
        val nestedMapClass = mapOf("Nested" to mapClass).toGenericMap()
        val result = client.invoke<ArgsReturnNestedMap, GenericMap<String, GenericMap<String, Int>>>(
            uri = uri,
            method = "returnNestedMap",
            args = ArgsReturnNestedMap(nestedMapClass)
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertEquals(nestedMapClass, result.getOrThrow())
    }
}
