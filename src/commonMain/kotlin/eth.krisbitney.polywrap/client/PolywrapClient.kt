package eth.krisbitney.polywrap.client

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.resolution.algorithms.buildCleanUriHistory
import eth.krisbitney.polywrap.core.resolution.algorithms.getImplementations as getImplementationsFromUri
import eth.krisbitney.polywrap.core.types.*
import eth.krisbitney.polywrap.core.util.getEnvFromUriHistory
import eth.krisbitney.polywrap.core.wrap.WrapManifest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PolywrapClient(val config: ClientConfig) : Client {

    override fun getInterfaces(): List<InterfaceImplementations>? {
        return config.interfaces
    }

    override fun getEnvs(): List<Env>? {
        return config.envs
    }

    override fun getResolver(): UriResolver {
        return config.resolver
    }

    override fun getEnvByUri(uri: Uri): Env? {
        config.envs?.forEach { env ->
            if (env.uri == uri) {
                return env
            }
        }
        return null
    }

    override suspend fun getManifest(uri: Uri): Deferred<Result<WrapManifest>> = coroutineScope {
        async {
            val load = loadWrapper(uri)
            if (load.isFailure) {
                Result.failure<WrapManifest>(load.exceptionOrNull()!!)
            }
            val wrapper = load.getOrThrow()
            val manifest = wrapper.getManifest()
            Result.success(manifest)
        }
    }

    override suspend fun getFile(
        uri: Uri,
        path: String,
    ): Deferred<Result<ByteArray>> = coroutineScope {
        async {
            val load = loadWrapper(uri)
            if (load.isFailure) {
                Result.failure<ByteArray>(load.exceptionOrNull()!!)
            }
            val wrapper = load.getOrThrow()

            val result = wrapper.getFile(path)

            if (result.isFailure) {
                val error = WrapError(
                    reason = result.error?.message,
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
        resolutionContext: UriResolutionContext?,
    ): Deferred<Result<List<Uri>>> = coroutineScope {
        async {
            getImplementationsFromUri(
                uri,
                getInterfaces() ?: listOf(),
                if (applyResolution) this@PolywrapClient else null,
                resolutionContext
            )
        }
    }

    override suspend fun invokeWrapper(
        wrapper: Wrapper,
        options: InvokeOptions,
    ): Deferred<InvokeResult<ByteArray>> = wrapper.invoke(options, this)

    override suspend fun invoke(options: InvokeOptions): Deferred<InvokeResult<ByteArray>> {
        val resolutionContext = options.resolutionContext ?: BasicUriResolutionContext()
        val loadWrapperResult = loadWrapper(options.uri, resolutionContext)

        if (loadWrapperResult.isFailure) {
            Result.failure<ByteArray>(loadWrapperResult.exceptionOrNull()!!)
        }
        val wrapper = loadWrapperResult.getOrThrow()

        val resolutionPath = resolutionContext.getResolutionPath()

        val env = getEnvFromUriHistory(
            resolutionPath.ifEmpty { listOf(options.uri) },
            this
        )

        return invokeWrapper(wrapper, options.copy(env = env.env))
    }

    override suspend fun tryResolveUri(
        uri: Uri,
        resolutionContext: UriResolutionContext?
    ): Deferred<Result<UriPackageOrWrapper>> = coroutineScope {
        async {
            val uriResolver = getResolver()
            val context = resolutionContext ?: BasicUriResolutionContext()
            uriResolver.tryResolveUri(uri, this@PolywrapClient, context)
        }
    }

    private suspend fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Result<Wrapper> {
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
            return Result.failure(error)
        }

        return when (val uriPackageOrWrapper = result.getOrThrow()) {
            is UriPackageOrWrapper.UriValue -> {
                val message = "Unable to find URI ${uriPackageOrWrapper.uri.uri}."
                val history = buildCleanUriHistory(context.getHistory())
                val error = WrapError(
                    reason = message,
                    code = WrapErrorCode.URI_NOT_FOUND,
                    uri = uri.uri,
                    resolutionStack =  history
                )
                Result.failure(error)
            }
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
                    return Result.failure(error)
                }

                createWrapperResult
            }
            is UriPackageOrWrapper.WrapperValue -> Result.success(uriPackageOrWrapper.wrapper)
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
