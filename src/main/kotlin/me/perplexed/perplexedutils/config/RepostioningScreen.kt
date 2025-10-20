package me.perplexed.perplexedutils.config

import com.mojang.blaze3d.platform.InputConstants
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.api.utils.MutableDimension
import dev.isxander.yacl3.gui.utils.GuiUtils
import dev.isxander.yacl3.gui.utils.YACLRenderHelper
import dev.isxander.yacl3.impl.utils.DimensionIntegerImpl
import me.perplexed.perplexedutils.features.PingDisplayWidget
import me.perplexed.perplexedutils.features.ToggleDisplayWidget
import me.perplexed.perplexedutils.hud.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import java.util.function.Consumer
import java.util.function.Function

val client = Minecraft.getInstance()
val font = Minecraft.getInstance().font

class RepositionScreen(private var type: GuiTypes = GuiTypes.SPRINT) : Screen(Component.literal("Repositioning!")) {

    private var featureIndex = 0
    private var edited = false
    private var featureWidget: FeatureWidget? = null


    private var saveButton: Button? = null
    private var resetButton: Button? = null
    private var featureButton: Button? = null
    private var doneButton: Button? = null
    private var defaultButton: Button? = null
    private var scaleSlider: ScaleSliderElement? = null

    //todo add sprite
    //todo reset and save confirmation
    override fun init() {
        val y: Int = (0.85*super.height).toInt()
        val buttonHeight: Int = (0.065*super.height).toInt()
        val buttonWidth: Int = (0.3*super.width).toInt()
        val xGap: Int= (0.05*super.width).toInt()
        val yGap: Int = (0.02*super.height).toInt()
        featureWidget = type.provideWidget(this)

        scaleSlider = ScaleSliderElement(DimensionIntegerImpl((this.width/2 + 1.0/4*buttonWidth + xGap).toInt(),y,buttonWidth,buttonHeight),
            Component.literal("Scale"), featureWidget!!.scale(),0.1,5.0,0.1) {featureWidget!!.updateScale(it)}

        val trioStart = this.width/2 + 1.0/4*buttonWidth + xGap

        defaultButton = Button.builder(Component.literal("Default")) {
            featureWidget!!.default()
            featureWidget!!.save()
            newNormal()
        }.bounds(trioStart.toInt(),y+buttonHeight+yGap,buttonWidth/4,buttonHeight).build()


        resetButton = Button.builder(Component.literal("Reset")) { this.reset() }
            .bounds((trioStart+3.0/8*buttonWidth).toInt(),y+buttonHeight+yGap,buttonWidth/4,buttonHeight).build()

        saveButton = Button.builder(Component.literal("Save")) {
            featureWidget!!.save()
            this.reset()
        }.bounds((trioStart + 3.0/4 * buttonWidth).toInt(),y+buttonHeight+yGap,buttonWidth/4,buttonHeight).build()

        featureButton = Button.builder(Component.literal(type.pretty)) { nextType() }
            .bounds((this.width/2 - 5.0/4*buttonWidth - xGap).toInt(), y,buttonWidth,buttonHeight).build()

        doneButton = Button.builder(Component.literal("Done")) {
            if (edited) {
                it.message = Component.literal("Save first!").withColor(0xFF0000)
                return@builder
            }
            onClose()
        }.bounds( this.width/2 - buttonWidth/4, y,buttonWidth/2,buttonHeight).build()


        if (!edited) {
            saveButton?.active = false
            resetButton?.active = false
        }
        this.addRenderableWidget(featureButton!!)
        this.addRenderableWidget(doneButton!!)
        this.addRenderableWidget(scaleSlider!!)
        this.addRenderableWidget(saveButton!!)
        this.addRenderableWidget(defaultButton!!)
        this.addRenderableWidget(resetButton!!)
        this.addRenderableWidget(featureWidget!!)
    }


    override fun onClose() {
        saveConfig()
        client.setScreen(configScreenInit(null))
    }

    private fun nextType() {
        if (edited) {
            featureButton?.message = Component.literal("Save before moving on!").withColor(0xFF0000)
            return
        }

        type = GuiTypes.entries[(++featureIndex)% GuiTypes.entries.size]
        this.removeWidget(featureWidget!!)
        featureWidget = type.provideWidget(this)
        reset()
        featureButton?.message = Component.literal(type.pretty)
        this.addRenderableWidget(featureWidget!!)
    }


    private fun reset() {
        this.featureWidget!!.reset()
        newNormal()
    }

    private fun newNormal() {
        doneButton?.message = Component.literal("Done")
        featureButton?.message = Component.literal(type.pretty)
        this.scaleSlider?.value = featureWidget!!.scale()
        this.saveButton?.active = false
        this.resetButton?.active = false
        this.edited = false
    }

    fun markEdits() {
        if (edited) return
        edited = true
        this.saveButton?.active = true
        this.resetButton?.active = true
    }
}

