package io.polywrap.util

import okio.FileSystem

actual object FileSystemFactory {
    actual fun create(): FileSystem = FileSystem.SYSTEM
}
