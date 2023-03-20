package util

import eth.krisbitney.polywrap.util.readFile
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*


class IoTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun readFileReturnsFileContents() = runTest {
        val result = readFile("/Users/kris/IdeaProjects/krisbitney/polywrap-kt/src/commonTest/resources/util/test.txt").await()
        assertTrue(result.isSuccess)

        val expected = "Hello, world!".encodeToByteArray()
        assertContentEquals(expected, result.getOrNull())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun readFileReturnsErrorIfFileDoesNotExist() = runTest {
        val result = readFile("nonexistent.txt").await()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Throwable)
    }

}