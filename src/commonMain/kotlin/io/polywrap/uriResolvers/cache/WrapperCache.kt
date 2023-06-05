package io.polywrap.uriResolvers.cache

import io.polywrap.core.Wrapper

/**
 * A cache for storing [Wrapper] instances.
 */
interface WrapperCache {
    /**
     * Gets the [Wrapper] instance for the given URI.
     * @param uri The URI to get the [Wrapper] for.
     * @return The [Wrapper] instance, or null if it does not exist.
     */
    fun get(uri: String): Wrapper?

    /**
     * Sets the [Wrapper] instance for the given URI.
     * @param uri The URI to set the [Wrapper] for.
     * @param wrapper The [Wrapper] instance to set.
     */
    fun set(uri: String, wrapper: Wrapper)
}
