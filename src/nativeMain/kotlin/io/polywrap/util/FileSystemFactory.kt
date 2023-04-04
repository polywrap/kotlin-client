package io.polywrap.util

import okio.FileSystem

/**
 * Get a reference to a native FileSystem implementation.
 */
actual object FileSystemFactory {
    /**
     * Get a reference to a native FileSystem implementation.
     *
     * @return The FileSystem reference.
     */
    actual fun create(): FileSystem = FileSystem.SYSTEM
}