class ScaleSliderElement(
    val dim: Dimension<Int>,
    message: Component,
    var value: Float,
    private val min: Double,
    private val max: Double,
    private val interval: Double,
    private val task: Consumer<Float>
) : AbstractWidget(dim.x(),dim.y(),dim.width(),dim.height(),message) {

    private var sliderBounds: Dimension<Int> = dim
        .withWidth((dim.width()*0.7).toInt())
        .withHeight((dim.height()*0.9).toInt())
        .withX(dim.x() + (dim.width()*0.2).toInt())

    private var mouseDown = false
    private var focused = false

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val shortenedName: Component = Component.literal(
            GuiUtils.shortenString(
                super.getMessage().string,
                font,
                this.dim.width() as Int -
                        if (super.isHovered()) sliderBounds.width() + font.width(super.getMessage()) + 10 / 2
                        else - 12, "..."
            )
        ).setStyle(super.getMessage().style)
        YACLRenderHelper.renderButtonTexture(guiGraphics, dim.x(), dim.y(), width, height, true, isHovered())
        val txtY = dim.y().toFloat() + dim.height().toFloat() / 2.0f - 4.5f
        if (!this.isHovered())
            guiGraphics.drawString(font, shortenedName, this.dim.x() + 5, txtY.toInt(), -0x1, true)
        this.drawValueText(guiGraphics)
        if (this.isHovered()) {
            this.drawHoveredControl(guiGraphics)
        }
    }

    private fun drawHoveredControl(graphics: GuiGraphics) {
        graphics.fill(
            sliderBounds.x(),
            sliderBounds.centerY() - 1,
            sliderBounds.xLimit(),
            sliderBounds.centerY(), -1
        )
        graphics.fill(
            sliderBounds.x() + 1,
            sliderBounds.centerY(),
            sliderBounds.xLimit() + 1,
            sliderBounds.centerY() + 1, -12566464
        )
        graphics.fill(
            this@ScaleSliderElement.thumbX - this@ScaleSliderElement.thumbWidth / 2 + 1,
            sliderBounds.y() + 1,
            thumbX + this@ScaleSliderElement.thumbWidth / 2 + 1,
            sliderBounds.yLimit() + 1, -12566464
        )
        graphics.fill(
            this@ScaleSliderElement.thumbX - this@ScaleSliderElement.thumbWidth / 2,
            sliderBounds.y(),
            thumbX + this@ScaleSliderElement.thumbWidth / 2,
            sliderBounds.yLimit(), -1
        )
    }

    private fun drawValueText(graphics: GuiGraphics) {
        graphics.pose().pushMatrix()
        if (super.isHovered()) {
            graphics.pose()
                .translate(-((sliderBounds.width() + 6).toFloat() + thumbWidth.toFloat() / 2.0f), 0.0f)
        }

        val txtY = (dim.y() as Int).toFloat() + (dim.height() as Int).toFloat() / 2.0f - 4.5f

        graphics.drawString(
            font, "${this.value}",
            this.dim.xLimit() - font.width(super.getMessage()) - 5,
            txtY.toInt(),
            -0x1, true)
        graphics.pose().popMatrix()
    }

    override fun mouseClicked(event: MouseButtonEvent, bl:Boolean): Boolean {
        if (event.button() == 0 && sliderBounds.isPointInside(event.x.toInt(), event.y.toInt())) {
            this.mouseDown = true
            this.setValueFromMouse(event.x)
            return true
        } else {
            return false
        }
    }

    override fun mouseDragged(event: MouseButtonEvent , deltaX: Double, deltaY: Double): Boolean {
        if (event.button() == 0 && this.mouseDown) {
            this.setValueFromMouse(event.x)
            return true
        } else {
            return false
        }
    }

    private fun incrementValue(amount: Double) {
        updateValue(this.value + (amount*this.interval).toFloat())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontal: Double, vertical: Double): Boolean {
        val window = Minecraft.getInstance().window
        val shift = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
        val ctrl = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL)

        if (this.isMouseOver(mouseX, mouseY) && (shift || ctrl)) {
            this.incrementValue(vertical)
            return true
        } else {
            return false
        }
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        if (this.mouseDown) {
            client.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }

        this.mouseDown = false
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (!this.focused) return true

        when (keyEvent.scancode) {
            262 -> this.incrementValue(1.0)
            263 -> this.incrementValue(-1.0)
            else -> return false
        }

        return true
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return super.isMouseOver(mouseX, mouseY) || this.mouseDown
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.USAGE,"$value")
    }

    private fun setValueFromMouse(mouseX: Double) {
        val new = (mouseX - sliderBounds.x())/sliderBounds.width() * this.max
        updateValue(roundToInterval(new).toFloat())
    }

    private fun roundToInterval(value: Double): Double {
        return Mth.clamp(this.min + this.interval * Math.round(value / this.interval), this.min, this.max)
    }

    private fun updateValue(value: Float) {
        this.value = value
        task.accept(this.value)
    }
    val thumbX: Int
        get() = (sliderBounds.x().toFloat() + (sliderBounds.width().toFloat()) * this.value/this.max).toInt()

    val thumbWidth: Int
        get() = 4
}

