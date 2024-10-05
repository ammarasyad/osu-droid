package com.soratsuki.datatypes

enum class ElementType(val value: Int) {
    Background(0),
    Video(1),
    Break(2),
    Colour(3),
    Sprite(4),
    Sample(5),
    Animation(6);

    companion object {
        fun parse(s: String): ElementType? {
            if (s.isEmpty()) return null
            val f = s[0]
            if (f in '0'..'6') return entries[f - '0']
            return when (f) {
                'B' -> {
                    val c = s[1]
                    if (c == 'a') Background else Break
                }
                'V' -> Video
                'C' -> Colour
                'S' -> {
                    val c = s[1]
                    if (c == 'p') Sprite else Sample
                }
                'A' -> Animation
                else -> null
            }
        }
    }
}