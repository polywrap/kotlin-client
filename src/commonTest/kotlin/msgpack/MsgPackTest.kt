package msgpack

import io.polywrap.core.WrapEnv
import io.polywrap.core.msgpack.MsgPackMap
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.msgpack.toMsgPackMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.test.*

class MsgPackTest {
    @Test
    fun shouldEncodeAndDecodeObject() {
        @Serializable
        data class CustomObject(
            val firstKey: String,
            val secondKey: String
        )
        val expectedBytes = intArrayOf(
            130, 168, 102, 105, 114, 115, 116, 75,
            101, 121, 170, 102, 105, 114, 115, 116,
            86, 97, 108, 117, 101, 169, 115, 101,
            99, 111, 110, 100, 75, 101, 121, 171,
            115, 101, 99, 111, 110, 100, 86, 97,
            108, 117, 101
        ).map(Int::toByte).toByteArray()

        val customObject = CustomObject("firstValue", "secondValue")
        val encoded = msgPackEncode(customObject)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded = msgPackDecode<CustomObject>(encoded).getOrThrow()
        assertEquals(customObject, decoded)
    }

    @Test
    fun shouldEncodeAndDecodeMap() {
        val customMap = mapOf(
            "firstKey" to "firstValue",
            "secondKey" to "secondValue"
        )
        val msgPackMap = MsgPackMap(customMap)

        // 199 means Ext8, 43 means 43 bytes, 1 means generic map ext type,
        // and the remainder is the 43 bytes of the map
        val expectedBytes = intArrayOf(
            199, 43, 1, 130, 168, 102, 105, 114, 115,
            116, 75, 101, 121, 170, 102, 105, 114, 115,
            116, 86, 97, 108, 117, 101, 169, 115, 101,
            99, 111, 110, 100, 75, 101, 121, 171, 115,
            101, 99, 111, 110, 100, 86, 97, 108, 117,
            101
        ).map(Int::toByte).toByteArray()

        val encoded = msgPackEncode(msgPackMap)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded = msgPackDecode<MsgPackMap<String, String>>(encoded).getOrThrow()
        assertEquals(msgPackMap, decoded)
    }

    @Test
    fun shouldEncodeAndDecodeNestedMap() {
        val customMap: Map<String, MsgPackMap<String, String>> = mapOf(
            "firstKey" to MsgPackMap(mapOf("one" to "1")),
            "secondKey" to MsgPackMap(mapOf("second" to "2"))
        )
        val msgPackMap = MsgPackMap(customMap)

        val expectedBytes = intArrayOf(
            199, 43, 1, 130, 168, 102, 105, 114, 115,
            116, 75, 101, 121, 199, 7, 1, 129, 163,
            111, 110, 101, 161, 49, 169, 115, 101, 99,
            111, 110, 100, 75, 101, 121, 199, 10, 1,
            129, 166, 115, 101, 99, 111, 110, 100, 161,
            50
        ).map(Int::toByte).toByteArray()

        val encoded = msgPackEncode(msgPackMap)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded = msgPackDecode<MsgPackMap<String, MsgPackMap<String, String>>>(encoded).getOrThrow()
        assertEquals(msgPackMap, decoded)
    }

    @Test
    fun shouldEncodeAndDecodeMapOfBytes() {
        val customMap: Map<String, ByteArray> = mapOf(
            "firstKey" to byteArrayOf(1, 2, 3),
            "secondKey" to byteArrayOf(3, 2, 1)
        )
        val msgPackMap = MsgPackMap(customMap)

        val expectedBytes = intArrayOf(
            199, 30, 1, 130, 168, 102, 105, 114, 115,
            116, 75, 101, 121, 196, 3, 1, 2, 3, 169,
            115, 101, 99, 111, 110, 100, 75, 101, 121,
            196, 3, 3, 2, 1
        ).map(Int::toByte).toByteArray()

        val encoded = msgPackEncode(msgPackMap)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded = msgPackDecode<MsgPackMap<String, ByteArray>>(encoded).getOrThrow()
        assertEquals(msgPackMap.map.keys.toString(), decoded.map.keys.toString())
        val expectedValues = msgPackMap.map.values.map { it.contentToString() }
        val receivedValues = decoded.map.values.map { it.contentToString() }
        assertEquals(expectedValues, receivedValues)
    }

    @Test
    fun shouldEncodeAndDecodeMapOfStringAny() {
        val env: WrapEnv = mapOf(
            "firstKey" to "firstValue",
            "secondKey" to "secondValue"
        )

        val expectedBytes = intArrayOf(
            130, 168, 102, 105, 114, 115, 116, 75,
            101, 121, 170, 102, 105, 114, 115, 116,
            86, 97, 108, 117, 101, 169, 115, 101,
            99, 111, 110, 100, 75, 101, 121, 171,
            115, 101, 99, 111, 110, 100, 86, 97,
            108, 117, 101
        ).map(Int::toByte).toByteArray()

        val encoded = msgPackEncode(serializer(), env)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded: Result<WrapEnv> = msgPackDecode(serializer(), encoded)
        assertEquals(env, decoded.getOrThrow())
    }

    @Test
    fun shouldEncodeAndDecodeObjectWithMapProperty() {
        @Serializable
        data class CustomObject(
            val firstKey: String,
            val secondKey: MsgPackMap<String, String>?
        )

        val map = mapOf(
            "firstKey" to "firstKey=24",
            "secondKey" to "secondValue"
        ).toMsgPackMap()

        val customObject = CustomObject("firstValue", map)
        val encoded = msgPackEncode(customObject)
        val decoded = msgPackDecode<CustomObject>(encoded).getOrThrow()
        assertEquals(customObject, decoded)

        val customObjectWithNull = CustomObject("firstValue", null)
        val encodedWithNull = msgPackEncode(customObjectWithNull)
        val decodedWithNull = msgPackDecode<CustomObject>(encodedWithNull).getOrThrow()
        assertEquals(customObjectWithNull, decodedWithNull)
    }
}
