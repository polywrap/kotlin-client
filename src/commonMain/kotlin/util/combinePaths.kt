package util

/**
 * Combines two paths, normalizing any path separators and ensuring that there is exactly one separator between them.
 *
 * @param a the first path
 * @param b the second path to append to the first
 * @return the combined path
 */
fun combinePaths(a: String, b: String): String {
    // Normalize all path separators
    var pathA = a.replace("\\", "/")
    var pathB = b.replace("\\", "/")

    // Append a separator if one doesn't exist
    if (!pathA.endsWith("/")) {
        pathA += "/"
    }

    // Remove any leading separators from the second path
    var i = 0
    var leading = pathB[i]
    while (leading == '/' || leading == '.') {
        leading = pathB[++i]
    }
    pathB = pathB.substring(i)

    return pathA + pathB
}
