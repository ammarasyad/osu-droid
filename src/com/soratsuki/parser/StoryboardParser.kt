package com.soratsuki.parser

import androidx.core.text.isDigitsOnly
import com.edlplan.framework.math.Anchor
import com.edlplan.framework.math.Vec2
import com.soratsuki.Storyboard
import com.soratsuki.commands.StoryboardAlphaCommand
import com.soratsuki.commands.StoryboardBlendCommand
import com.soratsuki.commands.StoryboardColorCommand
import com.soratsuki.commands.StoryboardCommandGroup
import com.soratsuki.commands.StoryboardFlipHCommand
import com.soratsuki.commands.StoryboardFlipVCommand
import com.soratsuki.commands.StoryboardRotationCommand
import com.soratsuki.commands.StoryboardScaleCommand
import com.soratsuki.commands.StoryboardXCommand
import com.soratsuki.commands.StoryboardYCommand
import com.soratsuki.datatypes.Depth
import com.soratsuki.datatypes.EaseElasticOutHalf
import com.soratsuki.datatypes.EaseElasticOutQuarter
import com.soratsuki.datatypes.EasePow10Out
import com.soratsuki.datatypes.ElementType
import com.soratsuki.datatypes.ElementType.*
import com.soratsuki.datatypes.LoopType
import com.soratsuki.layers.StoryboardLayer
import com.soratsuki.sprites.StoryboardSprite
import com.soratsuki.sprites.StoryboardAnimationSprite
import com.soratsuki.sprites.StoryboardBasicSprite
import com.soratsuki.texture.StoryboardTexturePool
import org.andengine.util.color.Color
import org.andengine.util.debug.Debug
import org.andengine.util.modifier.ease.EaseBackIn
import org.andengine.util.modifier.ease.EaseBackInOut
import org.andengine.util.modifier.ease.EaseBackOut
import org.andengine.util.modifier.ease.EaseBounceIn
import org.andengine.util.modifier.ease.EaseBounceInOut
import org.andengine.util.modifier.ease.EaseBounceOut
import org.andengine.util.modifier.ease.EaseCircularIn
import org.andengine.util.modifier.ease.EaseCircularInOut
import org.andengine.util.modifier.ease.EaseCircularOut
import org.andengine.util.modifier.ease.EaseCubicIn
import org.andengine.util.modifier.ease.EaseCubicInOut
import org.andengine.util.modifier.ease.EaseCubicOut
import org.andengine.util.modifier.ease.EaseElasticIn
import org.andengine.util.modifier.ease.EaseElasticInOut
import org.andengine.util.modifier.ease.EaseElasticOut
import org.andengine.util.modifier.ease.EaseExponentialIn
import org.andengine.util.modifier.ease.EaseExponentialInOut
import org.andengine.util.modifier.ease.EaseExponentialOut
import org.andengine.util.modifier.ease.EaseLinear
import org.andengine.util.modifier.ease.EaseQuadIn
import org.andengine.util.modifier.ease.EaseQuadInOut
import org.andengine.util.modifier.ease.EaseQuadOut
import org.andengine.util.modifier.ease.EaseQuartIn
import org.andengine.util.modifier.ease.EaseQuartInOut
import org.andengine.util.modifier.ease.EaseQuartOut
import org.andengine.util.modifier.ease.EaseQuintIn
import org.andengine.util.modifier.ease.EaseQuintInOut
import org.andengine.util.modifier.ease.EaseQuintOut
import org.andengine.util.modifier.ease.EaseSineIn
import org.andengine.util.modifier.ease.EaseSineInOut
import org.andengine.util.modifier.ease.EaseSineOut
import org.andengine.util.modifier.ease.IEaseFunction
import java.io.File
import kotlin.math.max

class StoryboardParser(var storyboard: Storyboard?, private val texturePool: StoryboardTexturePool) {
    var sprite: StoryboardSprite? = null
    private var currentCommandGroup: StoryboardCommandGroup? = null
    private val variableHashMap = mutableMapOf<String, String>()

    fun release() {
        currentCommandGroup = null
        sprite = null
        storyboard = null
        variableHashMap.clear()
    }

