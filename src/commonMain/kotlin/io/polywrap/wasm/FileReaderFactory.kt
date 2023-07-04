package io.polywrap.wasm

import io.polywrap.core.FileReader

object FileReaderFactory {

    /**
     * Creates a [FileReader] that reads files from memory.
     * The [manifest] and [wasmModule] parameters are the content of the manifest and wasm module files respectively.
     * The [baseFileReader] parameter is an optional [FileReader] that will be used to read files that are not the
     * manifest or wasm module.
     * If [baseFileReader] is not specified, then an error will be returned when a file other than the manifest or
     * wasm module is read.
     * @param manifest The content of the manifest file.
     * @param wasmModule The content of the wasm module file.
     * @param baseFileReader An optional [FileReader] that will be used to read files that are not the manifest or
     *                       wasm module.
     * @return A [FileReader] that reads files from memory.
     */
    fun fromMemory(manifest: ByteArray, wasmModule: ByteArray, baseFileReader: FileReader? = null): FileReader {
        return object : FileReader() {
            override fun readFile(filePath: String): Result<ByteArray> {
                return when {
                    filePath == WRAP_MANIFEST_PATH -> Result.success(manifest)
                    filePath == WRAP_MODULE_PATH -> Result.success(wasmModule)
                    baseFileReader != null -> baseFileReader.readFile(filePath)
                    else -> Result.failure(Error("File not found at $filePath."))
                }
            }
        }
    }

    /**
     * Creates a [FileReader] that reads files from memory.
     * The [manifest] parameter is the content of the manifest file.
     * The [baseFileReader] parameter is an optional [FileReader] that will be used to read files that are not the
     * manifest.
     * If [baseFileReader] is not specified, then an error will be returned when a file other than the manifest is
     * read.
     * @param manifest The content of the manifest file.
     * @param baseFileReader An optional [FileReader] that will be used to read files that are not the manifest.
     * @return A [FileReader] that reads files from memory.
     */
    fun fromManifest(manifest: ByteArray, baseFileReader: FileReader): FileReader {
        return object : FileReader() {
            override fun readFile(filePath: String): Result<ByteArray> {
                return if (filePath == WRAP_MANIFEST_PATH) {
                    Result.success(manifest)
                } else {
                    baseFileReader.readFile(filePath)
                }
            }
        }
    }

    /**
     * Creates a [FileReader] that reads files from memory.
     * The [wasmModule] parameter is the content of the wasm module file.
     * The [baseFileReader] parameter is an optional [FileReader] that will be used to read files that are not the
     * wasm module.
     * If [baseFileReader] is not specified, then an error will be returned when a file other than the wasm module is
     * read.
     * @param wasmModule The content of the wasm module file.
     * @param baseFileReader An optional [FileReader] that will be used to read files that are not the wasm module.
     * @return A [FileReader] that reads files from memory.
     */
    fun fromWasmModule(wasmModule: ByteArray, baseFileReader: FileReader): FileReader {
        return object : FileReader() {
            override fun readFile(filePath: String): Result<ByteArray> {
                return if (filePath == WRAP_MODULE_PATH) {
                    Result.success(wasmModule)
                } else {
                    baseFileReader.readFile(filePath)
                }
            }
        }
    }
}
