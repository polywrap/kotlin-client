package eth.krisbitney.polywrap.plugin

import eth.krisbitney.polywrap.core.types.WrapPackage
import eth.krisbitney.polywrap.core.types.Wrapper
import eth.krisbitney.polywrap.core.wrap.WrapManifest
import kotlinx.coroutines.Deferred

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
    override suspend fun getManifest(): Result<WrapManifest> {
        return Result.success(manifest)
    }

    /**
     * Produce an instance of the package's Plugin Wrapper
     *
     * @return A [PluginWrapper] instance
     */
    override suspend fun createWrapper(): Result<Wrapper> {
        return Result.success(PluginWrapper(pluginModule))
    }

    /**
     * Not Implemented. Throws a NotImplementedError.
     */
    override suspend fun getFile(path: String): Deferred<Result<ByteArray>> {
        throw NotImplementedError()
    }
}
