package wasm

import io.polywrap.core.types.FileReader
import io.polywrap.core.wrap.WrapManifest
import io.polywrap.wasm.FileReaderFactory
import io.polywrap.wasm.WasmPackage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import readTestResource
import kotlin.test.*

class WasmPackageTest {

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
    fun getters() = runTest {
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

        val fileResult = pkg.getFile(FileReader.WRAP_MODULE_PATH).await()
        assertContentEquals(fileResult.getOrNull(), wasmModule)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createWrapper() = runTest {
        val manifest: ByteArray = readTestResource(manifestPath).getOrThrow()
        val wasmModule: ByteArray = readTestResource(modulePath).getOrThrow()
        val fileReader = FileReaderFactory.fromMemory(
            manifest = manifest,
            wasmModule = wasmModule,
            baseFileReader = baseFileReader
        )
        val pkg = WasmPackage(fileReader)

        val wrapperResult = pkg.createWrapper()
        assertTrue(wrapperResult.isSuccess)
    }
}
