package wasm

import io.polywrap.core.types.FileReader
import io.polywrap.wasm.FileReaderFactory
import readTestResource
import kotlin.test.*

class FileReaderTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as"
    private val manifestPath = "$wrapperPath/wrap.info"
    private val modulePath = "$wrapperPath/wrap.wasm"

    private val baseFileReader = object : FileReader() {
        override fun readFile(filePath: String): Result<ByteArray> {
            return readTestResource("$wrapperPath/$filePath")
        }
    }

    @Test
    fun fromMemoryWithBaseFileReader() {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    @Test
    fun fromMemoryWithoutBaseFileReader() {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()

        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule
        )

        val manifestResult = fileReader.readFile(FileReader.WRAP_MANIFEST_PATH)
        assertTrue(manifestResult.isSuccess)
        assertContentEquals(manifest, manifestResult.getOrNull())

        val wasmModuleResult = fileReader.readFile(FileReader.WRAP_MODULE_PATH)
        assertTrue(wasmModuleResult.isSuccess)
        assertContentEquals(wasmModule, wasmModuleResult.getOrNull())

        val fileResult = fileReader.readFile("file.txt")
        assertTrue(fileResult.isFailure)
        assertEquals("File not found at file.txt.", fileResult.exceptionOrNull()?.message)
    }

    @Test
    fun fromManifest() {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromManifest(
            manifest = manifest,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    @Test
    fun fromWasmModule() {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromWasmModule(
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        compareResult(fileReader, manifest, wasmModule)
    }

    private fun compareResult(fileReader: FileReader, manifest: ByteArray, wasmModule: ByteArray) {
        val manifestResult = fileReader.readFile(FileReader.WRAP_MANIFEST_PATH)
        assertTrue(manifestResult.isSuccess)
        assertContentEquals(manifest, manifestResult.getOrNull())

        val wasmModuleResult = fileReader.readFile(FileReader.WRAP_MODULE_PATH)
        assertTrue(wasmModuleResult.isSuccess)
        assertContentEquals(wasmModule, wasmModuleResult.getOrNull())

        val fileResult = fileReader.readFile("file.txt")
        assertTrue(fileResult.isSuccess)
        assertEquals("file.txt content", fileResult.getOrNull()?.decodeToString())
    }
}
