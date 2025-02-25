package com.reco1l.osu.hud.elements

import com.reco1l.osu.hud.HUDElement
import com.reco1l.osu.hud.HUDElementSkinData
import com.reco1l.osu.playfield.SpriteFont
import ru.nsu.ccfit.zuev.osu.game.GameScene
import ru.nsu.ccfit.zuev.osu.scoring.StatisticV2
import ru.nsu.ccfit.zuev.skins.OsuSkin
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class HUDAccuracyCounter : HUDElement() {

    private val sprite = SpriteFont(OsuSkin.get().scorePrefix)
    private val format = DecimalFormat("0.00%", DecimalFormatSymbols(Locale.US))

    init {
        sprite.text = "100.00%"
        attachChild(sprite)
        onMeasureContentSize()
    }

    override fun setSkinData(data: HUDElementSkinData?) {
        super.setSkinData(data)

        // Some skins use a wider % character, so we need to adjust the width of the counter so it
        // doesn't become too wide for the editor.
        width = sprite.characters['0']!!.width * 7f
        height = sprite.characters['0']!!.height.toFloat()
    }

    override fun onGameplayUpdate(gameScene: GameScene, statistics: StatisticV2, secondsElapsed: Float) {
        sprite.text = format.format(statistics.accuracy)
    }

}