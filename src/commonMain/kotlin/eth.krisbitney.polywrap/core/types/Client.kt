package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.wrap.WrapManifest
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.resolution.UriResolutionHandler
import kotlinx.coroutines.Deferred

/** A map of string-indexed, Msgpack-serializable environmental variables associated with a wrapper */
typealias WrapperEnv = Map<String, Any>

/**
 * Core Client configuration that can be passed to the PolywrapClient or PolywrapCoreClient constructors.
 * @property resolver configure URI resolution for redirects, packages, and wrappers
 * @property interfaces set environmental variables for a wrapper
 * @property envs register interface implementations
 */
data class ClientConfig(
    val resolver: UriResolver,
    val interfaces: Map<Uri, List<Uri>>? = null,
    val envs: Map<Uri, WrapperEnv>? = null,
)

/**
 * CoreClient invokes wrappers and interacts with wrap packages.
 */
interface Client : Invoker, UriResolutionHandler {

    /**
     * Returns all interfaces from the configuration used to instantiate the client.
     * @return an array of interfaces and their registered implementations
     */
    fun getInterfaces(): Map<Uri, List<Uri>>?

    /**
     * Returns all env registrations from the configuration used to instantiate the client.
     * @return an array of env objects containing wrapper environmental variables
     */
    fun getEnvs(): Map<Uri, WrapperEnv>?

    /**
     * Returns an env (a set of environmental variables) from the configuration
     * used to instantiate the client.
     * @param uri the URI used to register the env
     * @return an env, or undefined if an env is not found at the given URI
     */
    fun getEnvByUri(uri: Uri): WrapperEnv?

    /**
     * Returns the URI resolver from the configuration used to instantiate the client.
     * @return an object that implements the IUriResolver interface
     */
    fun getResolver(): UriResolver

    /**
     * Returns a package's wrap manifest.
     * @param uri a wrap URI
     * @return a Result containing the eth.krisbitney.polywrap.core.WrapManifest if the request was successful
     */
    suspend fun getManifest(uri: Uri): Deferred<Result<WrapManifest>>

    /**
     * Returns a file contained in a wrap package.
     * @param uri a wrap URI
     * @param path file path from wrapper root
     * @return a Result containing a file if the request was successful
     */
    suspend fun getFile(
        uri: Uri,
        path: String,
    ): Deferred<Result<ByteArray>>

    /**
     * Validate a wrapper, given a URI.
     *
     * @param uri the Uri to resolve
     * @param abi validate the full ABI
     * @param recursive recursively validate imports
     * @return a Result containing a boolean or Error
     */
    suspend fun validate(
        uri: Uri,
        abi: Boolean = false,
        recursive: Boolean = false
    ): Deferred<Result<Boolean>>
}