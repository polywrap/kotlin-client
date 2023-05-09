open class UniffiPipelineConfig(
    var isRelease: Boolean = false,
    var clonesDir: String = "",
    var desktopJniPath: String = "",
    var androidJniPath: String = "",
    var rustTargets: List<String> = listOf(),
    var libname: String = "",
    var uniffiKotlinMppBindingsDir: String = ""
)
