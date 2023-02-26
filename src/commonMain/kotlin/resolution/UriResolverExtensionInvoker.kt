package resolution

import types.InvokeOptions
import types.Invoker

/**
 * Data class representing a Uri, a manifest, or neither.
 * @property uri the Wrap Uri associated with the resource, or null if the resource is not a Uri.
 * @property manifest the serialized Wrap manifest associated with the resource, or null if there is no manifest.
 */
data class MaybeUriOrManifest(
    val uri: String?,
    val manifest: ByteArray?
)

object UriResolverExtensionInvoker {
    /**
     * Use an invoker to try to resolve a URI using a wrapper that implements the UriResolver interface
     *
     * @param invoker - invokes the wrapper with the resolution URI as an argument
     * @param wrapper - URI for wrapper that implements the UriResolver interface
     * @param uri - the URI to resolve
     */
    suspend fun tryResolveUri(
        invoker: Invoker,
        wrapper: Uri,
        uri: Uri
    ): Result<MaybeUriOrManifest> {
        return invoker.invoke<MaybeUriOrManifest>(
            InvokeOptions(
                uri = wrapper,
                method = "tryResolveUri",
                args = mapOf(
                    "authority" to uri.authority,
                    "path" to uri.path
                )
            )
        )
    }
    // $start: UriResolverInterface-getFile
    /**
     * Use an invoker to fetch a file using a wrapper that implements the UriResolver interface
     *
     * @param invoker - invokes the wrapper with the filepath as an argument
     * @param wrapper - URI for wrapper that implements the UriResolver interface
     * @param path - a filepath, the format of which depends on the UriResolver
     */
    suspend fun getFile(
        invoker: Invoker,
        wrapper: Uri,
        path: String
    ): Result<ByteArray?> {
        return invoker.invoke<ByteArray?>(
            InvokeOptions(
                uri = wrapper,
                method = "getFile",
                args = mapOf(
                    "path" to path
                )
            )
        )
    }
}
