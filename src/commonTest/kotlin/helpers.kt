import eth.krisbitney.polywrap.util.FileSystemFactory
import eth.krisbitney.polywrap.util.readFile
import okio.Path.Companion.toPath

//fun readTestResource(filePath: String): Result<ByteArray> {
//    val resourceRoot = "src/commonTest/resources";
//    val resource = Resource("$resourceRoot/$filePath")
//    return if (resource.exists()) {
//        val bytes = resource.readText().encodeToByteArray()
//        Result.success(bytes)
//    } else {
//        Result.failure(Exception("File not found at $filePath."));
//    }
//}

suspend fun readTestResource(filePath: String): Result<ByteArray> {
    val resourceRoot = "/Users/kris/IdeaProjects/krisbitney/polywrap-kt/src/commonTest/resources"
    val resource = "$resourceRoot/$filePath"
    val fs = FileSystemFactory.create()
    return if (fs.exists(resource.toPath())) {
        val bytes = readFile(resource).await().getOrThrow()
        Result.success(bytes)
    } else {
        Result.failure(Exception("File not found at $filePath."))
    }
}

