package algorithms

import resolution.Uri
import resolution.UriResolutionContext
import resolution.UriResolutionHandler

suspend fun applyResolution(uri: Uri, uriResolutionHandler: UriResolutionHandler, resolutionContext: UriResolutionContext? = null): Result<Uri> {
    val result = uriResolutionHandler.tryResolveUri(uri, resolutionContext)

    if (result.isFailure) {
        return Result.failure(result.exceptionOrNull()!!)
    }

    return Result.success(result.getOrThrow().uri)
}
