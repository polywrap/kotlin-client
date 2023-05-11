fun rustTargetToAndroidAbiOrDesktopValue(target: String): String {
    return when (target) {
        "armv7-linux-androideabi" -> "armeabi-v7a"
        "i686-linux-android" -> "x86"
        "aarch64-linux-android" -> "arm64-v8a"
        "x86_64-linux-android" -> "x86_64"
        "x86_64-apple-darwin" -> "x86_64-darwin"
        "aarch64-apple-darwin" -> "aarch64-darwin"
        "aarch64-unknown-linux-gnu" -> "aarch64-linux"
        "x86_64-unknown-linux-gnu" -> "x86_64-linux"
        "x86_64-pc-windows-gnu" -> "x86_64-windows"
        else -> "x86_64"
    }
}

