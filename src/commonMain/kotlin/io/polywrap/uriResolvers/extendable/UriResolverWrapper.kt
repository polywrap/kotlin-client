package io.polywrap.uriResolvers.extendable

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.types.*
import io.polywrap.core.util.getEnvFromUriHistory
import io.polywrap.msgpack.msgPackDecode
import io.polywrap.msgpack.msgPackEncode
import io.polywrap.uriResolvers.ResolverWithHistory
import io.polywrap.wasm.WasmPackage
import kotlinx.serialization.serializer

/**
 * A wrapper class for URI Resolver Extension implementations. Inherits from [ResolverWithHistory].
 *
 * @property implementationUri The URI of the implementation to be used for resolving.
 */
class UriResolverWrapper(private val implementationUri: Uri) : ResolverWithHistory() {

    /**
     * Returns the step description for this URI Resolver Extension.
     * @param uri The URI being resolved.
     * @param result The result of the URI resolution as a [Result] of [UriPackageOrWrapper].
     * @return A string representing the step description.
     */
    override fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String =
        "ResolverExtension (${implementationUri.uri})"

    /**
     * Tries to resolve the given URI using the implementation specified by [implementationUri].
     * @param uri The URI being resolved.
     * @param client The [Client] instance for the current request.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @param resolveToPackage A flag indicating whether the URI should be resolved to a package or not.
     * @return A [Result] containing a [UriPackageOrWrapper] instance.
     */
    override fun _tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        val result = tryResolveUriWithImplementation(uri, implementationUri, client, resolutionContext)

        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }

        val uriOrManifest = result.getOrThrow()

        if (uriOrManifest?.uri != null) {
            val resultUri = Uri(uriOrManifest.uri)
            return Result.success(UriPackageOrWrapper.UriValue(resultUri))
        } else if (uriOrManifest?.manifest != null) {
            val wrapPackage = WasmPackage(
                uriOrManifest.manifest,
                UriResolverExtensionFileReader(implementationUri, uri, client)
            )

            return Result.success(UriPackageOrWrapper.PackageValue(uri, wrapPackage))
        }

        return Result.success(UriPackageOrWrapper.UriValue(uri))
    }

    private fun tryResolveUriWithImplementation(
        uri: Uri,
        implementationUri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<MaybeUriOrManifest?> {
        val subContext = resolutionContext.createSubContext()
        val loadExtensionResult = loadResolverExtension(uri, implementationUri, client, subContext)

        if (loadExtensionResult.isFailure) {
            return Result.failure(loadExtensionResult.exceptionOrNull()!!)
        }

        val extensionWrapper = loadExtensionResult.getOrThrow()

        val env = getEnvFromUriHistory(subContext.getResolutionPath(), client)

        val result = client.invokeWrapper(
            wrapper = extensionWrapper,
            options = InvokeOptions(
                uri = implementationUri,
                method = "tryResolveUri",
                args = msgPackEncode(
                    serializer(),
                    mapOf(
                        "authority" to uri.authority,
                        "path" to uri.path
                    )
                ),
                env = env
            )
        )
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        return msgPackDecode(serializer(), result.getOrThrow())
    }

    private fun loadResolverExtension(
        currentUri: Uri,
        resolverExtensionUri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<Wrapper> {
        val result = client.tryResolveUri(resolverExtensionUri, resolutionContext)

        return when (val uriPackageOrWrapper = result.getOrNull()) {
            is UriPackageOrWrapper.UriValue -> {
                Result.failure(
                    IllegalStateException(
                        "While resolving ${currentUri.uri} with URI resolver extension ${resolverExtensionUri.uri}, the extension could not be fully resolved. Last tried URI is ${uriPackageOrWrapper.uri}"
                    )
                )
            }
            is UriPackageOrWrapper.PackageValue -> uriPackageOrWrapper.pkg.createWrapper()
            is UriPackageOrWrapper.WrapperValue -> Result.success(uriPackageOrWrapper.wrapper)
            else -> Result.failure(result.exceptionOrNull()!!)
        }
    }
}
