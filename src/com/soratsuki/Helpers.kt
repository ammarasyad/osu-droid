package com.soratsuki

inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    var i = 0
    val size = size
    while (i < size) action(this[i++])
}

inline fun <T> Array<out T>.fastForEach(action: (T) -> Unit) {
    for (i in indices) action(this[i])
}

inline fun <T> Array<out T>.fastForEachIndexed(action: (Int, T) -> Unit) {
    for (i in indices) action(i, this[i])
}