    fun parseStoryboardFile(file: File) =
        file.bufferedReader().use { reader ->
            var line = reader.readLine()
            var isVariableSection = false
            var isEventSection = false
            while (line != null) {
                val trim = line.trim()
                if (trim.isBlank() || trim.startsWith("//")) {
                    line = reader.readLine()
                    continue
                }

                when {
                    isVariableSection -> handleVariables(line)
                    isEventSection -> handleEvents(line)
//                    else -> Debug.w("Unknown section: $line")
                }

                if (trim.startsWith('[') && trim.endsWith(']')) {
                    isVariableSection = trim == "[Variables]"
                    isEventSection = trim == "[Events]"
                }

//                when (trim) {
//                    "Variables" -> handleVariables(line)
//                    "Events" -> handleEvents(line)
//                    else -> Debug.w("Unknown section: $line")
//                }

                line = reader.readLine()
            }
        }

    // TODO: Handle UseSkinSprites?
//    fun handleGeneral(line: String) {
//
//    }

    private fun handleEvents(line: String) {
        // Format: Sprite, Depth, Origin, Filename, X, Y
        val decodedLine = decodeVariables(line)
        try {
            val depth = decodedLine.count { it == '_' || it == ' '}
            val split = decodedLine.substring(depth).split(",")
            if (depth == 0) {
                val type = ElementType.parse(split[0]) ?: throw IllegalArgumentException("Unknown element type: ${split[0]}")
                when (type) {
                    Sprite -> {
                        val layer = StoryboardLayer.parse(split[1])
                        val origin = parseOrigin(split[2])
                        val filename = cleanFilename(split[3])
                        val x = split[4].toFloat()
                        val y = split[5].toFloat()

                        sprite = StoryboardBasicSprite(layer, origin, filename, x, y, texturePool.getOrAdd(filename))
                    }
                    Animation -> {
                        val layer = StoryboardLayer.parse(split[1])
                        val origin = parseOrigin(split[2])
                        val filename = cleanFilename(split[3])
                        val x = split[4].toFloat()
                        val y = split[5].toFloat()
                        val frameCount = split[6].toInt()
                        val frameDelay = split[7].toDouble()
                        val loopType = LoopType.parse(split[8])

                        sprite = StoryboardAnimationSprite(layer, origin, filename, x, y, texturePool.getOrAdd(filename), frameCount, frameDelay, loopType)
                    }
                    Sample -> {
                        val time = split[1].toDouble()
                        val layer = StoryboardLayer.parse(split[2])
                        val path = cleanFilename(split[3])
                        val volume = if (split.size > 4) split[4].toDouble() else 100.0

                        storyboard?.addSampleCommand(time, layer, path, volume)
                    }
                    Background -> storyboard?.backgroundFile = cleanFilename(split[2])
//                    Background -> {
//                        val backgroundFile = cleanFilename(split[2])
//                        storyboard?.addElement(StoryboardBasicSprite(StoryboardLayer("bg", Depth.BACKGROUND), Anchor.Center, backgroundFile, 320f, 240f, texturePool.getOrAdd(backgroundFile).texture))
//                    }
                    /*Background, */Colour, Video, Break -> return // Not a storyboard event
                }
            } else {
                if (depth < 2)
                    currentCommandGroup = sprite?.commandGroup

                when (val commandType = split[0].substringAfter("__")) {
                    "T" -> TODO()
                    "L" -> {
                        val startTime = split[1].toDouble()
                        val loopCount = split[2].toInt()

                        currentCommandGroup = sprite?.addLoopingGroup(startTime, max(0, loopCount - 1))
                    }
                    else -> {
                        val easing = parseEasing(split[1])
                        val startTime = split[2].toFloat()
                        val endTime = if (split.size >= 4) split[3].toFloatOrNull() ?: startTime else startTime

                        when (commandType) {
                            "F" -> {
                                val startValue = split[4].toFloat()
                                val endValue = if ((split.size >= 6) && split[5].isNotBlank()) split[5].toFloat() else startValue
//                                sprite?.addCommand(StoryboardAlphaCommand(easing, startTime, endTime, startValue, endValue))
                                currentCommandGroup?.addCommand(StoryboardAlphaCommand(easing, startTime, endTime, startValue, endValue))
                            }

                            "S" -> {
                                val startValue = Vec2(split[4].toFloat())
                                val endValue = if ((split.size >= 6) && split[5].isNotBlank()) Vec2(split[5].toFloat()) else startValue
                                currentCommandGroup?.addCommand(StoryboardScaleCommand(easing, startTime, endTime, startValue, endValue))
                            }

                            "V" -> {
                                val startValue = Vec2(split[4].toFloat(), split[5].toFloat())
                                val endValue = if ((split.size >= 8) && split[6].isNotBlank() && split[7].isNotBlank()) Vec2(split[6].toFloat(), split[7].toFloat()) else startValue
                                currentCommandGroup?.addCommand(StoryboardScaleCommand(easing, startTime, endTime, startValue, endValue))
                            }

                            "R" -> {
                                val startValue = split[4].toFloat()
                                val endValue = if ((split.size >= 6) && split[5].isNotBlank()) split[5].toFloat() else startValue
                                currentCommandGroup?.addCommand(StoryboardRotationCommand(easing, startTime, endTime, startValue, endValue))
                            }

                            "M" -> {
                                val startX = split[4].toFloat()
                                val startY = split[5].toFloat()
                                val endX = if ((split.size >= 7) && split[6].isNotBlank()) split[6].toFloat() else startX
                                val endY = if ((split.size >= 8) && split[7].isNotBlank()) split[7].toFloat() else startY
                                currentCommandGroup?.addCommand(StoryboardXCommand(easing, startTime, endTime, startX, endX))
                                currentCommandGroup?.addCommand(StoryboardYCommand(easing, startTime, endTime, startY, endY))
                            }

                            "MX" -> {
                                val startValue = split[4].toFloat()
                                val endValue = if ((split.size >= 6) && split[5].isNotBlank()) split[5].toFloat() else startValue
                                currentCommandGroup?.addCommand(StoryboardXCommand(easing, startTime, endTime, startValue, endValue))
                            }

                            "MY" -> {
                                val startValue = split[4].toFloat()
                                val endValue = if ((split.size >= 6) && split[5].isNotBlank()) split[5].toFloat() else startValue
                                currentCommandGroup?.addCommand(StoryboardYCommand(easing, startTime, endTime, startValue, endValue))
                            }

                            "C" -> {
                                val startR = split[4].toFloat()
                                val startG = split[5].toFloat()
                                val startB = split[6].toFloat()
                                val endR = if ((split.size >= 8) && split[7].isNotBlank()) split[7].toFloat() else startR
                                val endG = if ((split.size >= 9) && split[8].isNotBlank()) split[8].toFloat() else startG
                                val endB = if ((split.size >= 10) && split[9].isNotBlank()) split[9].toFloat() else startB

                                val startColor = Color(startR, startG, startB)
                                val endColor = Color(endR, endG, endB)

                                currentCommandGroup?.addCommand(StoryboardColorCommand(easing, startTime, endTime, startColor, endColor))
                            }

                            "P" ->
                                when (split[4]) {
                                    "A" -> currentCommandGroup?.addCommand(StoryboardBlendCommand(easing, startTime, endTime, true, startTime == endTime))
                                    "H" -> currentCommandGroup?.addCommand(StoryboardFlipHCommand(easing, startTime, endTime, true, startTime == endTime))
                                    "V" -> currentCommandGroup?.addCommand(StoryboardFlipVCommand(easing, startTime, endTime, true, startTime == endTime))
                                }

                            else -> Debug.w("Unknown command type: $commandType")
                        }

                        sprite?.let {
                            storyboard?.addElement(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Debug.w("Unable to parse line: $line, skipping", e)
        }
    }

    private fun handleVariables(line: String) {
        val pair = splitKeyVal(line, "=", false)
        variableHashMap[pair.first] = pair.second
    }

    private fun decodeVariables(line: String): String {
        var decoded = line
        while (line.contains("$")) {
            for ((key, value) in variableHashMap) {
                decoded = decoded.replace(key, value)
            }

            if (decoded == line)
                break
        }

        return decoded
    }

    private companion object {
        fun splitKeyVal(line: String, delimiter: String, trim: Boolean = true): Pair<String, String> {
            val split = line.split(delimiter)
            return if (trim)
                split[0].trim() to split[1].trim()
            else
                split[0] to split[1]
        }

        fun cleanFilename(filename: String) = filename.replace("\"", "").replace("\\", "/")

        fun parseOrigin(origin: Int): Anchor = when (origin) {
            0 -> Anchor.TopLeft
            1 -> Anchor.Center
            2 -> Anchor.CenterLeft
            3 -> Anchor.TopRight
            4 -> Anchor.BottomCenter
            5 -> Anchor.TopCenter
            6 -> Anchor.TopLeft // Custom according to https://osu.ppy.sh/wiki/en/Storyboard/Scripting/Objects, but same effect as TopLeft
            7 -> Anchor.CenterRight
            8 -> Anchor.BottomLeft
            9 -> Anchor.BottomRight
            else -> throw IllegalArgumentException("Origin $origin is invalid!")
        }

        fun parseOrigin(origin: String): Anchor {
            if (origin.isDigitsOnly()) {
                return parseOrigin(origin.toInt())
            }

            return when (origin) {
                "TopLeft" -> Anchor.TopLeft
                "TopCentre" -> Anchor.TopCenter
                "TopRight" -> Anchor.TopRight
                "CentreLeft" -> Anchor.CenterLeft
                "Centre" -> Anchor.Center
                "CentreRight" -> Anchor.CenterRight
                "BottomLeft" -> Anchor.BottomLeft
                "BottomCentre" -> Anchor.BottomCenter
                "BottomRight" -> Anchor.BottomRight
                else -> throw IllegalArgumentException("Origin $origin is invalid!")
            }
        }

        fun parseEasing(easing: String): IEaseFunction {
            var easeFunction = easeMap[easing.toInt()]
            if (easeFunction == null) {
                Debug.w("Unknown easing function: $easing, defaulting to linear easing")
                easeFunction = EaseLinear.getInstance()
            }

            return easeFunction!!
        }

        private val easeMap = mapOf(
            0 to EaseLinear.getInstance(),
            1 to EaseQuadIn.getInstance(),
            2 to EaseQuadOut.getInstance(),
            3 to EaseQuadIn.getInstance(),
            4 to EaseQuadOut.getInstance(),
            5 to EaseQuadInOut.getInstance(),
            6 to EaseCubicIn.getInstance(),
            7 to EaseCubicOut.getInstance(),
            8 to EaseCubicInOut.getInstance(),
            9 to EaseQuartIn.getInstance(),
            10 to EaseQuartOut.getInstance(),
            11 to EaseQuartInOut.getInstance(),
            12 to EaseQuintIn.getInstance(),
            13 to EaseQuintOut.getInstance(),
            14 to EaseQuintInOut.getInstance(),
            15 to EaseSineIn.getInstance(),
            16 to EaseSineOut.getInstance(),
            17 to EaseSineInOut.getInstance(),
            18 to EaseExponentialIn.getInstance(),
            19 to EaseExponentialOut.getInstance(),
            20 to EaseExponentialInOut.getInstance(),
            21 to EaseCircularIn.getInstance(),
            22 to EaseCircularOut.getInstance(),
            23 to EaseCircularInOut.getInstance(),
            24 to EaseElasticIn.getInstance(),
            25 to EaseElasticOut.getInstance(),
            26 to EaseElasticOutHalf,
            27 to EaseElasticOutQuarter,
            28 to EaseElasticInOut.getInstance(),
            29 to EaseBackIn.getInstance(),
            30 to EaseBackOut.getInstance(),
            31 to EaseBackInOut.getInstance(),
            32 to EaseBounceIn.getInstance(),
            33 to EaseBounceOut.getInstance(),
            34 to EaseBounceInOut.getInstance(),
            // 35 to EasePow10Out, not mentioned in the wiki
        )
    }
}