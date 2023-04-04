package io.polywrap.util

import kotlin.js.Json
import kotlin.js.json

/**
 * Creates a JSON object of a specified type [T] by applying the given [builder] function.
 *
 * @param T A subtype of [Json] representing the desired JSON object type.
 * @param builder A lambda with receiver function that applies modifications to the JSON object.
 * @return A JSON object of type [T] with the modifications applied by the [builder] function.
 */
inline fun <T : Json> jsObject(crossinline builder: T.() -> Unit): T {
    return json().unsafeCast<T>().apply(builder)
}
