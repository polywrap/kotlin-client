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
    abstract val uri: Uri

    override fun getKind(): UriPackageOrWrapperKind {
        return when(this) {
            is UriValue -> UriPackageOrWrapperKind.URI
            is UriWrapPackage -> UriPackageOrWrapperKind.PACKAGE
            is UriWrapper -> UriPackageOrWrapperKind.WRAPPER
        }
    }

    override fun asUri(): Uri {
        if (this is UriValue) {
            return this.uri
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
}

/**
 * Indicates that a URI resolved to a Uri
 * @property uri The resolved URI value
 */
data class UriValue(override val uri: Uri) : UriPackageOrWrapper()

/**
 * Indicates that a URI resolved to a wrap package
 * @property pkg The resolved package value
 * @property uri The resolved URI value
 */
data class UriWrapPackage(override val uri: Uri, val pkg: FfiWrapPackage) : UriPackageOrWrapper(), FfiUriWrapPackage {
    override fun getUri(): Uri = uri

    override fun getPackage(): FfiWrapPackage = pkg

}

/**
 * Indicates that a URI resolved to a wrapper
 * @property wrapper The resolved wrapper value
 * @property uri The resolved URI value
 */
data class UriWrapper(override val uri: Uri, val wrapper: Wrapper) : UriPackageOrWrapper(), FfiUriWrapper {
    override fun getUri(): Uri = uri

    override fun getWrapper(): Wrapper = wrapper

}
