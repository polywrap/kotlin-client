package eth.krisbitney.polywrap.wasm

/**
 * An abstract class for reading files.
 */
abstract class FileReader {

    /**
     * Reads the file at the specified [filePath] and returns the content as a [Result] object.
     *
     * @param filePath The path to the file to be read.
     *
     * @return A [Result] object that contains the content of the file as a [ByteArray] if the file is read successfully.
     *         If there was an error while reading the file, the [Result] object will contain an appropriate error message.
     */
    abstract suspend fun readFile(filePath: String): Result<ByteArray>

    companion object {

        fun fromMemory(manifest: ByteArray, wasmModule: ByteArray, baseFileReader: FileReader? = null): FileReader {
            return object : FileReader() {
                override suspend fun readFile(filePath: String): Result<ByteArray> {
                    return when {
                        filePath == WRAP_MANIFEST_PATH -> Result.success(manifest)
                        filePath == WRAP_MODULE_PATH -> Result.success(wasmModule)
                        baseFileReader != null -> baseFileReader.readFile(filePath)
                        else -> Result.failure(Error("File not found at $filePath."))
                    }
                }
            }
        }

        fun fromManifest(manifest: ByteArray, baseFileReader: FileReader): FileReader {
            return object : FileReader() {
                override suspend fun readFile(filePath: String): Result<ByteArray> {
                    return if (filePath == WRAP_MANIFEST_PATH) {
                        Result.success(manifest)
                    } else {
                        baseFileReader.readFile(filePath)
                    }
                }
            }
        }

        fun fromWasmModule(wasmModule: ByteArray, baseFileReader: FileReader): FileReader {
            return object : FileReader() {
                override suspend fun readFile(filePath: String): Result<ByteArray> {
                    return if (filePath == WRAP_MODULE_PATH) {
                        Result.success(wasmModule)
                    } else {
                        baseFileReader.readFile(filePath)
                    }
                }
            }
        }
    }
}