//todo fix outline when hovering, fix dragging when scaled
abstract class FeatureWidget(val screen: Screen, val ogDim: UVDim, private var scale: Float, message: Component = Component.literal("bla"))
    : AbstractWidget(0, 0, 0, 0, message) {
    
    var dim: UVDim = ogDim
    init {
        dim = ogDim.withWidth(scale.toDouble()*ogDim.width())
            .withHeight(scale.toDouble()*ogDim.height()).withX(dim.x()).withY(dim.y()) as UVDim
    }
    
    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.dim.setX(dim.x().coerceAtMost(1.0-dim.width()  ))
        this.dim.setY(dim.y().coerceAtMost(1.0-dim.height()))

        val resolved = dim.withResolution(guiGraphics.guiWidth(), guiGraphics.guiHeight())
        this.x = resolved.x()
        this.y = resolved.y()
        this.width = resolved.width()
        this.height = resolved.height()


        renderWidgy(guiGraphics)
        guiGraphics.submitOutline(resolved.x()-1,resolved.y()-1,resolved.width()+1,resolved.height()+1,
            0xff6be2f2.toInt())
    }

    open fun updateScale(scale: Float) {
        this.scale = scale
        (screen as RepositionScreen).markEdits()
        dim = ogDim.withWidth(scale.toDouble()*ogDim.width())
            .withHeight(scale.toDouble()*ogDim.height()).withX(dim.x()).withY(dim.y()) as UVDim
    }

    override fun onDrag(event: MouseButtonEvent, dragX: Double,  dragY: Double) {
        (screen as RepositionScreen).markEdits()
        dim = dim.withX(event.x / screen.width).withY(event.y/screen.height) as UVDim
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.HINT,this.message)
    }

    fun scale(): Float = scale

    abstract fun save()
    abstract fun reset()
    abstract fun default()
    abstract fun renderWidgy(guiGraphics: GuiGraphics)

}

class UVDim(private var x: Double, private var y: Double, private var width: Double, private var height: Double)
    : MutableDimension<Double> {

    override fun x(): Double = x
    override fun y(): Double = y
    override fun width(): Double = width
    override fun height(): Double = height

    override fun xLimit(): Double = x + width

    override fun yLimit(): Double = y + height

    override fun centerX(): Double = x/2

    override fun centerY(): Double = y/2

    override fun clone(): MutableDimension<Double> = UVDim(x,y,width,height)

    override fun expand(width: Double, height: Double): MutableDimension<Double> {
        this.height += height
        this.width += width
        return this
    }

    override fun move(x: Double, y: Double): MutableDimension<Double> {
        this.x += x
        this.y += y
        return this
    }

    override fun setHeight(height: Double): MutableDimension<Double> {
        this.height = height
        return this
    }

    override fun setWidth(width: Double): MutableDimension<Double> {
        this.width = width
        return this
    }

    override fun setY(y: Double): MutableDimension<Double> {
        this.y = y
        return this
    }

    override fun setX(x: Double): MutableDimension<Double> {
        this.x = x
        return this
    }

    override fun expanded(width: Double, height: Double): Dimension<Double> = this.clone().expand(height, width)

    override fun moved(x: Double, y: Double): Dimension<Double> = this.clone().move(x,y)

    override fun withHeight(height: Double): Dimension<Double> = this.clone().setHeight(height)

    override fun withWidth(width: Double): Dimension<Double>  = this.clone().setWidth(width)

    override fun withY(y: Double): Dimension<Double> = this.clone().setY(y)

    override fun withX(x: Double): Dimension<Double>  = this.clone().setX(x)

    override fun isPointInside(x: Double, y: Double): Boolean {
        return x in x()..xLimit() && y in y()..yLimit()
    }
    
    fun withResolution(screenWidth: Int,screenHeight: Int): Dimension<Int> {
        return DimensionIntegerImpl((x*screenWidth).toInt(), (y*screenHeight).toInt(),
            (width*screenWidth).toInt(), (height*screenHeight).toInt()
        )   
    }

}

enum class GuiTypes(val pretty: String, val widgetSupplier: Function<Screen, FeatureWidget>) {
    SPRINT("SprintDisplay",{ToggleDisplayWidget(it, me.perplexed.perplexedutils.features.sprintDisplay.data)}),
    SNEAK("SneakDisplay",{ToggleDisplayWidget(it, me.perplexed.perplexedutils.features.sneakDisplay.data)}),
    ITEM("ItemHUD",{ItemHudWidget(it, hudById("item") as ItemHUD)}),
    ARROW("ArrowHUD",{ArrowHudWidget(it, hudById("arrow") as ArrowHUD)}),
    PING("PingDisplay",{PingDisplayWidget(it, me.perplexed.perplexedutils.features.pingDisplay.data)});

    /*
    GYATT("Gyatt", {TestWidget1(it,TextData("ball",true,-1, doubleArrayOf(0.0,0.0),1f,true))}),
    SIMAG("Simag", {TestWidget2(it, doubleArrayOf(0.0,0.0),
        TestWidget2.size[1].toDouble()/it.height,TestWidget2.size[0].toDouble()/it.width,1f)});
     */


    fun provideWidget(screen: Screen): FeatureWidget = widgetSupplier.apply(screen)
}