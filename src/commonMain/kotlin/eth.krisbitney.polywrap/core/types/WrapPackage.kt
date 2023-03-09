package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.wrap.WrapManifest

/**
 * A wrap package, capable of producing instances of a wrapper and its manifest
 */
interface WrapPackage {
    /**
     * Produce an instance of the wrap manifest
     *
     * @return A Result containing the wrap manifest or an error
     */
    suspend fun getManifest(): Result<WrapManifest>

    /**
     * Produce an instance of the wrapper
     *
     * @return A Result containing the wrapper or an error
     */
    suspend fun createWrapper(): Result<Wrapper>
}
