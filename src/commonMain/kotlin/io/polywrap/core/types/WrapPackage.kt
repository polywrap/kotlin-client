package io.polywrap.core.types

import io.polywrap.core.wrap.WrapManifest
import kotlinx.coroutines.Deferred

/**
 * A wrap package, capable of producing instances of a wrapper and its manifest
 */
interface WrapPackage {
    /**
     * Get a file from the Wrapper package.
     *
     * @param path The path to the file.
     * @return The result of the file retrieval.
     */
    suspend fun getFile(path: String): Deferred<Result<ByteArray>>

    /**
     * Produce an instance of the wrap manifest
     *
     * @return A Result containing the wrap manifest or an error
     */
    suspend fun getManifest(): Result<WrapManifest>

    /**
     * Produce an instance of the wrapper
     *
     * @return A Result containing the wrapper or an error
     */
    suspend fun createWrapper(): Result<Wrapper>
}
