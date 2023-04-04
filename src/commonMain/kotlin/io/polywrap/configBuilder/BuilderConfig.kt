package io.polywrap.configBuilder

import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper
import io.polywrap.core.types.WrapperEnv

/**
 * An intermediary representation of the Polywrap Client configuration.
 */
/**
 * Represents in intermediary representation of the Polywrap Client configuration,
 * used to facilitate config composition in the [ClientConfigBuilder].
 *
 * @property envs A [MutableMap] mapping environment URIs to their respective [WrapperEnv].
 * @property interfaces A [MutableMap] mapping interface URIs to their respective [MutableSet] of implementation URIs.
 * @property redirects A [MutableMap] mapping source URIs to their respective destination URIs.
 * @property wrappers A [MutableMap] mapping wrapper URIs to their respective [Wrapper] instances.
 * @property packages A [MutableMap] mapping package URIs to their respective [WrapPackage] instances.
 * @property resolvers A [MutableList] of [UriResolver] instances.
 */
data class BuilderConfig(
    val envs: MutableMap<String, WrapperEnv>,
    val interfaces: MutableMap<String, MutableSet<String>>,
    val redirects: MutableMap<String, String>,
    val wrappers: MutableMap<String, Wrapper>,
    val packages: MutableMap<String, WrapPackage>,
    val resolvers: MutableList<UriResolver>
)
