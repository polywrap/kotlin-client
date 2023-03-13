package eth.krisbitney.polywrap.wasm.runtime

/**
 * Writes the contents of the source byte array to the destination byte array,
 * starting at the specified destination offset. The source byte array is copied
 * into the destination byte array, overwriting any existing data in the destination
 * array at and after the specified offset.
 *
 * @param source the source byte array to be copied
 * @param destination the destination byte array to copy the source array to
 * @param dstOffset the offset in the destination array at which to start writing
 * @return the destination byte array
 */
fun writeBytes(source: ByteArray, destination: ByteArray, dstOffset: Int): ByteArray {
    source.copyInto(destination, dstOffset, 0, source.size)
    return destination
}

/**
 * Reads a specified number of bytes from the source byte array, starting at
 * the specified offset, into a new byte array. The resulting byte array contains
 * the bytes read from the source array, starting at the specified offset and
 * continuing for the specified length.
 *
 * @param source the source byte array to read from
 * @param srcOffset the offset in the source array at which to start reading
 * @param length the number of bytes to read from the source array
 * @return a new byte array containing the bytes read from the source array
 */
fun readBytes(source: ByteArray, srcOffset: Int, length: Int): ByteArray {
    val destination = ByteArray(length)
    source.copyInto(destination, 0, srcOffset, srcOffset + length)
    return destination
}