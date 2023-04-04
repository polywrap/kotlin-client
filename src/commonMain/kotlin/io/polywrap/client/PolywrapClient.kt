package io.polywrap.client

import io.polywrap.core.resolution.*
import io.polywrap.core.resolution.algorithms.buildCleanUriHistory
import io.polywrap.core.types.*
import io.polywrap.core.util.getEnvFromUriHistory
import io.polywrap.core.wrap.WrapManifest
import io.polywrap.msgpack.EnvSerializer
import io.polywrap.msgpack.msgPackDecode
import io.polywrap.msgpack.msgPackEncode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.serializer
import io.polywrap.core.resolution.algorithms.getImplementations as getImplementationsFromUri

/**
 * A client for interacting with Polywrap packages, providing high-level operations
 * such as retrieving package files, invoking package methods, and resolving URIs.
 *
 * @property config The [ClientConfig] configuration for this client instance.
 */
class PolywrapClient(val config: ClientConfig) : Client {

    /**
     * Returns the interface implementations stored in the configuration.
     *
     * @return A map of interface URIs to a list of their respective implementation URIs.
     */
    override fun getInterfaces(): Map<Uri, List<Uri>>? {
        return config.interfaces
    }

    /**
     * Returns the environments stored in the configuration.
     *
     * @return A map of environment URIs to their respective [WrapperEnv] instances.
     */
    override fun getEnvs(): Map<Uri, WrapperEnv>? {
        return config.envs
    }

    /**
     * Returns the [UriResolver] stored in the configuration.
     *
     * @return The configured [UriResolver].
     */
    override fun getResolver(): UriResolver {
        return config.resolver
    }

    /**
     * Retrieves the [WrapperEnv] associated with the specified URI.
     *
     * @param uri The URI of the wrapper environment to retrieve.
     * @return The [WrapperEnv] associated with the given URI, or null if not found.
     */
    override fun getEnvByUri(uri: Uri): WrapperEnv? {
        config.envs?.forEach { env ->
            if (env.key == uri) {
                return env.value
            }
        }
        return null
    }

    /**
     * Retrieves the manifest of the package at the specified URI.
     *
     * @param uri The URI of the package to retrieve the manifest for.
     * @return A [Deferred] [Result] containing the [WrapManifest], or an error if the retrieval fails.
     */
    override suspend fun getManifest(uri: Uri): Deferred<Result<WrapManifest>> = coroutineScope {
        async {
            val load = loadPackage(uri).await()
            if (load.isFailure) {
                Result.failure<WrapManifest>(load.exceptionOrNull()!!)
            }
            val pkg = load.getOrThrow()
            val manifest = pkg.getManifest()
            if (manifest.isFailure) {
                val exception = manifest.exceptionOrNull()!!
                val error = WrapError(
                    reason = exception.message ?: "Failed to retrieve manifest",
                    code = WrapErrorCode.CLIENT_GET_FILE_ERROR,
                    uri = uri.uri
                )
                Result.failure<WrapManifest>(error)
            }
            Result.success(manifest.getOrThrow())
        }
    }

    /**
     * Retrieves the file at the specified path within the package at the specified URI.
     *
     * @param uri The URI of the package containing the file.
     * @param path The path of the file within the package.
     * @return A [Deferred] [Result] containing the file content as a [ByteArray], or an error if the retrieval fails.
     */
    override suspend fun getFile(
        uri: Uri,
        path: String
    ): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val load = loadPackage(uri).await()
            if (load.isFailure) {
                Result.failure<ByteArray>(load.exceptionOrNull()!!)
            }
            val pkg = load.getOrThrow()

            val result = pkg.getFile(path).await()

            if (result.isFailure) {
                val exception = result.exceptionOrNull()!!
                val error = WrapError(
                    reason = exception.message ?: "Failed to retrieve file",
                    code = WrapErrorCode.CLIENT_GET_FILE_ERROR,
                    uri = uri.uri
                )
                Result.failure<ByteArray>(error)
            }

