package me.perplexed.perplexedutils.features

import com.google.gson.JsonObject
import dev.isxander.yacl3.api.ButtonOption
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import me.perplexed.perplexedutils.config.FeatureWidget
import me.perplexed.perplexedutils.config.GuiTypes
import me.perplexed.perplexedutils.config.RepositionScreen
import me.perplexed.perplexedutils.config.UVDim
import me.perplexed.perplexedutils.gson
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.Util
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
import net.minecraft.resources.ResourceLocation


var ping: Long = 0
private var ticks = 0
val pingDisplay = PingElement(PingGyaan(false, doubleArrayOf(0.0,0.0),1f,true))

fun savePingDisplay(): JsonObject {
    return gson.toJsonTree(pingDisplay.data).asJsonObject
}

fun tickPing() {
    if (pingDisplay.data.active && ticks++ % 5 == 0)
        Minecraft.getInstance().connection?.send(ServerboundPingRequestPacket(Util.getMillis()))
}

fun loadPingDisplay(data: JsonObject) {
    pingDisplay.data = gson.fromJson(data["ping-display"]?.toString(),PingGyaan::class.java) ?: pingDisplay.data
    HudElementRegistry.addFirst(ResourceLocation.parse("perplexedutils:pingdisplay"), pingDisplay)
}

fun pingDisplayConfig(): OptionGroup {
    val ping = OptionGroup.createBuilder()
        .name(Component.literal("PingDisplay"))
    ping.option(
        Option.createBuilder<Boolean>()
        .name(Component.literal("Active"))
        .binding(false, { pingDisplay.data.active}, { pingDisplay.data.active = it})
        .controller{ BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())
    ping.option(
        Option.createBuilder<Float>()
        .name(Component.literal("Scale"))
        .binding(1.0f,{ pingDisplay.data.scale},{pingDisplay.data.scale = it})
        .controller{
            FloatSliderControllerBuilder.create(it)
                .step(0.1f)
                .range(0.1f,5f)}
        .build())
    ping.option(
        Option.createBuilder<Boolean>()
        .name(Component.literal("Shadow"))
        .binding(true, { pingDisplay.data.shadow}, { pingDisplay.data.shadow = it})
        .controller{ BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
        .build())
    ping.option(
        ButtonOption.createBuilder()
            .name(Component.literal("Edit Ping Display Position"))
            .text(Component.empty())
            .action{bacl, _ -> Minecraft.getInstance().setScreen(RepositionScreen(GuiTypes.PING))}
            .build())

    return ping.build()
}

class PingElement(var data: PingGyaan) : HudElement {
    private var text = "0 ms"
    private var color = 0xffffffff.toInt()

    override fun render(graphics: GuiGraphics, p1: DeltaTracker?) {
        if (!data.active) return

        updatePing()
        val font = Minecraft.getInstance().font
        val xy = data.pos(graphics)

        graphics.pose().pushMatrix()

        graphics.pose().scale(data.scale,data.scale)
        graphics.drawString(font, text, xy[0], xy[1], color ,data.shadow)

        graphics.pose().popMatrix()
    }

    private fun updatePing() {
        text = "$ping ms"
        color = when (ping) {
            in 0 until 50 -> 0xff428f03
            in 50 until 100 -> 0xff11d14b
            in 100 until 200 -> 0xffedda2f
            in 200 until 300 -> 0xffed3f2f
            else -> 0xff730b01
        }.toInt()
    }

}

class PingDisplayWidget(screen: Screen, private val ping: PingGyaan)
    : FeatureWidget(screen, UVDim(ping.pos[0], ping.pos[1],
    Minecraft.getInstance().font.width("69 ms").toDouble()/screen.width,
    Minecraft.getInstance().font.lineHeight.toDouble()/screen.height),ping.scale) {


    override fun save() {
        ping.pos[0] = this.dim.x()
        ping.pos[1] = this.dim.y()
        ping.scale = this.scale()
    }

    override fun reset() {
        this.dim.setX(ping.pos[0])
            .setY(ping.pos[1])
        this.updateScale(ping.scale)
    }

    override fun default() {
        this.dim.setX(0.0)
            .setY(0.0)
        this.updateScale(1f)
    }

    override fun renderWidgy(graphics: GuiGraphics) {
        val font = Minecraft.getInstance().font
        val xy = dim.withResolution(graphics.guiWidth(), graphics.guiHeight())

        graphics.pose().pushMatrix()
        graphics.pose().scale(this.scale(),this.scale())

        graphics.drawString(font, "69 ms", (xy.x()/this.scale()).toInt(), (xy.y()/this.scale()).toInt(), 0xff11d14b.toInt(),ping.shadow)

        graphics.pose().popMatrix()
    }

}

data class PingGyaan(var active: Boolean, var pos: DoubleArray, var scale: Float, var shadow: Boolean) {
    fun pos(ctxt: GuiGraphics): IntArray {
        return intArrayOf((pos[0] * ctxt.guiWidth()/scale).toInt(), (pos[1] * ctxt.guiHeight()/scale).toInt())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PingGyaan

        if (active != other.active) return false
        if (!pos.contentEquals(other.pos)) return false
        if (scale != other.scale) return false
        if (shadow != other.shadow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = active.hashCode()
        result = 31 * result + pos.contentHashCode()
        result = 31 * result + scale.hashCode()
        result = 31 * result + shadow.hashCode()
        return result
    }
}