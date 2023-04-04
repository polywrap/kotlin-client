package io.polywrap.configBuilder

import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper
import io.polywrap.core.types.WrapperEnv

/**
 * An intermediary representation of the Polywrap Client configuration.
 */
data class BuilderConfig(
    val envs: MutableMap<String, WrapperEnv>,
    val interfaces: MutableMap<String, MutableSet<String>>,
    val redirects: MutableMap<String, String>,
    val wrappers: MutableMap<String, Wrapper>,
    val packages: MutableMap<String, WrapPackage>,
    val resolvers: MutableList<UriResolver>
)
