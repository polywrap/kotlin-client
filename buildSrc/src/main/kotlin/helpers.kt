
fun rustTargetToAndroidAbiOrDesktopValue(target: String): String {
    return when (target) {
        "armv7-linux-androideabi" -> "android-armv7"
        "i686-linux-android" -> "android-x86"
        "aarch64-linux-android" -> "android-aarch64"
        "x86_64-linux-android" -> "android-x86-64"
        "x86_64-apple-darwin" -> "darwin-x86-64"
        "aarch64-apple-darwin" -> "darwin-aarch64"
        "aarch64-unknown-linux-gnu" -> "linux-aarch64"
        "x86_64-unknown-linux-gnu" -> "linux-x86-64"
        "x86_64-pc-windows-gnu" -> "win32-x86-64"
        else -> throw IllegalArgumentException("Unknown target: $target")
    }
}


