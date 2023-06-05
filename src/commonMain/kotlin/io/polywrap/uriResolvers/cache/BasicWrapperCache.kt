package io.polywrap.uriResolvers.cache

import io.polywrap.core.Wrapper

/**
 * A simple cache for storing [Wrapper] instances.
 */
class BasicWrapperCache : WrapperCache, AutoCloseable {

    private val cache: MutableMap<String, Wrapper> = mutableMapOf()

    override fun get(uri: String): Wrapper? = cache[uri]

    override fun set(uri: String, wrapper: Wrapper) {
        cache[uri] = wrapper
    }

    override fun close() {
        cache.values.forEach {
            if (it is AutoCloseable) {
                it.close()
            }
        }
    }
}
