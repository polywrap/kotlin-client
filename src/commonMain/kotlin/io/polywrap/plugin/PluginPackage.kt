package io.polywrap.plugin

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.wrap.WrapManifest
import uniffi.polywrap_native.FfiWrapper
import uniffi.polywrap_native.IffiWrapper

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

    override fun getManifest(): Result<WrapManifest> = Result.success(manifest)

    /**
     * Produce an instance of the package's Plugin Wrapper
     *
     * @return A [PluginWrapper] instance
     */
    override fun createWrapper(): PluginWrapper<TConfig> = PluginWrapper(pluginModule)

    override fun ffiCreateWrapper(): IffiWrapper = createWrapper()

    /**
     * Not Implemented. Throws a NotImplementedError.
     */
    override fun getFile(path: String): Result<ByteArray> {
        throw NotImplementedError()
    }
}
