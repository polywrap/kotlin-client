package resolution

/**
 * Indicates that a URI resolved to either a wrap package, a wrapper, or a URI
 * @property type The type of the resolved value, which will be one of: "uri", "package", or "wrapper"
 */
sealed class UriPackageOrWrapper {
    abstract val type: String
    abstract val uri: Uri

    /**
     * Indicates that a URI resolved to a Uri
     * @property type The type of the resolved value, which is always "uri"
     * @property uri The resolved URI value
     */
    data class UriValue(override val uri: Uri) : UriPackageOrWrapper() {
        override val type: String = "uri"
    }

    /**
     * Indicates that a URI resolved to a wrap package
     * @property type The type of the resolved value, which is always "package"
     * @property uri The resolved wrap package value, which implements the IUriPackage interface
     */
    data class UriPackageValue(val pkg: PackageRedirect) : UriPackageOrWrapper() {
        override val type: String = "package"
        override val uri: Uri
            get() = pkg.uri
    }

    /**
     * Indicates that a URI resolved to a wrapper
     * @property type The type of the resolved value, which is always "wrapper"
     * @property uri The resolved wrapper value, which implements the IUriWrapper interface
     */
    data class UriWrapperValue(val wrapper: WrapperRedirect) : UriPackageOrWrapper() {
        override val type: String = "wrapper"
        override val uri: Uri
            get() = wrapper.uri
    }
}