package io.polywrap.client

import io.polywrap.core.resolution.*
import io.polywrap.core.resolution.algorithms.buildCleanUriHistory
import io.polywrap.core.types.*
import io.polywrap.core.util.getEnvFromUriHistory
import io.polywrap.core.wrap.WrapManifest
import io.polywrap.msgpack.EnvSerializer
import io.polywrap.msgpack.MapArgsSerializer
import io.polywrap.msgpack.NullableKVSerializer
import io.polywrap.msgpack.msgPackDecode
import io.polywrap.msgpack.msgPackEncode
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
     * @return A [Result] containing the [WrapManifest], or an error if the retrieval fails.
     */
    override fun getManifest(uri: Uri): Result<WrapManifest> {
        val load = loadPackage(uri)
        if (load.isFailure) {
            return Result.failure(load.exceptionOrNull()!!)
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
            return Result.failure(error)
        }
        return Result.success(manifest.getOrThrow())
    }

    /**
     * Retrieves the file at the specified path within the package at the specified URI.
     *
     * @param uri The URI of the package containing the file.
     * @param path The path of the file within the package.
     * @return A [Result] containing the file content as a [ByteArray], or an error if the retrieval fails.
     */
    override fun getFile(
        uri: Uri,
        path: String
    ): Result<ByteArray> {
        val load = loadPackage(uri)
        if (load.isFailure) {
            return Result.failure(load.exceptionOrNull()!!)
        }
        val pkg = load.getOrThrow()

        val result = pkg.getFile(path)

        if (result.isFailure) {
            val exception = result.exceptionOrNull()!!
            val error = WrapError(
                reason = exception.message ?: "Failed to retrieve file",
                code = WrapErrorCode.CLIENT_GET_FILE_ERROR,
                uri = uri.uri
            )
            return Result.failure(error)
        }

        return result
    }

    /**
     * Retrieves the list of implementation URIs for the specified interface URI.
     *
     * @param uri The URI of the interface for which implementations are being requested.
     * @param applyResolution If true, the client will attempt to resolve URIs using its [UriResolver].
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Result] containing the list of implementation URIs.
     */
    override fun getImplementations(
        uri: Uri,
        applyResolution: Boolean,
        resolutionContext: UriResolutionContext?
    ): Result<List<Uri>> = getImplementationsFromUri(
        uri,
        getInterfaces() ?: mapOf(),
        if (applyResolution) this else null,
        resolutionContext
    )

    /**
     * Invokes the specified [Wrapper] with the provided [InvokeOptions].
     *
     * @param wrapper The [Wrapper] to be invoked.
     * @param options The [InvokeOptions] specifying the URI, method, arguments, and other settings for the invocation.
     * @return A [Result] containing the invocation result as a [ByteArray], or an error if the invocation fails.
     */
    override fun invokeWrapper(
        wrapper: Wrapper,
        options: InvokeOptions
    ): Result<ByteArray> = wrapper.invoke(options, this)

    /**
     * Invokes the wrapper at the specified URI with the provided [InvokeOptions].
     *
     * @param options The [InvokeOptions] specifying the URI, method, arguments, and other settings for the invocation.
     * @return A [Result] containing the invocation result as a [ByteArray], or an error if the invocation fails.
     */
    override fun invoke(options: InvokeOptions): Result<ByteArray> {
        val resolutionContext = options.resolutionContext ?: BasicUriResolutionContext()
        val loadWrapperResult = loadWrapper(options.uri, resolutionContext)

        if (loadWrapperResult.isFailure) {
            return Result.failure(loadWrapperResult.exceptionOrNull()!!)
        }
        val wrapper = loadWrapperResult.getOrThrow()

        val resolutionPath = resolutionContext.getResolutionPath()

        if (options.env == null) {
            val env = getEnvFromUriHistory(
                resolutionPath.ifEmpty { listOf(options.uri) },
                this@PolywrapClient
            )
            return invokeWrapper(wrapper, options.copy(env = env))
        }

        return invokeWrapper(wrapper, options)
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method, arguments, and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args A map of arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified R> invoke(
        uri: Uri,
        method: String,
        args: Map<String, Any?>? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
    ): InvokeResult<R> {
        val options = InvokeOptions(
            uri = uri,
            method = method,
            args = args?.let { msgPackEncode(MapArgsSerializer, it) },
            env = env?.let { msgPackEncode(EnvSerializer, it) },
            resolutionContext = resolutionContext
        )
        val result = invoke(options)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        // The only valid Map return type is Map<String, Any> and its nullable versions
        if (R::class == Map::class) {
            val decoded = msgPackDecode(NullableKVSerializer, result.getOrThrow())
            return if (decoded.isFailure) {
                Result.failure(decoded.exceptionOrNull()!!)
            } else {
                Result.success(decoded.getOrThrow() as R)
            }
        }
        return msgPackDecode(serializer<R>(), result.getOrThrow())
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method and arguments of type [T], and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args An instance of type [T] representing the arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified T, reified R> invoke(
        uri: Uri,
        method: String,
        args: T? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
    ): InvokeResult<R> {
        val options = InvokeOptions(
            uri = uri,
            method = method,
            args = args?.let { msgPackEncode(serializer<T>(), it) },
            env = env?.let { msgPackEncode(EnvSerializer, it) },
            resolutionContext = resolutionContext
        )
        val result = invoke(options)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        // The only valid Map return type is Map<String, Any> and its nullable versions
        if (R::class == Map::class) {
            val decoded = msgPackDecode(NullableKVSerializer, result.getOrThrow())
            return if (decoded.isFailure) {
                Result.failure(decoded.exceptionOrNull()!!)
            } else {
                Result.success(decoded.getOrThrow() as R)
            }
        }
        return msgPackDecode(serializer<R>(), result.getOrThrow())
    }

    /**
     * Attempts to resolve the specified URI using the client's [UriResolver].
     *
     * @param uri The URI to be resolved.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @param resolveToPackage If true, the client will attempt to resolve the URI to a package rather than a wrapper.
     * @return A [Result] containing the resolved [UriPackageOrWrapper], or an error if the resolution fails.
     */
    override fun tryResolveUri(
        uri: Uri,
        resolutionContext: UriResolutionContext?,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        val uriResolver = getResolver()
        val context = resolutionContext ?: BasicUriResolutionContext()
        return uriResolver.tryResolveUri(uri, this, context, resolveToPackage)
    }

    /**
     * Validates the package at the specified URI.
     *
     * @param uri The URI of the package to validate.
     * @param abi If true, ABI validation will be performed.
     * @param recursive If true, validation will be performed recursively on all dependencies.
     * @return A [Result] containing a boolean indicating the validation result, or an error if validation fails.
     */
    override fun validate(
        uri: Uri,
        abi: Boolean,
        recursive: Boolean
    ): Result<Boolean> {
        throw NotImplementedError("validate() is not yet implemented.")
    }

    private fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Result<Wrapper> {
        val context = resolutionContext ?: BasicUriResolutionContext()

        val result = tryResolveUri(uri, context, false)

        if (result.isFailure) {
            val history = buildCleanUriHistory(context.getHistory())
            val error = WrapError(
                reason = "A URI Resolver returned an error.",
                code = WrapErrorCode.URI_RESOLVER_ERROR,
                uri = uri.uri,
                resolutionStack = history,
                cause = result.exceptionOrNull()
            )
            return Result.failure(error)
        }

        return when (val uriPackageOrWrapper = result.getOrThrow()) {
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

    private fun loadPackage(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Result<WrapPackage> {
        val context = resolutionContext ?: BasicUriResolutionContext()

        val result = tryResolveUri(uri, context, true)

        if (result.isFailure) {
            val history = buildCleanUriHistory(context.getHistory())
            val error = WrapError(
                reason = "A URI Resolver returned an error.",
                code = WrapErrorCode.URI_RESOLVER_ERROR,
                uri = uri.uri,
                resolutionStack = history,
                cause = result.exceptionOrNull()
            )
            return Result.failure(error)
        }

        return when (val uriPackageOrWrapper = result.getOrThrow()) {
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
