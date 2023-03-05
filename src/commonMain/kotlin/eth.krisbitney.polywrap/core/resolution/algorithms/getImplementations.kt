package eth.krisbitney.polywrap.core.resolution.algorithms

import eth.krisbitney.polywrap.core.types.InterfaceImplementations
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.resolution.UriResolutionHandler
import eth.krisbitney.polywrap.core.types.WrapError
import eth.krisbitney.polywrap.core.types.WrapErrorCode
import eth.krisbitney.polywrap.core.types.WrapErrorOptions

/**
 * Retrieves all implementations of a given wrapper interface from an array of interface implementations.
 *
 * @param wrapperInterfaceUri The URI of the wrapper interface to find implementations of.
 * @param interfaces An array of interface implementations to search for implementations of the wrapper interface in.
 * @param client (Optional) A [UriResolutionHandler] instance used to resolve URIs.
 * @param resolutionContext (Optional) The context used for URI resolution.
 * @return A [Result] that contains the list of implementation URIs if successful, or a [WrapError] if there was an error.
 */
suspend fun getImplementations(
    wrapperInterfaceUri: Uri,
    interfaces: List<InterfaceImplementations>,
    client: UriResolutionHandler? = null,
    resolutionContext: UriResolutionContext? = null
): Result<List<Uri>> {
    val result = mutableListOf<Uri>()

    var finalUri = wrapperInterfaceUri

    // resolve redirects for wrapper interface URI
    if (client != null) {
        val redirectsResult = applyResolution(wrapperInterfaceUri, client, resolutionContext)
        if (redirectsResult.isFailure) {
            val error = WrapError("Failed to resolve redirects",
                redirectsResult.exceptionOrNull(),
                WrapErrorOptions(
                    uri = wrapperInterfaceUri.uri,
                    code = WrapErrorCode.CLIENT_GET_IMPLEMENTATIONS_ERROR,
                )
            )
            return Result.failure(error)
        }
        finalUri = redirectsResult.getOrThrow()
    }

    val addAllImp = addAllImplementationsFromImplementationsArray(result, interfaces, finalUri, client, resolutionContext)

    return if (addAllImp.isSuccess) Result.success(result) else Result.failure(addAllImp.exceptionOrNull()!!)
}

/*
 * Adds all implementation URIs from an array of interface implementations.
 *
 * @param implementationsArray An array of interface implementations to search for implementations of the wrapper interface in.
 * @param wrapperInterfaceUri The URI of the wrapper interface to find implementations of.
 * @return A [Result] that contains `null` if successful, or a [WrapError] if there was an error.
 */
private suspend fun addAllImplementationsFromImplementationsArray(
    result: MutableList<Uri>,
    implementationsArray: List<InterfaceImplementations>,
    wrapperInterfaceUri: Uri,
    uriResolutionHandler: UriResolutionHandler? = null,
    resolutionContext: UriResolutionContext? = null,
): Result<Unit> {
    for (interfaceImplementations in implementationsArray) {
        // resolve redirects for the current interface URI
        val fullyResolvedUri: Uri = if (uriResolutionHandler != null) {
            val redirectsResult = applyResolution(interfaceImplementations.interfaceUri, uriResolutionHandler, resolutionContext)
            if (redirectsResult.isFailure) {
                val error = WrapError("Failed to resolve redirects",
                    redirectsResult.exceptionOrNull(),
                    WrapErrorOptions(
                        uri = interfaceImplementations.interfaceUri.uri,
                        code = WrapErrorCode.CLIENT_GET_IMPLEMENTATIONS_ERROR,
                    )
                )
                return Result.failure(error)
            }
            redirectsResult.getOrThrow()
        } else {
            interfaceImplementations.interfaceUri
        }

        // if the interface URIs match, add the implementations
        if (Uri.equals(fullyResolvedUri, wrapperInterfaceUri)) {
            for (implementation in interfaceImplementations.implementations) {
                // add implementation only if not already in the list
                if (!result.contains(implementation)) {
                    result.add(implementation)
                }
            }
        }
    }
    return Result.success(Unit)
}