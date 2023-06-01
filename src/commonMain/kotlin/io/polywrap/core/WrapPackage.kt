package io.polywrap.core

import io.polywrap.core.wrap.WrapManifest
import uniffi.main.FfiWrapPackage

/**
 * A wrap package, capable of producing instances of a wrapper and its manifest
 */
interface WrapPackage : FfiWrapPackage {
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
}
