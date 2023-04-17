package io.polywrap.core.types

import io.polywrap.core.wrap.WrapManifest

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
    fun getFile(path: String): Result<ByteArray>

    /**
     * Produce an instance of the wrap manifest
     *
     * @return A Result containing the wrap manifest or an error
     */
    fun getManifest(): Result<WrapManifest>

    /**
     * Produce an instance of the wrapper
     *
     * @return A Result containing the wrapper or an error
     */
    fun createWrapper(): Result<Wrapper>
}
