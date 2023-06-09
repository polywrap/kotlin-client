package io.polywrap.core.resolution

import io.polywrap.core.Wrapper
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriWrapPackage
import uniffi.main.FfiUriWrapper
import uniffi.main.FfiWrapPackage

/**
 * Indicates that a URI resolved to either a wrap package, a wrapper, or a URI
 */
sealed class UriPackageOrWrapper : FfiUriPackageOrWrapper {
    abstract val uriValue: Uri

    override fun getKind(): UriPackageOrWrapperKind {
        return when (this) {
            is UriValue -> UriPackageOrWrapperKind.URI
            is UriWrapPackage -> UriPackageOrWrapperKind.PACKAGE
            is UriWrapper -> UriPackageOrWrapperKind.WRAPPER
        }
    }

    override fun asUri(): Uri {
        if (this is UriValue) {
            return this.uriValue
        } else {
            throw IllegalStateException("Not a URI")
        }
    }

    override fun asWrapper(): FfiUriWrapper {
        if (this is UriWrapper) {
            return this
        } else {
            throw IllegalStateException("Not a wrapper")
        }
    }

    override fun asPackage(): FfiUriWrapPackage {
        if (this is UriWrapPackage) {
            return this
        } else {
            throw IllegalStateException("Not a package")
        }
    }

    /**
     * Indicates that a URI resolved to a Uri
     * @property uriValue The resolved URI value
     */
    data class UriValue(override val uriValue: Uri) : UriPackageOrWrapper()

    /**
     * Indicates that a URI resolved to a wrap package
     * @property pkg The resolved package value
     * @property uriValue The resolved URI value
     */
    data class UriWrapPackage(override val uriValue: Uri, val pkg: FfiWrapPackage) : UriPackageOrWrapper(), FfiUriWrapPackage {
        override fun getUri(): Uri = uriValue

        override fun getPackage(): FfiWrapPackage = pkg
    }

    /**
     * Indicates that a URI resolved to a wrapper
     * @property wrap The resolved wrapper value
     * @property uriValue The resolved URI value
     */
    data class UriWrapper(override val uriValue: Uri, val wrap: Wrapper) : UriPackageOrWrapper(), FfiUriWrapper {
        override fun getUri(): Uri = uriValue

        override fun getWrapper(): Wrapper = wrap
    }
}
