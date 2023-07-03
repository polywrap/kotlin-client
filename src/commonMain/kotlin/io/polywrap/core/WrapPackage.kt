package io.polywrap.core

import io.polywrap.core.wrap.WrapManifest
import io.polywrap.plugin.PluginWrapper
import uniffi.polywrap_native.IffiWrapPackage

/**
 * A wrap package, capable of producing instances of a wrapper and its manifest
 */
interface WrapPackage : IffiWrapPackage {
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
     * Produce an instance of the package's Wrapper
     *
     * @return A [Wrapper] instance
     */
    fun createWrapper(): Wrapper
}
