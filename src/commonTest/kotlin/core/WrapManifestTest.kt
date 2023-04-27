package core

import encodedTestManifest
import io.polywrap.core.wrap.WrapManifest
import readTestResource
import testManifest
import kotlin.test.*

class WrapManifestTest {

    @Test
    fun shouldSerializeAndDeserializeManifest() {
        val encoded = WrapManifest.serialize(testManifest)
        val decoded = WrapManifest.deserialize(encoded).getOrThrow()
        assertEquals(testManifest, decoded)
    }

    @Test
    fun shouldDeserializeManifest() {
        val decoded = WrapManifest.deserialize(encodedTestManifest).getOrThrow()
        assertEquals(testManifest, decoded)
    }

    @Test
    fun shouldThrowIncorrectVersionFormatError() {
        assertFailsWith<IllegalArgumentException>("Unsupported WrapManifest version: bad-str. Expected version: 0.1") {
            testManifest.copy(version = "bad-str")
        }
    }

    @Test
    fun shouldThrowIfNameFieldIncorrectPattern() {
        assertFailsWith<IllegalArgumentException>("WrapManifest name contains invalid characters: foo bar baz \$%##\$@#\$@#\$@#\$#\$") {
            testManifest.copy(name = "foo bar baz $%##$@#$@#$@#$#$")
        }
    }

    @Test
    fun shouldThrowWrongTypeError() {
        assertFailsWith<IllegalArgumentException>("Unsupported WrapManifest type: true. Supported types: wasm, plugin, interface") {
            testManifest.copy(type = "true")
        }
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_Asyncify() {
        val testCase = "wrappers/asyncify/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_BigNumberType() {
        val testCase = "wrappers/bignumber-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_BytesType() {
        val testCase = "wrappers/bytes-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_EnumType() {
        val testCase = "wrappers/enum-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_EnvType() {
        val testCaseExternal = "wrappers/env-type/00-external/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCaseExternal)

        val testCaseMain = "wrappers/env-type/01-main/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCaseMain)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_InterfaceInvoke() {
        val testCaseInterface = "wrappers/interface-invoke/00-interface/wrap.info"
        testSerializeAndDeserializeManifest(testCaseInterface)

        val testCaseImplementation = "wrappers/interface-invoke/01-implementation/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCaseImplementation)

        val testCaseWrapper = "wrappers/interface-invoke/02-wrapper/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCaseWrapper)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_JsonType() {
        val testCase = "wrappers/json-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_MapType() {
        val testCase = "wrappers/map-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_NumbersType() {
        val testCase = "wrappers/numbers-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_ObjectType() {
        val testCase = "wrappers/object-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    @Test
    fun shouldSerializeAndDeserializeManifest_SimpleMapType() {
        val testCase = "wrappers/simple-map-type/implementations/as/wrap.info"
        testSerializeAndDeserializeManifest(testCase)
    }

    private fun testSerializeAndDeserializeManifest(testCase: String) {
        val bytes = readTestResource(testCase).getOrThrow()
        val manifest = WrapManifest.deserialize(bytes).getOrThrow()
        WrapManifest.serialize(manifest)
    }
}
