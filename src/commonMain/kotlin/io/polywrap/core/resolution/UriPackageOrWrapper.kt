package io.polywrap.core.resolution

import io.polywrap.core.Wrapper
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriPackageOrWrapperKind
import uniffi.main.FfiUriWrapPackage
import uniffi.main.FfiUriWrapper
import uniffi.main.FfiWrapPackage

/**
 * Indicates that a URI resolved to either a wrap package, a wrapper, or a URI
 */
sealed class UriPackageOrWrapper : FfiUriPackageOrWrapper, AutoCloseable {
    abstract val uriValue: FfiUri

    override fun getKind(): FfiUriPackageOrWrapperKind {
        return when (this) {
            is UriValue -> FfiUriPackageOrWrapperKind.URI
            is UriWrapPackage -> FfiUriPackageOrWrapperKind.PACKAGE
            is UriWrapper -> FfiUriPackageOrWrapperKind.WRAPPER
        }
    }

    override fun asUri(): FfiUri {
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
    data class UriValue(override val uriValue: FfiUri) : UriPackageOrWrapper() {
        override fun close() = this.uriValue.close()
    }

    /**
     * Indicates that a URI resolved to a wrap package
     * @property pkg The resolved package value
     * @property uriValue The resolved URI value
     */
    data class UriWrapPackage(override val uriValue: FfiUri, val pkg: FfiWrapPackage) : UriPackageOrWrapper(), FfiUriWrapPackage {
        override fun getUri(): FfiUri = uriValue

        override fun getPackage(): FfiWrapPackage = pkg

        override fun close() = this.uriValue.close()
    }

    /**
     * Indicates that a URI resolved to a wrapper
     * @property wrap The resolved wrapper value
     * @property uriValue The resolved URI value
     */
    data class UriWrapper(override val uriValue: FfiUri, val wrap: Wrapper) : UriPackageOrWrapper(), FfiUriWrapper {
        override fun getUri(): FfiUri = uriValue

        override fun getWrapper(): Wrapper = wrap

        override fun close() {
            this.uriValue.close()
            if (this.wrap is AutoCloseable) {
                this.wrap.close()
            }
        }
    }
}
