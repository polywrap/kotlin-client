package plugins

import emptyMockInvoker
import io.polywrap.plugins.filesystem.FileSystemPlugin
import io.polywrap.plugins.filesystem.wrapHardCoded.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FileSystemPluginTest {

    private val plugin = FileSystemPlugin()
    private val invoker = emptyMockInvoker
    private val testPath = "test_dir"
    private val testFile = "$testPath/test_file.txt"
    private val testContent = "Hello, World!"

    private fun prepareTestFile() = runBlocking {
        val argsMkdir = ArgsMkdir(testPath)
        plugin.mkdir(argsMkdir, invoker)

        val argsWrite = ArgsWriteFile(testFile, testContent.encodeToByteArray())
        plugin.writeFile(argsWrite, invoker)
    }

    private fun prepareTestDirectory() = runBlocking {
        val args = ArgsMkdir(testPath)
        plugin.mkdir(args, invoker)
    }

    private fun reset() = runBlocking {
        val fileExistsArgs = ArgsExists(testFile)
        if (plugin.exists(fileExistsArgs, invoker)) {
            val rmArgs = ArgsRm(testFile)
            plugin.rm(rmArgs, invoker)
        }

        val dirExistsArgs = ArgsExists(testPath)
        if (plugin.exists(dirExistsArgs, invoker)) {
            val rmDirArgs = ArgsRmdir(testPath)
            plugin.rmdir(rmDirArgs, invoker)
        }
    }

    @AfterTest
    fun afterEach() {
        reset()
    }

    @Test
    fun testReadFile() = runTest {
        prepareTestFile()

        val args = ArgsReadFile(testFile)
        val result = plugin.readFile(args, invoker)
        assertEquals(testContent, result.decodeToString())
    }

    @Test
    fun testReadFileAsString() = runTest {
        prepareTestFile()

        val args = ArgsReadFileAsString(testFile)
        val result = plugin.readFileAsString(args, invoker)
        assertEquals(testContent, result)
    }

    @Test
    fun testExists() = runTest {
        val args = ArgsExists(testFile)
        assertFalse(plugin.exists(args, invoker))

        prepareTestFile()
        assertTrue(plugin.exists(args, invoker))
    }

    @Test
    fun testWriteFile() = runTest {
        prepareTestDirectory()
        val args = ArgsWriteFile(testFile, testContent.encodeToByteArray())
        assertTrue(plugin.writeFile(args, invoker)!!)

        val readFileArgs = ArgsReadFile(testFile)
        val result = plugin.readFile(readFileArgs, invoker)
        assertEquals(testContent, result.decodeToString())
    }

    @Test
    fun testMkdir() = runTest {
        val args = ArgsMkdir(testPath)
        assertTrue(plugin.mkdir(args, invoker)!!)

        val existsArgs = ArgsExists(testPath)
        assertTrue(plugin.exists(existsArgs, invoker))
    }

    @Test
    fun testRm() = runTest {
        prepareTestFile()

        val args = ArgsRm(testFile)
        assertTrue(plugin.rm(args, invoker)!!)

        val existsArgs = ArgsExists(testFile)
        assertFalse(plugin.exists(existsArgs, invoker))
    }

    @Test
    fun testRmdir() = runTest {
        prepareTestDirectory()

        val args = ArgsRmdir(testPath)
        assertTrue(plugin.rmdir(args, invoker)!!)

        val existsArgs = ArgsExists(testPath)
        assertFalse(plugin.exists(existsArgs, invoker))
    }
}
