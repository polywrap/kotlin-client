package io.polywrap.util

import kotlin.js.Json
import kotlin.js.json

inline fun <T : Json> jsObject(crossinline builder: T.() -> Unit): T {
    return json().unsafeCast<T>().apply(builder)
}
