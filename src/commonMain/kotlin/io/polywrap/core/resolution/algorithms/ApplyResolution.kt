package io.polywrap.core.resolution.algorithms

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolutionHandler
fun applyResolution(uri: Uri, uriResolutionHandler: UriResolutionHandler, resolutionContext: UriResolutionContext? = null): Result<Uri> {
    val result = uriResolutionHandler.tryResolveUri(uri, resolutionContext)

    if (result.isFailure) {
        return Result.failure(result.exceptionOrNull()!!)
    }

    val resolvedUri = result.getOrThrow().uri

    return Result.success(resolvedUri)
}
