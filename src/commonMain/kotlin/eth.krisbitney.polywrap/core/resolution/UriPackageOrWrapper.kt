package eth.krisbitney.polywrap.core.resolution

/**
 * Indicates that a URI resolved to either a wrap package, a wrapper, or a URI
 */
sealed class UriPackageOrWrapper {

    /**
     * Indicates that a URI resolved to a Uri
     * @property uri The resolved URI value
     */
    data class UriValue(val uri: Uri) : UriPackageOrWrapper()

    /**
     * Indicates that a URI resolved to a wrap package
     * @property pkg The resolved package value
     */
    data class PackageValue(val pkg: PackageRedirect) : UriPackageOrWrapper()

    /**
     * Indicates that a URI resolved to a wrapper
     * @property wrapper The resolved wrapper value
     */
    data class WrapperValue(val wrapper: WrapperRedirect) : UriPackageOrWrapper()
}