            result
        }
    }

    /**
     * Retrieves the list of implementation URIs for the specified interface URI.
     *
     * @param uri The URI of the interface for which implementations are being requested.
     * @param applyResolution If true, the client will attempt to resolve URIs using its [UriResolver].
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Deferred] [Result] containing the list of implementation URIs.
     */
    override suspend fun getImplementations(
        uri: Uri,
        applyResolution: Boolean,
        resolutionContext: UriResolutionContext?
    ): Deferred<Result<List<Uri>>> = coroutineScope {
        async {
            getImplementationsFromUri(
                uri,
                getInterfaces() ?: mapOf(),
                if (applyResolution) this@PolywrapClient else null,
                resolutionContext
            )
        }
    }

    /**
     * Invokes the specified [Wrapper] with the provided [InvokeOptions].
     *
     * @param wrapper The [Wrapper] to be invoked.
     * @param options The [InvokeOptions] specifying the URI, method, arguments, and other settings for the invocation.
     * @return A [Deferred] [Result] containing the invocation result as a [ByteArray], or an error if the invocation fails.
     */
    override suspend fun invokeWrapper(
        wrapper: Wrapper,
        options: InvokeOptions
    ): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val result = wrapper.invoke(options, this@PolywrapClient).await()
            if (result.isFailure) {
                Result.failure<ByteArray>(result.exceptionOrNull()!!)
            }
            result
        }
    }

    /**
     * Invokes the wrapper at the specified URI with the provided [InvokeOptions].
     *
     * @param options The [InvokeOptions] specifying the URI, method, arguments, and other settings for the invocation.
     * @return A [Deferred] [Result] containing the invocation result as a [ByteArray], or an error if the invocation fails.
     */
    override suspend fun invoke(options: InvokeOptions): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val resolutionContext = options.resolutionContext ?: BasicUriResolutionContext()
            val loadWrapperResult = loadWrapper(options.uri, resolutionContext).await()

            if (loadWrapperResult.isFailure) {
                Result.failure<ByteArray>(loadWrapperResult.exceptionOrNull()!!)
            }
            val wrapper = loadWrapperResult.getOrThrow()

            val resolutionPath = resolutionContext.getResolutionPath()

            val env = getEnvFromUriHistory(
                resolutionPath.ifEmpty { listOf(options.uri) },
                this@PolywrapClient
            )

            invokeWrapper(wrapper, options.copy(env = env)).await()
        }
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method, arguments, and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args A map of arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Deferred] [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    suspend inline fun <reified R> invoke(
        uri: Uri,
        method: String,
        args: Map<String, Any>? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<InvokeResult<R>> = coroutineScope {
        async {
            val options = InvokeOptions(
                uri = uri,
                method = method,
                args = args?.let { msgPackEncode(EnvSerializer, it) },
                env = env?.let { msgPackEncode(EnvSerializer, it) },
                resolutionContext = resolutionContext
            )
            val result = invoke(options).await()
            if (result.isFailure) {
                Result.failure<R>(result.exceptionOrNull()!!)
            }
            msgPackDecode(serializer<R>(), result.getOrThrow())
        }
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method and arguments of type [T], and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args An instance of type [T] representing the arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Deferred] [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    suspend inline fun <reified T, reified R> invoke(
        uri: Uri,
        method: String,
        args: T? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<InvokeResult<R>> = coroutineScope {
        async {
            val options = InvokeOptions(
                uri = uri,
                method = method,
                args = args?.let { msgPackEncode(serializer<T>(), it) },
                env = env?.let { msgPackEncode(EnvSerializer, it) },
                resolutionContext = resolutionContext
            )
            val result = invoke(options).await()
            if (result.isFailure) {
                Result.failure<R>(result.exceptionOrNull()!!)
            }
            msgPackDecode(serializer<R>(), result.getOrThrow())
        }
    }

    /**
     * Attempts to resolve the specified URI using the client's [UriResolver].
     *
     * @param uri The URI to be resolved.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @param resolveToPackage If true, the client will attempt to resolve the URI to a package rather than a wrapper.
     * @return A [Deferred] [Result] containing the resolved [UriPackageOrWrapper], or an error if the resolution fails.
     */
    override suspend fun tryResolveUri(
        uri: Uri,
        resolutionContext: UriResolutionContext?,
        resolveToPackage: Boolean
    ): Deferred<Result<UriPackageOrWrapper>> = coroutineScope {
        async {
            val uriResolver = getResolver()
            val context = resolutionContext ?: BasicUriResolutionContext()
            uriResolver.tryResolveUri(uri, this@PolywrapClient, context, true)
        }
    }

    /**
     * Validates the package at the specified URI.
     *
     * @param uri The URI of the package to validate.
     * @param abi If true, ABI validation will be performed.
     * @param recursive If true, validation will be performed recursively on all dependencies.
     * @return A [Deferred] [Result] containing a boolean indicating the validation result, or an error if validation fails.
     */
    override suspend fun validate(
        uri: Uri,
        abi: Boolean,
        recursive: Boolean
    ): Deferred<Result<Boolean>> {
        throw NotImplementedError("validate() is not yet implemented.")
    }

    private suspend fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<Result<Wrapper>> = coroutineScope {
        async {
            val context = resolutionContext ?: BasicUriResolutionContext()

            val result = tryResolveUri(uri, context).await()

            if (result.isFailure) {
                val history = buildCleanUriHistory(context.getHistory())
                val error = WrapError(
                    reason = "A URI Resolver returned an error.",
                    code = WrapErrorCode.URI_RESOLVER_ERROR,
                    uri = uri.uri,
                    resolutionStack = history,
                    cause = result.exceptionOrNull()
                )
                Result.failure<Wrapper>(error)
            }

            when (val uriPackageOrWrapper = result.getOrThrow()) {
                is UriPackageOrWrapper.WrapperValue -> Result.success(uriPackageOrWrapper.wrapper)
                is UriPackageOrWrapper.PackageValue -> {
                    val createWrapperResult = uriPackageOrWrapper.pkg.createWrapper()

                    if (createWrapperResult.isFailure) {
                        val exception = createWrapperResult.exceptionOrNull()!!
                        val error = WrapError(
                            reason = exception.message ?: "Unknown error occurred when loading wrapper",
                            code = WrapErrorCode.CLIENT_LOAD_WRAPPER_ERROR,
                            uri = uri.uri,
                            cause = exception
                        )
                        Result.failure<Wrapper>(error)
                    }

                    createWrapperResult
                }
                else -> {
                    val message = "Unable to find URI ${uriPackageOrWrapper.uri.uri}."
                    val history = buildCleanUriHistory(context.getHistory())
                    val error = WrapError(
                        reason = message,
                        code = WrapErrorCode.URI_NOT_FOUND,
                        uri = uri.uri,
                        resolutionStack = history
                    )
                    Result.failure(error)
                }
            }
        }
    }

    private suspend fun loadPackage(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<Result<WrapPackage>> = coroutineScope {
        async {
            val context = resolutionContext ?: BasicUriResolutionContext()

            val result = tryResolveUri(uri, context, true).await()

            if (result.isFailure) {
                val history = buildCleanUriHistory(context.getHistory())
                val error = WrapError(
                    reason = "A URI Resolver returned an error.",
                    code = WrapErrorCode.URI_RESOLVER_ERROR,
                    uri = uri.uri,
                    resolutionStack = history,
                    cause = result.exceptionOrNull()
                )
                Result.failure<WrapPackage>(error)
            }

            when (val uriPackageOrWrapper = result.getOrThrow()) {
                is UriPackageOrWrapper.PackageValue -> Result.success(uriPackageOrWrapper.pkg)
                else -> {
                    val message = "Unable to find URI ${uriPackageOrWrapper.uri.uri}."
                    val history = buildCleanUriHistory(context.getHistory())
                    val error = WrapError(
                        reason = message,
                        code = WrapErrorCode.URI_NOT_FOUND,
                        uri = uri.uri,
                        resolutionStack = history
                    )
                    Result.failure(error)
                }
            }
        }
    }
}
