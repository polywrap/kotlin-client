package io.polywrap.uriResolvers.cache

import io.polywrap.core.Uri
import io.polywrap.core.types.Wrapper

/**
 * A cache for storing [Wrapper] instances.
 */
interface WrapperCache {
    /**
     * Gets the [Wrapper] instance for the given [Uri].
     * @param uri The [Uri] to get the [Wrapper] for.
     * @return The [Wrapper] instance for the given [Uri], or null if it does not exist.
     */
    fun get(uri: Uri): Wrapper?

    /**
     * Sets the [Wrapper] instance for the given [Uri].
     * @param uri The [Uri] to set the [Wrapper] for.
     * @param wrapper The [Wrapper] instance to set.
     */
    fun set(uri: Uri, wrapper: Wrapper)
}
