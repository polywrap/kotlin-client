package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.wrap.WrapManifest
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.resolution.UriResolutionHandler

/**
 * Core Client configuration that can be passed to the PolywrapClient or PolywrapCoreClient constructors.
 * @property resolver configure URI resolution for redirects, packages, and wrappers
 * @property interfaces set environmental variables for a wrapper
 * @property envs register interface implementations
 */
data class CoreClientConfig(
    val resolver: UriResolver,
    val interfaces: List<InterfaceImplementations>? = null,
    val envs: List<Env>? = null,
)

/**
 * CoreClient invokes wrappers and interacts with wrap packages.
 */
interface Client : Invoker, UriResolutionHandler {

    /**
     * Returns the configuration used to instantiate the client.
     * @return an immutable core client config
     */
    fun getConfig(): CoreClientConfig

    /**
     * Returns all interfaces from the configuration used to instantiate the client.
     * @return an array of interfaces and their registered implementations
     */
    fun getInterfaces(): List<InterfaceImplementations>?

    /**
     * Returns all env registrations from the configuration used to instantiate the client.
     * @return an array of env objects containing wrapper environmental variables
     */
    fun getEnvs(): List<Env>?

    /**
     * Returns an env (a set of environmental variables) from the configuration
     * used to instantiate the client.
     * @param uri the URI used to register the env
     * @return an env, or undefined if an env is not found at the given URI
     */
    fun getEnvByUri(uri: Uri): Env?

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
    suspend fun getManifest(uri: Uri): Result<WrapManifest>

    /**
     * Returns a file contained in a wrap package.
     * @param uri a wrap URI
     * @param path file path from wrapper root
     * @param encoding file encoding
     * @return a Result containing a file if the request was successful
     */
    suspend fun getFile(
        uri: Uri,
        path: String,
        encoding: String = "utf-8"
    ): Result<String>

    /**
     * Returns the interface implementations associated with an interface URI from the
     * configuration used to instantiate the client.
     *
     * @param uri - a wrap URI
     * @param applyResolution - If true, follow redirects to resolve URIs
     * @param resolutionContext - Use and update an existing resolution context
     * @return a Result containing an array of URIs if the request was successful
     */
    suspend fun getImplementations(
        uri: Uri,
        applyResolution: Boolean = false,
        resolutionContext: UriResolutionContext? = null,
    ): Result<List<Uri>>

    // typescript tsdoc comment
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
    ): Result<Boolean>
}