package eth.krisbitney.polywrap.core.resolution

/**
 * A context for URI resolution that keeps track of which URIs are being resolved,
 * the path of resolution, and the history of steps taken to resolve the URIs.
 *
 * @property resolvingUriMap a mutable map that keeps track of whether a URI is being resolved
 * @property resolutionPath a mutable set that keeps track of the current path of resolution
 * @property history a mutable list that keeps track of the history of steps taken to resolve URIs
 */
class BasicUriResolutionContext(
    private val resolvingUriMap: MutableMap<String, Boolean> = mutableMapOf(),
    private val resolutionPath: MutableSet<String> = mutableSetOf(),
    private val history: MutableList<UriResolutionStep> = mutableListOf()
) : UriResolutionContext {

    /**
     * Constructs a UriResolutionContext with the given resolving URI map and history.
     * @param resolvingUriMap a map that keeps track of whether a URI is being resolved
     * @param history a list that keeps track of the history of steps taken to resolve URIs
     */
    constructor(resolvingUriMap: MutableMap<String, Boolean>, history: MutableList<UriResolutionStep>) : this(
        resolvingUriMap,
        mutableSetOf(),
        history
    )

    /**
     * Checks whether a given URI is currently being resolved.
     * @param uri the URI to check
     * @return true if the URI is currently being resolved, false otherwise
     */
    override fun isResolving(uri: Uri): Boolean {
        return resolvingUriMap[uri.toString()] ?: false
    }

    /**
     * Marks a given URI as currently being resolved and adds it to the path of resolution.
     * @param uri the URI to start resolving
     */
    override fun startResolving(uri: Uri) {
        resolvingUriMap[uri.toString()] = true
        resolutionPath.add(uri.toString())
    }

    /**
     * Marks a given URI as no longer being resolved.
     * @param uri the URI to stop resolving
     */
    override fun stopResolving(uri: Uri) {
        resolvingUriMap.remove(uri.toString())
    }

    /**
     * Adds a step to the history of resolution.
     * @param step the step to add to the history
     */
    override fun trackStep(step: UriResolutionStep) {
        history.add(step)
    }

    /**
     * Returns an immutable copy of the history of resolution.
     * @return an immutable list of the steps taken to resolve URIs
     */
    override fun getHistory(): List<UriResolutionStep> {
        return history.toList()
    }

    /**
     * Returns an immutable copy of the path of resolution.
     * @return an immutable list of URIs representing the current path of resolution
     */
    override fun getResolutionPath(): List<Uri> {
        return resolutionPath.map { Uri(it) }
    }

    /**
     * Creates a sub-context of this context with the same URI map and current path of resolution.
     * @return a new UriResolutionContext instance with a copy of the URI map and current path of resolution
     */
    override fun createSubHistoryContext(): UriResolutionContext {
        return BasicUriResolutionContext(resolvingUriMap.toMutableMap(), resolutionPath)
    }

    /**
     * Creates a sub-context of this context with the same URI map and history of resolution.
     * @return a new UriResolutionContext instance with a copy of the URI map and history of resolution
     */
    override fun createSubContext(): UriResolutionContext {
        return BasicUriResolutionContext(resolvingUriMap.toMutableMap(), history)
    }
}