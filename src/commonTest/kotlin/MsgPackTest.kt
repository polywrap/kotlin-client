import eth.krisbitney.polywrap.core.msgpack.msgpackDecode
import eth.krisbitney.polywrap.core.msgpack.msgpackEncode
import kotlinx.serialization.Serializable
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
        val encoded = msgpackEncode(customObject)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded = msgpackDecode<CustomObject>(encoded)
        assertEquals(customObject, decoded)
    }

    @Test @Ignore
    fun shouldEncodeAndDecodeMap() {
        val customMap = mapOf(
            "firstKey" to "firstValue",
            "secondKey" to "secondValue"
        )
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

        val encoded = msgpackEncode(customMap)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded: Map<String, String> = msgpackDecode(encoded)
        assertEquals(customMap, decoded)
    }

    @Test @Ignore
    fun shouldEncodeAndDecodeNestedMap() {
        val customMap: Map<String, Map<String, String>> = mapOf(
            "firstKey" to mapOf("one" to "1"),
            "secondKey" to mapOf("second" to "2")
        )
        val expectedBytes = intArrayOf(
            199, 43, 1, 130, 168, 102, 105, 114, 115,
            116, 75, 101, 121, 199, 7, 1, 129, 163,
            111, 110, 101, 161, 49, 169, 115, 101, 99,
            111, 110, 100, 75, 101, 121, 199, 10, 1,
            129, 166, 115, 101, 99, 111, 110, 100, 161,
            50
        ).map(Int::toByte).toByteArray()

        val encoded = msgpackEncode(customMap)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded: Map<String, Map<String, String>> = msgpackDecode(encoded)
        assertEquals(customMap, decoded)
    }

    @Test @Ignore
    fun shouldEncodeAndDecodeMapOfBytes() {
        val customMap: Map<String, ByteArray> = mapOf(
            "firstKey" to byteArrayOf(1, 2, 3),
            "secondKey" to byteArrayOf(3, 2, 1)
        )
        val expectedBytes = intArrayOf(
            199, 30, 1, 130, 168, 102, 105, 114, 115,
            116, 75, 101, 121, 196, 3, 1, 2, 3, 169,
            115, 101, 99, 111, 110, 100, 75, 101, 121,
            196, 3, 3, 2, 1
        ).map(Int::toByte).toByteArray()

        val encoded = msgpackEncode(customMap)
        assertTrue(encoded.contentEquals(expectedBytes))

        val decoded: Map<String, ByteArray> = msgpackDecode(encoded)
        assertEquals(customMap, decoded)
    }
}