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

class UriResolverWrapper(private val implementationUri: Uri) : ResolverWithHistory() {

    override suspend fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String =
        "ResolverExtension (${implementationUri.uri})"

    override suspend fun _tryResolveUri(
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

        if (uriOrManifest.uri != null) {
            val resultUri = Uri(uriOrManifest.uri)
            return Result.success(UriPackageOrWrapper.UriValue(resultUri))
        } else if (uriOrManifest.manifest != null) {
            val wrapPackage = WasmPackage(
                uriOrManifest.manifest,
                UriResolverExtensionFileReader(implementationUri, uri, client)
            )

            return Result.success(UriPackageOrWrapper.PackageValue(uri, wrapPackage))
        }

        return Result.success(UriPackageOrWrapper.UriValue(uri))
    }

    private suspend fun tryResolveUriWithImplementation(
        uri: Uri,
        implementationUri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<MaybeUriOrManifest> {
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
        ).await()
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        return msgPackDecode(serializer(), result.getOrThrow())
    }

    private suspend fun loadResolverExtension(
        currentUri: Uri,
        resolverExtensionUri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<Wrapper> {
        val result = client.tryResolveUri(resolverExtensionUri, resolutionContext).await()

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
