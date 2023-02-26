package types

import DeserializeManifestOptions
import WrapManifest

/**
 * A wrap package, capable of producing instances of a wrapper and its manifest
 */
interface WrapPackage {
    /**
     * Produce an instance of the wrap manifest
     *
     * @param noValidate If true, manifest validation step will be skipped
     * @return A Promise with a Result containing the wrap manifest or an error
     */
    suspend fun getManifest(noValidate: Boolean = false): Result<WrapManifest>

    /**
     * Produce an instance of the wrapper
     *
     * @param options DeserializeManifestOptions; customize manifest deserialization
     * @return A Promise with a Result containing the wrapper or an error
     */
    suspend fun createWrapper(options: DeserializeManifestOptions?): Result<Wrapper>
}
