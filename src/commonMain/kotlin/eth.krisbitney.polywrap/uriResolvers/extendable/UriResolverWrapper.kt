package eth.krisbitney.polywrap.uriResolvers.extendable

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriPackageOrWrapper
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.types.Client
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.core.types.Wrapper
import eth.krisbitney.polywrap.core.util.getEnvFromUriHistory
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import eth.krisbitney.polywrap.uriResolvers.ResolverWithHistory
import eth.krisbitney.polywrap.wasm.WasmPackage

class UriResolverWrapper(private val implementationUri: Uri) : ResolverWithHistory() {

    override suspend fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String =
        "ResolverExtension (${implementationUri.uri})"

    override suspend fun _tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
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
        val result = loadResolverExtension(uri, implementationUri, client, subContext)

        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }

        val extensionWrapper = result.getOrThrow()

        val env = getEnvFromUriHistory(subContext.getResolutionPath(), client)

        return client.invokeWrapper<MaybeUriOrManifest>(
            wrapper = extensionWrapper,
            InvokeOptions(
                uri = implementationUri,
                method = "tryResolveUri",
                args = msgPackEncode(
                    mapOf(
                        "authority" to uri.authority,
                        "path" to uri.path
                    )
                ),
                env = if (env != null) msgPackEncode(env.env) else null
            )
        ).await()
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
            else ->  Result.failure(result.exceptionOrNull()!!)
        }
    }
}