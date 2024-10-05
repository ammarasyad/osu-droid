package com.soratsuki.datatypes

enum class LoopType {
    LoopOnce,
    LoopForever;

    companion object {
        fun parse(s: String) = when (s) {
            "LoopOnce" -> LoopOnce
            "LoopForever" -> LoopForever
            else -> throw IllegalArgumentException("Invalid loop type: $s")
        }
    }
}