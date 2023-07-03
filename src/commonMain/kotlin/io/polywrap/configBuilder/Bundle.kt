package io.polywrap.configBuilder

import io.polywrap.core.WrapEnv
import io.polywrap.core.WrapPackage
import io.polywrap.core.resolution.Uri

/**
 * A [Bundle] is a collection of [Item]s that are to be added to the Polywrap Client configuration.
 *
 * @property items A [Map] of [Item]s to add to the configuration.
 * @see [IConfigBuilder.addBundle]
 */
abstract class Bundle {
    abstract val items: Map<String, Item>

    /**
     * A [Bundle.Item] is a [WrapPackage] and additional configuration to be added to the Polywrap Client Configuration.
     * This may include interface implementations, URI redirects, and/or a [WrapEnv].
     *
     * If a [WrapPackage] is not provided, the Polywrap Client will attempt to resolve the [Uri].
     *
     * Note that users can override values when configuring the Polywrap Client.
     *
     * @param uri The primary [Uri] that will resolve to [pkg].
     * @param pkg The [WrapPackage].
     * @param implements A list of interfaces that [pkg] implements.
     * @param redirectFrom A list of [Uri]s that will redirect to [uri].
     * @param env A default [WrapEnv] for [pkg].
     */
    class Item(
        val uri: Uri,
        val pkg: WrapPackage? = null,
        val implements: List<Uri>? = null,
        val redirectFrom: List<Uri>? = null,
        val env: WrapEnv? = null
    )
}
