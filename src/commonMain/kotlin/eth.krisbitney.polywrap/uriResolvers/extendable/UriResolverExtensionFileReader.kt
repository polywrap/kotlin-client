package eth.krisbitney.polywrap.uriResolvers.extendable

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.FileReader
import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.core.util.combinePaths
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class UriResolverExtensionFileReader(
    private val resolverExtensionUri: Uri,
    private val wrapperUri: Uri,
    private val invoker: Invoker,
) : FileReader() {

    override suspend fun readFile(filePath: String): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val path = combinePaths(wrapperUri.path, filePath)
            val result = UriResolverExtensionInvoker.getFile(invoker, resolverExtensionUri, path).await()

            if (result.isFailure) {
                Result.failure<ByteArray>(result.exceptionOrNull()!!)
            }

            val fileContent = result.getOrNull()
            if (fileContent != null) {
                Result.success(fileContent)
            } else {
                Result.failure(Exception("File not found at $path"))
            }
        }
    }
}
