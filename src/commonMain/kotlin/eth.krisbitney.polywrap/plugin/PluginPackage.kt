package eth.krisbitney.polywrap.plugin

import eth.krisbitney.polywrap.core.types.WrapPackage
import eth.krisbitney.polywrap.core.types.Wrapper
import eth.krisbitney.polywrap.core.wrap.WrapManifest
import kotlinx.coroutines.Deferred

class PluginPackage<TConfig>(
    private val pluginModule: PluginModule<TConfig>,
    private val manifest: WrapManifest
) : WrapPackage {


    override suspend fun getManifest(): Result<WrapManifest> {
        return Result.success(manifest)
    }

    override suspend fun createWrapper(): Result<Wrapper> {
        return Result.success(PluginWrapper(pluginModule))
    }

    override suspend fun getFile(path: String): Deferred<Result<ByteArray>> {
        TODO("Not implemented")
    }
}