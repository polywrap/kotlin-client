package eth.krisbitney.polywrap.core.resolution

/**
 * Track and output URI resolution state, path, and history.
 */
interface UriResolutionContext {
    /**
     * Check if a URI is in the process of being resolved.
     *
     * @param uri the URI to check
     * @return true if URI resolution is in process, false otherwise
     */
    fun isResolving(uri: Uri): Boolean

    /**
     * Start resolving a URI.
     *
     * @param uri the URI to resolve
     */
    fun startResolving(uri: Uri)

    /**
     * Stop resolving a URI.
     *
     * @param uri the URI being resolved
     */
    fun stopResolving(uri: Uri)

    /**
     * Push a step onto the resolution history stack.
     *
     * @param step a completed resolution step
     */
    fun trackStep(step: UriResolutionStep)

    /**
     * Get the history of all URI resolution steps completed.
     *
     * @return the history of all URI resolution steps completed
     */
    fun getHistory(): List<UriResolutionStep>

    /**
     * Get the current URI resolution path.
     *
     * @return the current URI resolution path
     */
    fun getResolutionPath(): List<Uri>

    /**
     * Create a new resolution context using the current URI resolution path.
     *
     * @return a new resolution context using the current URI resolution path
     */
    fun createSubHistoryContext(): UriResolutionContext

    /**
     * Create a new resolution context using the current URI resolution history.
     *
     * @return a new resolution context using the current URI resolution history
     */
    fun createSubContext(): UriResolutionContext
}
