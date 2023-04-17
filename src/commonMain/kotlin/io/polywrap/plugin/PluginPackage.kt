package io.polywrap.plugin

import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper
import io.polywrap.core.wrap.WrapManifest

/**
 * Implementation of the [WrapPackage] interface for Plugin Wrap packages.
 *
 * @param TConfig The type of the plugin configuration
 * @property pluginModule The [PluginModule] instance used to create the plugin wrapper
 * @property manifest The [WrapManifest] instance for the plugin
 */
data class PluginPackage<TConfig>(
    private val pluginModule: PluginModule<TConfig>,
    private val manifest: WrapManifest
) : WrapPackage {

    /**
     * Produce an instance of the WrapPackage's WRAP manifest
     *
     * @return A [WrapManifest] instance
     */
    override fun getManifest(): Result<WrapManifest> = Result.success(manifest)

    /**
     * Produce an instance of the package's Plugin Wrapper
     *
     * @return A [PluginWrapper] instance
     */
    override fun createWrapper(): Result<Wrapper> = Result.success(PluginWrapper(pluginModule))

    /**
     * Not Implemented. Throws a NotImplementedError.
     */
    override fun getFile(path: String): Result<ByteArray> {
        throw NotImplementedError()
    }
}
