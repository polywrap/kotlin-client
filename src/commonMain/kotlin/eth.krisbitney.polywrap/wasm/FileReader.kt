package eth.krisbitney.polywrap.wasm

abstract class FileReader {
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
