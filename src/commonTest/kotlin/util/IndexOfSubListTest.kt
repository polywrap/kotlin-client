package util

import io.polywrap.util.indexOfSubList
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexOfSubListTest {

    @Test
    fun `test subList exists in list`() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val subList = listOf(4, 5, 6)

        val result = list.indexOfSubList(subList)

        assertEquals(3, result)
    }

    @Test
    fun `test subList does not exist in list`() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val subList = listOf(10, 11, 12)

        val result = list.indexOfSubList(subList)

        assertEquals(-1, result)
    }

    @Test
    fun `test empty subList`() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val subList = emptyList<Int>()

        val result = list.indexOfSubList(subList)

        assertEquals(0, result)
    }

    @Test
    fun `test complex subList exists in list`() {
        val list = listOf("A", "B", "A", "B", "A", "C", "A", "B", "A", "B", "A", "B")
        val subList = listOf("A", "B", "A", "B", "A", "C")

        val result = list.indexOfSubList(subList)

        assertEquals(0, result)
    }

    @Test
    fun `test subList is the same as list`() {
        val list = listOf("A", "B", "C", "D", "E")
        val subList = listOf("A", "B", "C", "D", "E")

        val result = list.indexOfSubList(subList)

        assertEquals(0, result)
    }

    @Test
    fun `test subList is longer than list`() {
        val list = listOf(1, 2, 3)
        val subList = listOf(1, 2, 3, 4, 5)

        val result = list.indexOfSubList(subList)

        assertEquals(-1, result)
    }

    @Test
    fun `test finding ENV_MEMORY_IMPORTS_SIGNATURE in wasm module`() {
        val wasmModule = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00,
            0x65, 0x6e, 0x76, 0x06, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x02
        )
        val envMemoryImportsSignature = byteArrayOf(
            0x65, 0x6e, 0x76, 0x06, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x02
        )

        val result = wasmModule.toList().indexOfSubList(envMemoryImportsSignature.toList())

        assertEquals(8, result)
    }

    @Test
    fun `test finding ENV_MEMORY_IMPORTS_SIGNATURE at the end of wasm module`() {
        val wasmModule = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00,
            0x05, 0x04, 0x00, 0x01, 0x00, 0x00,
            0x65, 0x6e, 0x76, 0x06, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x02
        )
        val envMemoryImportsSignature = byteArrayOf(
            0x65, 0x6e, 0x76, 0x06, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x02
        )

        val result = wasmModule.toList().indexOfSubList(envMemoryImportsSignature.toList())

        assertEquals(14, result)
    }
}
