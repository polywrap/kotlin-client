package io.polywrap.core

/**
 * An abstract class for reading files.
 */
abstract class FileReader {

    /**
     * Reads the file at the specified [filePath] and returns the content as a [Result] object.
     * @param filePath The path to the file to be read.
     * @return A [Result] object that contains the content of the file as a [ByteArray] if the file is read successfully.
     */
    abstract fun readFile(filePath: String): Result<ByteArray>

    companion object {
        const val WRAP_MANIFEST_PATH = "wrap.info"
        const val WRAP_MODULE_PATH = "wrap.wasm"
    }
}
