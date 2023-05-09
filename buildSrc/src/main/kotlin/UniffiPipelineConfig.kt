open class UniffiPipelineConfig(
    var isRelease: Boolean = false,
    var clonesDir: String = "",
    var rustClientRepoCloneDir: String = "",
    var rustTargetDir: String = "",
    var rustLibsDir: String = "",
    var desktopJniPath: String = "",
    var androidJniPath: String = "",
    var rustTargets: List<String> = listOf(),
    var libname: String = "",
    var uniffiKotlinMppBindingsDir: String = ""
)
