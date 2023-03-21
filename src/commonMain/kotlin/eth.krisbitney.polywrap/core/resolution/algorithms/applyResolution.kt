package eth.krisbitney.polywrap.core.resolution.algorithms

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.resolution.UriResolutionHandler
suspend fun applyResolution(uri: Uri, uriResolutionHandler: UriResolutionHandler, resolutionContext: UriResolutionContext? = null): Result<Uri> {
    val result = uriResolutionHandler.tryResolveUri(uri, resolutionContext).await()

    if (result.isFailure) {
        return Result.failure(result.exceptionOrNull()!!)
    }

    val resolvedUri = result.getOrThrow().uri

    return Result.success(resolvedUri)
}
