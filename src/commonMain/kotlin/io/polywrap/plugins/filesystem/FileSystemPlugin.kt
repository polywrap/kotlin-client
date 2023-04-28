package io.polywrap.plugins.filesystem

import io.polywrap.core.types.Invoker
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsExists
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsMkdir
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsReadFile
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsReadFileAsString
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsRm
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsRmdir
import io.polywrap.plugins.filesystem.wrapHardCoded.ArgsWriteFile
import io.polywrap.plugins.filesystem.wrapHardCoded.Module
import io.polywrap.plugins.http.wrapHardCoded.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Get a reference to the current platform's FileSystem.
 */
internal expect object FileSystemFactory {
    /**
     * Get a reference to the FileSystem for the current platform.
     *
     * @return The FileSystem reference.
     */
    fun create(): FileSystem
}

/**
 * A plugin for making HTTP requests.
 *
 * @property config An optional configuration object for the plugin.
 */
class FileSystemPlugin(config: Config? = null) : Module<FileSystemPlugin.Config?>(config) {

    /**
     * Configuration class for FileSystemPlugin.
     */
    class Config()

    override suspend fun readFile(args: ArgsReadFile, invoker: Invoker): Result<Bytes>  = coroutineScope {
        val deferred = async {
            runCatching {
                val absPath = args.path.toPath(true)
                val asyncBytes = FileSystemFactory.create().read(absPath) { readByteArray() }
                asyncBytes
            }
        }
        deferred.await()
    }

    override suspend fun readFileAsString(args: ArgsReadFileAsString, invoker: Invoker): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun exists(args: ArgsExists, invoker: Invoker): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun writeFile(args: ArgsWriteFile, invoker: Invoker): Result<Boolean?> {
        TODO("Not yet implemented")
    }

    override suspend fun mkdir(args: ArgsMkdir, invoker: Invoker): Result<Boolean?> {
        TODO("Not yet implemented")
    }

    override suspend fun rm(args: ArgsRm, invoker: Invoker): Result<Boolean?> {
        TODO("Not yet implemented")
    }

    override suspend fun rmdir(args: ArgsRmdir, invoker: Invoker): Result<Boolean?> {
        TODO("Not yet implemented")
    }


    /**
     * Check if the path is a directory.
     *
     * @return True if the path is a directory, false otherwise.
     */
    private fun Path.isDirectory(): Boolean =
        FileSystemFactory.create().metadataOrNull(this)?.isDirectory == true

    /**
     * Check if the path is a file.
     *
     * @return True if the path is a file, false otherwise.
     */
    private fun Path.isFile(): Boolean =
        FileSystemFactory.create().metadataOrNull(this)?.isRegularFile == true

    /**
     * Convert a potentially-relative path to an absolute path.
     *
     * @return The absolute path
     */
    private fun Path.absolute(): Result<Path> = runCatching {
        if (isAbsolute) {
            this
        } else {
            val currentDir = "".toPath()
            FileSystemFactory.create().canonicalize(currentDir) / (this)
        }
    }
}

val fileSystemPlugin: PluginFactory<FileSystemPlugin.Config?> = { config: FileSystemPlugin.Config? ->
    PluginPackage(
        pluginModule = FileSystemPlugin(config),
        manifest = io.polywrap.plugins.filesystem.wrapHardCoded.manifest
    )
}
