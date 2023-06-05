package io.polywrap.core

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import uniffi.main.FfiException

/**
 * Client invokes wrappers and interacts with wrap packages.
 */
interface Client : Invoker {
    /**
     * Resolves the [Wrapper] at the specified URI.
     *
     * @param uri The URI of the wrapper to resolve.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Result] containing the [Wrapper], or an error if the resolution fails.
     *
     * @throws FfiException
     *
     * @note The returned Wrapper is owned by the caller and must be manually deallocated
     */
    fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Result<Wrapper>

    /**
     * Returns an env (a set of environmental variables) from the configuration
     * used to instantiate the client.
     * @param uri the URI used to register the env
     * @return an env, or undefined if an env is not found at the given URI
     */
    fun getEnvByUri(uri: String): WrapperEnv?
}
