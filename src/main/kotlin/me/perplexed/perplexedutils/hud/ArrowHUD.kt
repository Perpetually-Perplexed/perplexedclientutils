package me.perplexed.perplexedutils.hud

import dev.isxander.yacl3.api.ButtonOption
import dev.isxander.yacl3.api.OptionGroup
import me.perplexed.perplexedutils.config.FeatureWidget
import me.perplexed.perplexedutils.config.GuiTypes
import me.perplexed.perplexedutils.config.RepositionScreen
import me.perplexed.perplexedutils.config.UVDim
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ArrowHUD : HUD("arrow",false, doubleArrayOf(0.0,0.0),1f) {

    override fun render(ctxt: GuiGraphics, tracker: DeltaTracker) {
        if (!this.active) return

        val inv = Minecraft.getInstance().player?.inventory ?: return
        val arrow = Items.ARROW
        var counter = 0

        for (item in inv.nonEquipmentItems) {
            if (item.item == arrow) counter += item.count
        }

        val rendyPos = intArrayOf((pos[0]*ctxt.guiWidth()).toInt(), ((pos[1]*ctxt.guiHeight()).toInt()))
        ctxt.pose().pushMatrix()
        ctxt.pose().scale(scale,scale)

        val qkScale: (Int) -> Int = { (1.0/scale * it).toInt() }

        ctxt.renderFakeItem(ItemStack(Items.ARROW), qkScale(rendyPos[0]), qkScale(rendyPos[1]))
        ctxt.pose().popMatrix()

        ctxt.pose().pushMatrix()
        val font = Minecraft.getInstance().font
        ctxt.pose().scale(scale,scale)
        ctxt.pose().translate((17 - font.width("$counter")).toFloat(), 9.0f)
        ctxt.drawString(font, "$counter",
            qkScale(rendyPos[0]),
            qkScale(rendyPos[1]), -1, true)
        ctxt.pose().popMatrix()
    }

    override fun size(): IntArray {
        return intArrayOf(16,16)
    }


    override fun config(group: OptionGroup.Builder) {
        group.option(
            ButtonOption.createBuilder()
                .name(Component.literal("Edit ArrowHUD Position"))
                .text(Component.empty())
                .action{_, _ -> Minecraft.getInstance().setScreen(RepositionScreen(GuiTypes.ARROW))}
                .build())
    }
}

class ArrowHudWidget(screen: Screen, val hud: ArrowHUD)
    : FeatureWidget(screen, UVDim(hud.pos[0],hud.pos[1],16.0/screen.width,16.0/screen.height),hud.scale) {


    override fun save() {
        hud.pos[0] = this.dim.x()
        hud.pos[1] = this.dim.y()
        hud.scale = this.scale()
    }

    override fun reset() {
        this.dim.setX(hud.pos[0]).setY(hud.pos[1])
        this.updateScale(hud.scale)
    }

    override fun default() {
        this.dim.setX(0.0)
            .setY(0.0)
        this.updateScale(1f)
    }

    override fun renderWidgy(ctxt: GuiGraphics) {
        val counter = 42

        val xy = dim.withResolution(ctxt.guiWidth(), ctxt.guiHeight())

        val rendyPos = intArrayOf(xy.x(), xy.y())
        ctxt.pose().pushMatrix()
        ctxt.pose().scale(scale(),scale())

        val qkScale: (Int) -> Int = { (1.0/scale() * it).toInt() }

        ctxt.renderFakeItem(ItemStack(Items.ARROW), qkScale(rendyPos[0]), qkScale(rendyPos[1]))
        ctxt.pose().popMatrix()

        ctxt.pose().pushMatrix()
        val font = Minecraft.getInstance().font
        ctxt.pose().scale(scale(),scale())
        ctxt.pose().translate((17 - font.width("$counter")).toFloat(), 9.0f)
        ctxt.drawString(font, "$counter",
            qkScale(rendyPos[0]),
            qkScale(rendyPos[1]), -1, true)
        ctxt.pose().popMatrix()
    }

}