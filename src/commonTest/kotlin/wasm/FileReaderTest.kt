package wasm

import eth.krisbitney.polywrap.core.types.FileReader
import eth.krisbitney.polywrap.wasm.FileReaderFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import readTestResource
import kotlin.test.*

class FileReaderTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as"
    private val manifestPath = "$wrapperPath/wrap.info"
    private val modulePath = "$wrapperPath/wrap.wasm"

    private val baseFileReader = object : FileReader() {
        override suspend fun readFile(filePath: String): Deferred<Result<ByteArray>> = coroutineScope {
            async {
                readTestResource("$wrapperPath/$filePath")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromMemoryWithBaseFileReader() = runTest {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromMemoryWithoutBaseFileReader() = runTest {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()

        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule
        )

        val manifestResult = fileReader.readFile(FileReader.WRAP_MANIFEST_PATH).await()
        assertTrue(manifestResult.isSuccess)
        assertContentEquals(manifest, manifestResult.getOrNull())

        val wasmModuleResult = fileReader.readFile(FileReader.WRAP_MODULE_PATH).await()
        assertTrue(wasmModuleResult.isSuccess)
        assertContentEquals(wasmModule, wasmModuleResult.getOrNull())

        val fileResult = fileReader.readFile("file.txt").await()
        assertTrue(fileResult.isFailure)
        assertEquals("File not found at file.txt.", fileResult.exceptionOrNull()?.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromManifest() = runTest {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromManifest(
            manifest = manifest,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromWasmModule() = runTest {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromWasmModule(
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    private suspend fun compareResult(fileReader: FileReader, manifest: ByteArray, wasmModule: ByteArray) {
        val manifestResult = fileReader.readFile(FileReader.WRAP_MANIFEST_PATH).await()
        assertTrue(manifestResult.isSuccess)
        assertContentEquals(manifest, manifestResult.getOrNull())

        val wasmModuleResult = fileReader.readFile(FileReader.WRAP_MODULE_PATH).await()
        assertTrue(wasmModuleResult.isSuccess)
        assertContentEquals(wasmModule, wasmModuleResult.getOrNull())

        val fileResult = fileReader.readFile("file.txt").await()
        assertTrue(fileResult.isSuccess)
        assertEquals("file.txt content", fileResult.getOrNull()?.decodeToString())
    }
}
