package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.wrap.WrapManifest

/**
 * The Wrapper definition, which can be used to spawn
 * many invocations of this particular Wrapper. Internally
 * this class may do things like caching WASM bytecode, spawning
 * worker threads, or indexing into resolvers to find the requested method.
 */
interface Wrapper : Invocable {
    /**
     * Invoke the Wrapper based on the provided [InvokeOptions].
     *
     * @param options Options for this invocation.
     * @param invoker The client instance requesting this invocation. This client will be used for any sub-invocations that occur.
     * @return The result of the invocation.
     */
    override suspend fun invoke(options: InvokeOptions, invoker: Invoker): InvokeResult<ByteArray>

    /**
     * Get a file from the Wrapper package.
     *
     * @param path The path to the file.
     * @return The result of the file retrieval.
     */
    suspend fun getFile(path: String): Result<ByteArray>

    /**
     * Get a manifest from the Wrapper package.
     *
     * @return The [WrapManifest] for this Wrapper.
     */
    fun getManifest(): WrapManifest
}