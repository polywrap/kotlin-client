package wasm

import io.polywrap.core.FileReader
import io.polywrap.core.wrap.WrapManifest
import io.polywrap.wasm.FileReaderFactory
import io.polywrap.wasm.WasmPackage
import io.polywrap.wasm.WasmWrapper
import readTestResource
import kotlin.test.*

class WasmPackageTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as"
    private val manifestPath = "$wrapperPath/wrap.info"
    private val modulePath = "$wrapperPath/wrap.wasm"

    private val baseFileReader = object : FileReader() {
        override fun readFile(filePath: String): Result<ByteArray> {
            return readTestResource("$wrapperPath/$filePath")
        }
    }

    @Test
    fun getters() {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        val pkg = WasmPackage(fileReader)

        val manifestResult = pkg.getManifest()
        assertTrue(manifestResult.isSuccess)
        assertEquals(manifestResult.getOrNull(), WrapManifest.deserialize(manifest).getOrNull())

        val moduleResult = pkg.getWasmModule()
        assertContentEquals(moduleResult.getOrNull(), wasmModule)

        val fileResult = pkg.getFile(FileReader.WRAP_MODULE_PATH)
        assertContentEquals(fileResult.getOrNull(), wasmModule)
    }

    @Test
    fun createWrapper() {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        val pkg = WasmPackage(fileReader)

        val wrapperResult = pkg.createWrapper()
        assertNotNull(wrapperResult)
        assertTrue(wrapperResult is WasmWrapper)
    }
}
