package com.reco1l.osu.hud.editor

import com.reco1l.andengine.Anchor
import com.reco1l.andengine.Axes
import com.reco1l.andengine.container.ConstraintContainer
import com.reco1l.andengine.container.Container
import com.reco1l.andengine.container.LinearContainer
import com.reco1l.andengine.container.Orientation
import com.reco1l.andengine.shape.Box
import com.reco1l.andengine.shape.RoundedBox
import com.reco1l.andengine.sprite.ExtendedSprite
import com.reco1l.framework.ColorARGB
import com.reco1l.osu.hud.HUDElement
import com.reco1l.osu.updateThread
import org.anddev.andengine.input.touch.TouchEvent
import ru.nsu.ccfit.zuev.osu.Config
import ru.nsu.ccfit.zuev.osu.ResourceManager
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

class HUDElementOverlay(private val element: HUDElement) : ConstraintContainer() {


    private val elementProxy = Box().apply { color = ColorARGB.Transparent }

    private val toolbar = LinearContainer().apply {
        orientation = Orientation.Horizontal
        spacing = 4f

        attachChild(Button("delete", ColorARGB(0xFF260000)) {
            updateThread {
                element.remove()
            }
        })

        // Flip horizontally
        attachChild(Button("flip", ColorARGB(0xFF181825)) {
            element.scaleX = -element.scaleX
        })

        // Flip vertically
        attachChild(Button("flip", ColorARGB(0xFF181825)) {
            element.scaleY = -element.scaleY
        }.apply { icon.rotation = -90f })

    }

    private val scaleTip = Tip("scale") { deltaX, deltaY ->

        val scaleDelta = min(-deltaX, deltaY) / 100f

        element.scaleX = (abs(element.scaleX) + scaleDelta).coerceIn(0.5f, 5f) * sign(element.scaleX)
        element.scaleY = (abs(element.scaleY) + scaleDelta).coerceIn(0.5f, 5f) * sign(element.scaleY)
    }


    init {
        isVisible = false
        setSize(Config.getRES_WIDTH().toFloat(), Config.getRES_HEIGHT().toFloat())

        attachChild(elementProxy)
        attachChild(toolbar)
        attachChild(scaleTip)

        toolbar.anchor = Anchor.TopCenter
        toolbar.origin = Anchor.BottomCenter
        addConstraint(toolbar, elementProxy)

        scaleTip.anchor = Anchor.BottomLeft
        scaleTip.origin = Anchor.BottomLeft
        scaleTip.x = -(TIP_SIZE / 2)
        scaleTip.y = TIP_SIZE / 2
        addConstraint(scaleTip, elementProxy)
    }

    override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {
        if (!isVisible) {
            return false
        }
        return super.onAreaTouched(event, localX, localY)
    }

    override fun onManagedUpdate(pSecondsElapsed: Float) {

        // We need to cancel scale center
        elementProxy.x = element.drawX + (element.drawWidth * element.scaleCenterX) * (1f - abs(element.scaleX))
        elementProxy.y = element.drawY + (element.drawHeight * element.scaleCenterY) * (1f - abs(element.scaleY))

        elementProxy.width = element.drawWidth * abs(element.scaleX)
        elementProxy.height = element.drawHeight * abs(element.scaleY)

        super.onManagedUpdate(pSecondsElapsed)
    }


    private inner class Tip(texture: String, val onMove: (deltaX: Float, deltaY: Float) -> Unit) : Container() {

        init {
            setSize(TIP_SIZE, TIP_SIZE)

            background = RoundedBox().apply {
                cornerRadius = TIP_SIZE / 2
                color = ColorARGB(0xFF181825)
            }

            attachChild(ExtendedSprite().apply {
                textureRegion = ResourceManager.getInstance().getTexture(texture)
                anchor = Anchor.Center
                origin = Anchor.Center
                relativeSizeAxes = Axes.Both
                setSize(0.8f, 0.8f)
            })
        }


        private var initialX = 0f
        private var initialY = 0f

        override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {

            if (event.isActionDown) {
                initialX = localX
                initialY = localY
                return true
            }

            if (event.isActionMove) {
                onMove(localX - initialX, localY - initialY)
                return true
            }

            return false
        }
    }

    private inner class Button(texture: String, back: ColorARGB, val action: () -> Unit) : Container() {

        val icon = ExtendedSprite().apply {
            textureRegion = ResourceManager.getInstance().getTexture(texture)
            anchor = Anchor.Center
            origin = Anchor.Center
            relativeSizeAxes = Axes.Both
            setSize(0.8f, 0.8f)
        }

        init {
            setSize(BUTTON_SIZE, BUTTON_SIZE)

            background = RoundedBox().apply {
                cornerRadius = 12f
                color = back
            }

            attachChild(icon)

        }

        override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {
            if (event.isActionUp) {
                action()
                return true
            }

            if (event.isActionDown) {
                return true
            }

            return false
        }
    }


    companion object {
        private const val TIP_SIZE = 32f
        private const val BUTTON_SIZE = 46f
    }

}