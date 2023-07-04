open class UniffiPipelineConfig(
    var isRelease: Boolean = false,
    var clonesDir: String = "",
    var desktopJnaPath: String = "",
    var androidJnaPath: String = "",
    var rustTargets: List<String> = listOf(),
    var packageName: String = "",
    var libname: String = "",
    var bindingsDir: String = "",
    var rustClientRepoBranch: String = "main"
)
