package eth.krisbitney.polywrap.core.resolution

import eth.krisbitney.polywrap.core.types.WrapPackage
import eth.krisbitney.polywrap.core.types.Wrapper

/**
 * Indicates that a URI resolved to either a wrap package, a wrapper, or a URI
 */
sealed class UriPackageOrWrapper {
    abstract val uri: Uri

    /**
     * Indicates that a URI resolved to a Uri
     * @property uri The resolved URI value
     */
    class UriValue(override val uri: Uri) : UriPackageOrWrapper()

    /**
     * Indicates that a URI resolved to a wrap package
     * @property pkg The resolved package value
     * @property uri The resolved URI value
     */
    class PackageValue(override val uri: Uri, val pkg: WrapPackage) : UriPackageOrWrapper()

    /**
     * Indicates that a URI resolved to a wrapper
     * @property wrapper The resolved wrapper value
     * @property uri The resolved URI value
     */
    class WrapperValue(override val uri: Uri, val wrapper: Wrapper) : UriPackageOrWrapper()
}