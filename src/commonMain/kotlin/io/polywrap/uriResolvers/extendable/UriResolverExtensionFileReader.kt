package io.polywrap.uriResolvers.extendable

import io.polywrap.core.resolution.Uri
import io.polywrap.core.types.FileReader
import io.polywrap.core.types.Invoker
import io.polywrap.core.util.combinePaths

/**
 * A file reader class for reading files from a URI Resolver Extension, which inherits from [FileReader].
 *
 * @property resolverExtensionUri The URI of the resolver extension.
 * @property wrapperUri The URI of the wrapper containing the file.
 * @property invoker The [Invoker] instance used for invoking the extension.
 */
class UriResolverExtensionFileReader(
    private val resolverExtensionUri: Uri,
    private val wrapperUri: Uri,
    private val invoker: Invoker
) : FileReader() {

    /**
     * Reads the file at the given file path within the wrapper specified by [wrapperUri].
     * @param filePath The relative file path within the wrapper.
     * @return A [Result] containing the file content as a [ByteArray] or an exception if the file is not found or cannot be read.
     */
    override fun readFile(filePath: String): Result<ByteArray> {
        val path = combinePaths(wrapperUri.path, filePath)
        val result = UriResolverExtensionInvoker.getFile(invoker, resolverExtensionUri, path)

        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }

        val fileContent = result.getOrNull()
        return if (fileContent != null) {
            Result.success(fileContent)
        } else {
            Result.failure(Exception("File not found at $path"))
        }
    }
}
