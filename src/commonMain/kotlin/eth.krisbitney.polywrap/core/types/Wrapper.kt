package eth.krisbitney.polywrap.core.types

import kotlinx.coroutines.Deferred

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
     * @return A [Deferred] with [Result] containing a MsgPack encoded byte array or an error.
     */
    override suspend fun invoke(options: InvokeOptions, invoker: Invoker): Deferred<Result<ByteArray>>
}
