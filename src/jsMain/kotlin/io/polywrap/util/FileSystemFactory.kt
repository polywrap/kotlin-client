package io.polywrap.util

import okio.FileSystem
import okio.NodeJsFileSystem

/**
 * Get a reference to the NodeJS FileSystem implementation.
 */
actual object FileSystemFactory {
    /**
     * Get a reference to the NodeJS FileSystem implementation.
     *
     * @return The FileSystem reference.
     */
    actual fun create(): FileSystem = NodeJsFileSystem
}
