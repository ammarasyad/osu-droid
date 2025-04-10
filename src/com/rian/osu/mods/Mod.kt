package com.rian.osu.mods

import com.rian.osu.beatmap.sections.BeatmapDifficulty
import kotlin.reflect.KClass

/**
 * Represents a mod.
 */
abstract class Mod {
    /**
     * Whether scores with this [Mod] active can be submitted online.
     */
    open val isRanked = false

    /**
     * Whether adding this [Mod] will affect gameplay.
     */
    open val isRelevant = true

    /**
     * Whether this [Mod] is valid for multiplayer matches.
     *
     * Should be `false` for [Mod]s that make gameplay duration different across players.
     */
    open val isValidForMultiplayer = true

    /**
     * Whether this [Mod] is valid as a free mod in multiplayer matches.
     *
     * Should be `false` for [Mod]s that affect gameplay duration (e.g., [ModRateAdjust]).
     */
    open val isValidForMultiplayerAsFreeMod = true

    /**
     * The [Mod]s this [Mod] cannot be enabled with.
     */
    open val incompatibleMods = emptyArray<KClass<out Mod>>()

    /**
     * Calculates the score multiplier for this [Mod] with the given [BeatmapDifficulty].
     *
     * @param difficulty The [BeatmapDifficulty] to calculate the score multiplier for.
     * @return The score multiplier for this [Mod] with the given [BeatmapDifficulty].
     */
    open fun calculateScoreMultiplier(difficulty: BeatmapDifficulty) = 1f

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other !is Mod) {
            return false
        }

        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = isRanked.hashCode()

        result = 31 * result + isRelevant.hashCode()
        result = 31 * result + isValidForMultiplayer.hashCode()
        result = 31 * result + isValidForMultiplayerAsFreeMod.hashCode()
        result = 31 * result + incompatibleMods.contentHashCode()

        if (this is IModUserSelectable) {
            result = 31 * result + encodeChar.hashCode()
            result = 31 * result + acronym.hashCode()
            result = 31 * result + textureNameSuffix.hashCode()
            result = 31 * result + enum.hashCode()
        }

        return result
    }

    /**
     * Creates a deep copy of this [Mod].
     *
     * @return A deep copy of this [Mod].
     */
    abstract fun deepCopy(): Mod
}