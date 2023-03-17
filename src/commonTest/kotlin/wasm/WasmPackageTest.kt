package wasm

import eth.krisbitney.polywrap.core.wrap.WrapManifest
import eth.krisbitney.polywrap.wasm.FileReader
import eth.krisbitney.polywrap.wasm.FileReader.Companion.WRAP_MODULE_PATH
import eth.krisbitney.polywrap.wasm.WasmPackage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import readTestResource
import kotlin.test.*

class WasmPackageTest {

    private val wrapperPath = "wrappers/numbers-type/implementations/as"
    private val manifestPath = "$wrapperPath/wrap.info"
    private val modulePath = "$wrapperPath/wrap.wasm"

    private val baseFileReader = object : FileReader() {
        override suspend fun readFile(filePath: String): Result<ByteArray> {
            return readTestResource("$wrapperPath/$filePath")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getters() = runTest {
        val manifest: ByteArray =  readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReader.fromMemory(
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

        val fileResult = pkg.getFile(WRAP_MODULE_PATH)
        assertContentEquals(fileResult.getOrNull(), wasmModule)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createWrapper() = runTest {
        val manifest: ByteArray =  readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReader.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        val pkg = WasmPackage(fileReader)

        val wrapperResult = pkg.createWrapper()
        assertTrue(wrapperResult.isSuccess)
    }
}