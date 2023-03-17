package eth.krisbitney.polywrap.uriResolvers.util

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriPackageOrWrapper
import eth.krisbitney.polywrap.core.resolution.UriResolutionStep

/**
 * An exception that is thrown when an infinite loop is detected while resolving a URI.
 *
 * @param uri The URI that caused the infinite loop.
 * @param history A list of [UriResolutionStep] objects representing the resolution history.
 */
class InfiniteLoopException(uri: Uri, history: List<UriResolutionStep>)
    : Exception("An infinite loop was detected while resolving the URI: ${uri.uri}\n" +
        "History: ${getUriResolutionPath(history)}") {
    companion object {

        /**
         * Returns the URI resolution path filtered by relevant resolution steps.
         *
         * @param history A list of [UriResolutionStep] objects representing the resolution history.
         * @return A list of [UriResolutionStep] objects representing the filtered URI resolution path.
         */
        private fun getUriResolutionPath(history: List<UriResolutionStep>): List<UriResolutionStep> {
            return history.filter { step ->
                if (step.result.isFailure) {
                    true
                } else {
                    when (val uriPackageOrWrapper = step.result.getOrThrow()) {
                        is UriPackageOrWrapper.UriValue -> uriPackageOrWrapper.uri.uri != step.sourceUri.uri
                        is UriPackageOrWrapper.PackageValue -> true
                        is UriPackageOrWrapper.WrapperValue -> true
                    }
                }
            }.map { step ->
                if (!step.subHistory.isNullOrEmpty()) {
                    step.copy(subHistory = getUriResolutionPath(step.subHistory))
                } else {
                    step
                }
            }
        }
    }
}