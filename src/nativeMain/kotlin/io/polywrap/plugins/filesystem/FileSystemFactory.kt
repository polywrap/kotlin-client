package io.polywrap.plugins.filesystem

import okio.FileSystem

/**
 * Get a reference to a native FileSystem implementation.
 */
internal actual object FileSystemFactory {
    /**
     * Get a reference to a native FileSystem implementation.
     *
     * @return The FileSystem reference.
     */
    actual fun create(): FileSystem = FileSystem.SYSTEM
}