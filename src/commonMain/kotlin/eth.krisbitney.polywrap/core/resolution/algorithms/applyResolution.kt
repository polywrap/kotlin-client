package eth.krisbitney.polywrap.core.resolution.algorithms

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriPackageOrWrapper
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.resolution.UriResolutionHandler

suspend fun applyResolution(uri: Uri, uriResolutionHandler: UriResolutionHandler, resolutionContext: UriResolutionContext? = null): Result<Uri> {
    val result = uriResolutionHandler.tryResolveUri(uri, resolutionContext)

    if (result.isFailure) {
        return Result.failure(result.exceptionOrNull()!!)
    }

    val resolvedUri = when (val uriPackageOrWrapper = result.getOrThrow()) {
        is UriPackageOrWrapper.UriValue -> uriPackageOrWrapper.uri
        is UriPackageOrWrapper.PackageValue -> uriPackageOrWrapper.pkg.uri
        is UriPackageOrWrapper.WrapperValue -> uriPackageOrWrapper.wrapper.uri
    }

    return Result.success(resolvedUri)
}
