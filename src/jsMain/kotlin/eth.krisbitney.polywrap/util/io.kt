package eth.krisbitney.polywrap.util

import okio.FileSystem
import okio.NodeJsFileSystem

actual object FileSystemFactory {
    actual fun create(): FileSystem = NodeJsFileSystem
}