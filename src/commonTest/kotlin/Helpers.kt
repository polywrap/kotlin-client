import com.goncalossilva.resources.Resource

fun readTestResource(filePath: String): Result<ByteArray> {
    val resourceRoot = "src/commonTest/resources"
    val resource = Resource("$resourceRoot/$filePath")
    return if (resource.exists()) {
        Result.success(resource.readBytes())
    } else {
        Result.failure(Exception("File not found at $filePath."))
    }
}
