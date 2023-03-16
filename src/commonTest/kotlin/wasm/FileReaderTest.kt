package wasm

import eth.krisbitney.polywrap.wasm.FileReader
import eth.krisbitney.polywrap.wasm.FileReader.Companion.WRAP_MANIFEST_PATH
import eth.krisbitney.polywrap.wasm.FileReader.Companion.WRAP_MODULE_PATH
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import readTestResource
import kotlin.test.*

class FileReaderTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as";
    private val manifestPath = "$wrapperPath/wrap.info"
    private val modulePath = "$wrapperPath/wrap.wasm"

    private val baseFileReader = object : FileReader() {
        override suspend fun readFile(filePath: String): Result<ByteArray> {
            return readTestResource("$wrapperPath/$filePath");
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromMemoryWithBaseFileReader() = runTest {
        val manifest: ByteArray =  readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReader.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromMemoryWithoutBaseFileReader() = runTest {
        val manifest: ByteArray =  readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()

        val fileReader = FileReader.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
        )

        val manifestResult = fileReader.readFile(WRAP_MANIFEST_PATH)
        assertTrue(manifestResult.isSuccess)
        assertContentEquals(manifest, manifestResult.getOrNull())

        val wasmModuleResult = fileReader.readFile(WRAP_MODULE_PATH)
        assertTrue(wasmModuleResult.isSuccess)
        assertContentEquals(wasmModule, wasmModuleResult.getOrNull())

        val fileResult = fileReader.readFile("file.txt")
        assertTrue(fileResult.isFailure)
        assertEquals("File not found at file.txt.", fileResult.exceptionOrNull()?.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromManifest() = runTest {
        val manifest: ByteArray =  readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReader.fromManifest(
            manifest = manifest,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fromWasmModule() = runTest {
        val manifest: ByteArray =  readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReader.fromWasmModule(
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    private suspend fun compareResult(fileReader: FileReader, manifest: ByteArray, wasmModule: ByteArray): Unit {
        val manifestResult = fileReader.readFile(WRAP_MANIFEST_PATH)
        assertTrue(manifestResult.isSuccess)
        assertContentEquals(manifest, manifestResult.getOrNull())

        val wasmModuleResult = fileReader.readFile(WRAP_MODULE_PATH)
        assertTrue(wasmModuleResult.isSuccess)
        assertContentEquals(wasmModule, wasmModuleResult.getOrNull())

        val fileResult = fileReader.readFile("file.txt")
        assertTrue(fileResult.isSuccess)
        assertEquals("file.txt content", fileResult.getOrNull()?.decodeToString())
    }
}