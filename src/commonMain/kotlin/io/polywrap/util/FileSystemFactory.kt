package io.polywrap.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Get a reference to the current platform's FileSystem.
 */
expect object FileSystemFactory {
    /**
     * Get a reference to the FileSystem for the current platform.
     *
     * @return The FileSystem reference.
     */
    fun create(): FileSystem
}

/**
 * Read the contents of a file from the file system.
 *
 * @param filePath The path to the file to read.
 *
 * @return A [Result] containing either the file's contents as a [ByteArray] on success or an [Exception] on failure.
 */
suspend fun readFile(filePath: String): Deferred<Result<ByteArray>> = coroutineScope {
    async {
        runCatching {
            val absPath = filePath.toPath(true)
            val asyncBytes = FileSystemFactory.create().read(absPath) { readByteArray() }
            asyncBytes
        }
    }
}

/**
 * Check if the path is a directory.
 *
 * @return True if the path is a directory, false otherwise.
 */
fun Path.isDirectory(): Boolean =
    FileSystemFactory.create().metadataOrNull(this)?.isDirectory == true

/**
 * Check if the path is a file.
 *
 * @return True if the path is a file, false otherwise.
 */
fun Path.isFile(): Boolean =
    FileSystemFactory.create().metadataOrNull(this)?.isRegularFile == true

/**
 * Convert a potentially-relative path to an absolute path.
 *
 * @return The absolute path
 */
fun Path.absolute(): Result<Path> = runCatching {
    if (isAbsolute) {
        this
    } else {
        val currentDir = "".toPath()
        FileSystemFactory.create().canonicalize(currentDir) / (this)
    }
}
