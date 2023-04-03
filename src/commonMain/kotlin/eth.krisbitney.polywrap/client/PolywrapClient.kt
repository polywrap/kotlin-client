package eth.krisbitney.polywrap.client

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.resolution.algorithms.buildCleanUriHistory
import eth.krisbitney.polywrap.core.types.*
import eth.krisbitney.polywrap.core.util.getEnvFromUriHistory
import eth.krisbitney.polywrap.core.wrap.WrapManifest
import eth.krisbitney.polywrap.msgpack.EnvSerializer
import eth.krisbitney.polywrap.msgpack.msgPackDecode
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.serializer
import eth.krisbitney.polywrap.core.resolution.algorithms.getImplementations as getImplementationsFromUri

class PolywrapClient(val config: ClientConfig) : Client {

    override fun getInterfaces(): Map<Uri, List<Uri>>? {
        return config.interfaces
    }

    override fun getEnvs(): Map<Uri, WrapperEnv>? {
        return config.envs
    }

    override fun getResolver(): UriResolver {
        return config.resolver
    }

    override fun getEnvByUri(uri: Uri): WrapperEnv? {
        config.envs?.forEach { env ->
            if (env.key == uri) {
                return env.value
            }
        }
        return null
    }

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

    suspend inline fun <reified T, reified R> invoke(
        uri: Uri,
        method: String,
        args: T? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<InvokeResult<R>> = coroutineScope {
        val options = InvokeOptions(
            uri = uri,
            method = method,
            args = args?.let { msgPackEncode(serializer<T>(), it) },
            env = env?.let { msgPackEncode(EnvSerializer, it) },
            resolutionContext = resolutionContext
        )
        async {
            val result = invoke(options).await()
            if (result.isFailure) {
                Result.failure<R>(result.exceptionOrNull()!!)
            }
            msgPackDecode(serializer<R>(), result.getOrThrow())
        }
    }

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

    override suspend fun validate(
        uri: Uri,
        abi: Boolean,
        recursive: Boolean
    ): Deferred<Result<Boolean>> {
        throw NotImplementedError("validate() is not yet implemented.")
    }
}
