package io.polywrap.wasm

import io.polywrap.core.types.FileReader
import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper
import io.polywrap.core.wrap.WrapManifest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Implementation of the [WrapPackage] interface for Wasm Wrap packages
 *
 * @param fileReader A [FileReader] instance used to read the package files, including the Wasm module and WRAP manifest
 */
data class WasmPackage(private val fileReader: FileReader) : WrapPackage {

    /**
     * Creates a new [WasmPackage] instance with the given manifest buffer and wasm module buffer.
     *
     * @param manifestBuffer the manifest buffer
     * @param wasmModule the wasm module buffer
     * @param fileReader a file reader used to read other package files
     */
    constructor(manifestBuffer: ByteArray, wasmModule: ByteArray, fileReader: FileReader? = null) :
        this(FileReaderFactory.fromMemory(manifestBuffer, wasmModule, fileReader))

    /**
     * Creates a new [WasmPackage] instance with the given manifest buffer and file reader.
     *
     * @param manifestBuffer the manifest buffer
     * @param fileReader a file reader used to read other package files
     */
    constructor(manifestBuffer: ByteArray, fileReader: FileReader) : this(FileReaderFactory.fromManifest(manifestBuffer, fileReader))

    /**
     * Produce an instance of the WrapPackage's WRAP manifest
     *
     * @return A [WrapManifest] instance
     */
    override suspend fun getManifest(): Result<WrapManifest> {
        val result = fileReader.readFile(FileReader.WRAP_MANIFEST_PATH).await()
        if (result.isFailure) {
            return Result.failure(Error("Wrapper does not contain a WRAP manifest"))
        }
        return WrapManifest.deserialize(result.getOrThrow())
    }

    /**
     * Construct an instance of the package's Wasm Wrapper
     *
     * @return A [WasmWrapper] instance
     */
    override suspend fun createWrapper(): Result<Wrapper> {
        val module = getWasmModule()
        if (module.isFailure) {
            return Result.failure(module.exceptionOrNull()!!)
        }
        val wrapper = WasmWrapper(module.getOrThrow())
        return Result.success(wrapper)
    }

    /**
     * Retrieves the file at the specified path within the Wrap package.
     *
     * @param path The path to the file.
     * @return A [ByteArray] containing the file contents
     */
    override suspend fun getFile(path: String): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val dataResult = fileReader.readFile(path).await()
            if (dataResult.isFailure) {
                Result.failure<ByteArray>(Error("WasmWrapper: File was not found.\nSubpath: $path"))
            }
            dataResult
        }
    }

    /**
     * Reads the Wasm module from the Wrap package using the provided [FileReader], and returns the module bytes
     *
     * @return The Wasm module bytes
     */
    suspend fun getWasmModule(): Result<ByteArray> {
        val result = fileReader.readFile(FileReader.WRAP_MODULE_PATH).await()
        if (!result.isSuccess) {
            return Result.failure(Error("Wrapper does not contain a Wasm module"))
        }
        return result
    }
}
