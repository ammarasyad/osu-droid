package com.soratsuki.datatypes

import org.andengine.util.math.MathConstants
import org.andengine.util.modifier.ease.IEaseFunction
import kotlin.math.pow
import kotlin.math.sin

object EaseElasticOutHalf : IEaseFunction {
    override fun getPercentage(pSecondsElapsed: Float, pDuration: Float): Float {
        val pPercentageDone = pSecondsElapsed / pDuration

        if (pSecondsElapsed == 0f) {
            return 0f
        }
        if (pSecondsElapsed == pDuration) {
            return 1f
        }

        val p = pDuration * 0.3f
        val s = p / 4

        return 1 + 2f.pow((-10 * pPercentageDone)) * sin(((0.5f * pPercentageDone * pDuration - s) * MathConstants.PI_TWICE / p).toDouble()).toFloat()
    }
}

object EaseElasticOutQuarter : IEaseFunction {
    override fun getPercentage(pSecondsElapsed: Float, pDuration: Float): Float {
        val pPercentageDone = pSecondsElapsed / pDuration

        if (pSecondsElapsed == 0f) {
            return 0f
        }
        if (pSecondsElapsed == pDuration) {
            return 1f
        }

        val p = pDuration * 0.3f
        val s = p / 4

        return 1 + 2f.pow((-10 * pPercentageDone)) * sin(((0.25f * pPercentageDone * pDuration - s) * MathConstants.PI_TWICE / p).toDouble()).toFloat()
    }
}

object EasePow10Out : IEaseFunction {
    override fun getPercentage(pSecondsElapsed: Float, pDuration: Float): Float {
        val pPercentageDone = (pSecondsElapsed / pDuration) - 1f

        return pPercentageDone.pow(10 + 1) + 1f
    }
}