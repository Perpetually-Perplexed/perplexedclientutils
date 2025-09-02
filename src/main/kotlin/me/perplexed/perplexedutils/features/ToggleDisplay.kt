package me.perplexed.perplexedutils.features

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.isxander.yacl3.api.ButtonOption
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.ColorControllerBuilder
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import me.perplexed.perplexedutils.gson
import me.perplexed.perplexedutils.config.FeatureWidget
import me.perplexed.perplexedutils.config.GuiTypes
import me.perplexed.perplexedutils.config.RepositionScreen
import me.perplexed.perplexedutils.config.UVDim
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.awt.Color

var toggleDisplay = true
val sprintDisplay = ToggleDisplayElement(TextData("Sprint is enabled", true, 0xffffffff.toInt(), doubleArrayOf(0.0,0.0), 1.0f, true))
val sneakDisplay = ToggleDisplayElement(TextData("Sneak is enabled", true, 0xffffffff.toInt(), doubleArrayOf(0.0,0.0), 1.0f, true))

fun saveToggleDisplay(): JsonObject  {
    val parent = JsonObject()
    parent.addProperty("active", toggleDisplay)
    parent.add("sneak", gson.toJsonTree(sneakDisplay.data).asJsonObject)
    parent.add("sprint", gson.toJsonTree(sprintDisplay.data).asJsonObject)

    return parent
}

fun loadToggleDisplay(data: JsonObject) {
    toggleDisplay = gson.fromJson(data["active"], JsonElement::class.java)?.asBoolean ?: toggleDisplay
    sneakDisplay.data = gson.fromJson(data["sneak"]?.toString(),TextData::class.java) ?: sneakDisplay.data
    sprintDisplay.data = gson.fromJson(data["sprint"]?.toString(),TextData::class.java) ?: sprintDisplay.data

    HudLayerRegistrationCallback.EVENT.register { drawer ->
        drawer.attachLayerAfter(IdentifiedLayer.CHAT,ResourceLocation.parse("perplexedutils:sprint_display")){ c, t -> sprintDisplay.render(c,t)}
    }
    HudLayerRegistrationCallback.EVENT.register { drawer ->
        drawer.attachLayerAfter(IdentifiedLayer.CHAT,ResourceLocation.parse("perplexedutils:sneak_display")){c,t -> sneakDisplay.render(c,t)}
    }
}

