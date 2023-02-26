package types

import WrapManifest

/**
 * The Wrapper definition, which can be used to spawn
 * many invocations of this particular Wrapper. Internally
 * this class may do things like caching WASM bytecode, spawning
 * worker threads, or indexing into resolvers to find the requested method.
 */
interface Wrapper : Invocable {
    /**
     * Invoke the Wrapper based on the provided [options].
     *
     * @param options Options for this invocation.
     * @param invoker The client instance requesting this invocation.
     * This client will be used for any sub-invokes that occur.
     * @return A Promise with the result of the invocation.
     */
    override suspend fun <TData>invoke(options: InvokeOptions, invoker: Invoker): InvocableResult<TData>

    /**
     * Get a file from the Wrapper package.
     *
     * @param options Configuration options for file retrieval.
     * @return A Promise with the result of the file retrieval.
     */
    suspend fun getFile(
        path: String,
        encoding: String = "utf-8"
    ): Result<ByteArray>

    /**
     * Get a manifest from the Wrapper package.
     *
     * @return The WrapManifest for this Wrapper.
     */
    fun getManifest(): WrapManifest
}