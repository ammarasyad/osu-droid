package com.soratsuki.datatypes

enum class Depth(val value: Int, val visibleWhenPassing: Boolean = true, val visibleWhenFailing: Boolean = true) {
    BACKGROUND(0),
    FAIL(1, visibleWhenPassing = false),
    PASS(2, visibleWhenFailing = false),
    FOREGROUND(3),
    OVERLAY(4);

    companion object {
        fun parse(s: String): Depth {
            if (s.isEmpty())
                throw IllegalArgumentException("Depth is empty")

            val f = s[0]
            if (f in '0'..'3')
                return entries[f - '0']

            return when (f) {
                'B' -> BACKGROUND
                'F' -> {
                    val c = s[1]
                    if (c == 'A') FAIL else FOREGROUND
                }
                'O' -> OVERLAY
                'P' -> PASS
                else -> throw IllegalArgumentException("Invalid depth: $s")
            }
        }
    }
}