fun toggleDisplayConfig(): ConfigCategory {
    val desired = ConfigCategory.createBuilder()
    desired.name(Component.literal("ToggleDisplay"))

    desired.option(Option.createBuilder<Boolean>()
        .name(Component.literal("ToggleDisplay Active"))
        .binding(false, { toggleDisplay }, { toggleDisplay = it})
        .controller{BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())



    //sprint
    val sprint = OptionGroup.createBuilder()
        .name(Component.literal("ToggleSprint"))
    sprint.option(Option.createBuilder<Boolean>()
        .name(Component.literal("Active"))
        .binding(false, { sprintDisplay.data.active}, { sprintDisplay.data.active = it})
        .controller{BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())
    sprint.option(Option.createBuilder<String>()
        .name(Component.literal("Text"))
        .binding("Sprint is enabled", { sprintDisplay.data.text}, { sprintDisplay.data.text = it})
        .controller{StringControllerBuilder.create(it)}
        .build())
    sprint.option(Option.createBuilder<Color>()
        .name(Component.literal("Color"))
        .binding(Color.white, { Color(sprintDisplay.data.color)}, { sprintDisplay.data.color = it.rgb})
        .controller{ColorControllerBuilder.create(it)}
        .build())
    sprint.option(Option.createBuilder<Float>()
        .name(Component.literal("Scale"))
        .binding(1.0f,{ sprintDisplay.data.scale},{sprintDisplay.data.scale = it})
        .controller{
            FloatSliderControllerBuilder.create(it)
            .step(0.1f)
            .range(0.1f,5f)}
        .build())
    sprint.option(Option.createBuilder<Boolean>()
        .name(Component.literal("Shadow"))
        .binding(true, { sprintDisplay.data.shadow}, { sprintDisplay.data.shadow = it})
        .controller{BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())
    sprint.option(
        ButtonOption.createBuilder()
        .name(Component.literal("Edit SprintDisplay Position"))
        .text(Component.empty())
        .action{bacl, _ -> Minecraft.getInstance().setScreen(RepositionScreen(GuiTypes.SPRINT))}
        .build())
    
    desired.group(sprint.build())
    
    //sneak
    val sneak = OptionGroup.createBuilder()
        .name(Component.literal("ToggleSneak"))
    sneak.option(Option.createBuilder<Boolean>()
        .name(Component.literal("Active"))
        .binding(false, { sneakDisplay.data.active}, { sneakDisplay.data.active = it})
        .controller{BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())
    sneak.option(Option.createBuilder<String>()
        .name(Component.literal("Text"))
        .binding("Sneak is enabled", { sneakDisplay.data.text}, { sneakDisplay.data.text = it})
        .controller{StringControllerBuilder.create(it)}
        .build())
    sneak.option(Option.createBuilder<Color>()
        .name(Component.literal("Color"))
        .binding(Color.white, { Color(sneakDisplay.data.color)}, { sneakDisplay.data.color = it.rgb})
        .controller{ColorControllerBuilder.create(it)}
        .build())
    sneak.option(Option.createBuilder<Float>()
        .name(Component.literal("Scale"))
        .binding(1.0f,{ sneakDisplay.data.scale},{ sneakDisplay.data.scale = it})
        .controller{
            FloatSliderControllerBuilder.create(it)
                .step(0.1f)
                .range(0.1f,5f)}
        .build())
    sneak.option(Option.createBuilder<Boolean>()
        .name(Component.literal("Shadow"))
        .binding(true, { sneakDisplay.data.shadow}, { sneakDisplay.data.shadow = it})
        .controller{BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())
    sneak.option(
        ButtonOption.createBuilder()
            .name(Component.literal("Edit SneakDisplay Position"))
            .text(Component.empty())
            .action{bacl, _ -> Minecraft.getInstance().setScreen(RepositionScreen(GuiTypes.SNEAK))}
            .build())

    return desired.group(sneak.build()).build()
}


data class TextData(var text: String,var active: Boolean, var color: Int, var pos: DoubleArray, var scale: Float, var shadow: Boolean) {
    fun pos(ctxt: GuiGraphics): IntArray {
        return intArrayOf((pos[0] * ctxt.guiWidth()/scale).toInt(), (pos[1] * ctxt.guiHeight()/scale).toInt())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextData

        if (text != other.text) return false
        if (active != other.active) return false
        if (color != other.color) return false
        if (!pos.contentEquals(other.pos)) return false
        if (scale != other.scale) return false
        if (shadow != other.shadow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + active.hashCode()
        result = 31 * result + color
        result = 31 * result + pos.contentHashCode()
        result = (31 * result + scale).toInt()
        result = 31 * result + shadow.hashCode()
        return result
    }
}

class ToggleDisplayElement(var data: TextData) {
    fun render(graphics: GuiGraphics, p1: DeltaTracker) {
        if (Minecraft.getInstance().debugOverlay.showDebugScreen() || !toggleDisplay
            || !data.active) return

        val opts = Minecraft.getInstance().options

        if (this == sprintDisplay) {
            if (!opts.keySprint.isDown || !opts.toggleSprint().get())
                return
        } else if (this == sneakDisplay) {
            if (!opts.keyShift.isDown || !opts.toggleCrouch().get())
                return
        }
        
        
        val font = Minecraft.getInstance().font
        val xy = data.pos(graphics)

        graphics.pose().pushPose()

        graphics.pose().scale(data.scale,data.scale,1f)
        graphics.drawString(font, data.text, xy[0], xy[1], data.color,data.shadow)

        graphics.pose().popPose()
    }

}

class ToggleDisplayWidget(screen: Screen, private val textData: TextData)
    : FeatureWidget(screen, UVDim(textData.pos[0],textData.pos[1],
    Minecraft.getInstance().font.width(textData.text).toDouble()/screen.width,
    Minecraft.getInstance().font.lineHeight.toDouble()/screen.height),textData.scale) {


    override fun save() {
        textData.pos[0] = this.dim.x()
        textData.pos[1] = this.dim.y()
        textData.scale = this.scale()
    }

    override fun reset() {
        this.dim.setX(textData.pos[0])
            .setY(textData.pos[1])
        this.updateScale(textData.scale)
    }

    override fun default() {
        this.dim.setX(0.0)
            .setY(0.0)
        this.updateScale(1f)
    }

    override fun renderWidgy(graphics: GuiGraphics) {
        val font = Minecraft.getInstance().font
        val xy = dim.withResolution(graphics.guiWidth(), graphics.guiHeight())

        graphics.pose().pushPose()
        graphics.pose().scale(this.scale(),this.scale(),1f)

        graphics.drawString(font, textData.text, (xy.x()/this.scale()).toInt(), (xy.y()/this.scale()).toInt(), textData.color,textData.shadow)

        graphics.pose().popPose()
    }

}