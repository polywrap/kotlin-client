package eth.krisbitney.polywrap.core.resolution

/**
 * A step in the URI resolution algorithm
 *
 * @property sourceUri - The current URI being resolved
 * @property result - The resolution result for the current URI
 * @property description - A text/visual description of this URI step
 * @property subHistory - History of sub-steps that exist within the context of this URI resolution step
 */
data class UriResolutionStep(
    val sourceUri: Uri,
    val result: Result<UriPackageOrWrapper>,
    val description: String? = null,
    val subHistory: List<UriResolutionStep>? = null
)
