package plugins

import emptyMockInvoker
import io.polywrap.configBuilder.polywrapClient
import io.polywrap.core.resolution.Uri
import io.polywrap.plugins.filesystem.FileSystemPlugin
import io.polywrap.plugins.filesystem.fileSystemPlugin
import io.polywrap.plugins.filesystem.wrap.*
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FileSystemPluginIntegrationTest {

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
    fun invokeByClient() {
        prepareTestFile()

        val uri = Uri("plugin/file-system@1.0.0")
        val plugin = fileSystemPlugin(null)

        val client = polywrapClient {
            setPackage(uri.toString() to plugin)
        }

        val result = client.invoke<ByteArray>(
            uri = uri,
            method = "readFile",
            args = mapOf("path" to testFile)
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val response = result.getOrThrow()
        assertEquals(testContent, response.decodeToString())
    }
}
