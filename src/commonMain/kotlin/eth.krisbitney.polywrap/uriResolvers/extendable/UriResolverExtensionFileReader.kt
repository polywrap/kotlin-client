package eth.krisbitney.polywrap.uriResolvers.extendable

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.FileReader
import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.core.util.combinePaths

class UriResolverExtensionFileReader(
    private val resolverExtensionUri: Uri,
    private val wrapperUri: Uri,
    private val invoker: Invoker,
) : FileReader() {

    override suspend fun readFile(filePath: String): Result<ByteArray> {
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