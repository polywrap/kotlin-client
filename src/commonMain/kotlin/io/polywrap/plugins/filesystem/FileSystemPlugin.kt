package io.polywrap.plugins.filesystem

import io.polywrap.core.Invoker
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.filesystem.wrap.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A plugin for interacting with the platform's file system
 *
 * @property config An optional configuration object for the plugin.
 */
class FileSystemPlugin(config: Config? = null) : Module<FileSystemPlugin.Config?>(config) {

    /**
     * Configuration class for FileSystemPlugin.
     */
    class Config()

    override suspend fun readFile(args: ArgsReadFile, invoker: Invoker): ByteArray = coroutineScope {
        async {
            val absPath = args.path.toPath(true).absolute()
            FileSystem.SYSTEM.read(absPath) { readByteArray() }
        }.await()
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun readFileAsString(args: ArgsReadFileAsString, invoker: Invoker): String = coroutineScope {
        async {
            val bytes = readFile(ArgsReadFile(args.path), invoker)

            when (args.encoding ?: Encoding.UTF8) {
                Encoding.ASCII -> bytes.decodeToString() // ascii is a subset of utf-8
                Encoding.UTF8 -> bytes.decodeToString()
                Encoding.UTF16LE -> throw NotImplementedError("UTF16LE encoding is not supported")
                Encoding.UCS2 -> throw NotImplementedError("UCS2 encoding is not supported")
                Encoding.BASE64 -> Base64.encode(bytes)
                Encoding.BASE64URL -> Base64.UrlSafe.encode(bytes)
                Encoding.LATIN1 -> throw NotImplementedError("LATIN1 encoding is not supported")
                Encoding.BINARY -> throw NotImplementedError("BINARY encoding is not supported")
                Encoding.HEX -> bytes.encodeHex()
            }
        }.await()
    }

    override suspend fun exists(args: ArgsExists, invoker: Invoker): Boolean {
        val absPath = args.path.toPath(true).absolute()
        return FileSystem.SYSTEM.exists(absPath)
    }

    /** writes file to path; overwrites file if exists */
    override suspend fun writeFile(args: ArgsWriteFile, invoker: Invoker): Boolean? = coroutineScope {
        async {
            val absPath = args.path.toPath(true).absolute()
            FileSystem.SYSTEM.write(absPath) { write(args.data) }
            true
        }.await()
    }

    override suspend fun mkdir(args: ArgsMkdir, invoker: Invoker): Boolean? = coroutineScope {
        async {
            val absPath = args.path.toPath(true).absolute()
            if (args.recursive == true) {
                FileSystem.SYSTEM.createDirectories(absPath)
            } else {
                FileSystem.SYSTEM.createDirectory(absPath, true)
            }
            true
        }.await()
    }

    override suspend fun rm(args: ArgsRm, invoker: Invoker): Boolean? = coroutineScope {
        async {
            val absPath = args.path.toPath(true).absolute()
            val force = args.force == true
            if (args.recursive == true) {
                FileSystem.SYSTEM.deleteRecursively(absPath, !force)
            } else {
                FileSystem.SYSTEM.delete(absPath, !force)
            }
            true
        }.await()
    }

    override suspend fun rmdir(args: ArgsRmdir, invoker: Invoker): Boolean? = coroutineScope {
        async {
            val absPath = args.path.toPath(true).absolute()
            FileSystem.SYSTEM.delete(absPath, true)
            true
        }.await()
    }

    /**
     * Encode a byte array as a hex string.
     */
    private fun ByteArray.encodeHex(): String = joinToString("") { it.toInt().and(0xFF).toString(16) }

    /**
     * Convert a potentially-relative path to an absolute path.
     *
     * @return The absolute path
     */
    private fun Path.absolute(): Path {
        return if (isAbsolute) {
            this
        } else {
            val currentDir = "".toPath()
            FileSystem.SYSTEM.canonicalize(currentDir) / (this)
        }
    }
}

val fileSystemPlugin: PluginFactory<FileSystemPlugin.Config?> = { config: FileSystemPlugin.Config? ->
    PluginPackage(
        pluginModule = FileSystemPlugin(config),
        manifest = manifest
    )
}
