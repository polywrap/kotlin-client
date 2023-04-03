package util

import eth.krisbitney.polywrap.util.absolute
import eth.krisbitney.polywrap.util.readFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okio.Path.Companion.toPath
import kotlin.test.*

class IoTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun readFileReturnsFileContents() = runTest {
        val path = "".toPath().resolve("src/commonTest/resources/util/test.txt").absolute().getOrThrow()
        val result = readFile(path.toString()).await()
        assertNull(result.exceptionOrNull())

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
