package me.perplexed.perplexedutils.hud

import com.google.gson.JsonObject
import dev.isxander.yacl3.api.ButtonOption
import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.controller.ItemControllerBuilder
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.dropdown.ItemController
import dev.isxander.yacl3.gui.controllers.dropdown.ItemControllerElement
import dev.isxander.yacl3.gui.utils.ItemRegistryHelper
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl
import me.perplexed.perplexedutils.gson
import me.perplexed.perplexedutils.config.FeatureWidget
import me.perplexed.perplexedutils.config.GuiTypes
import me.perplexed.perplexedutils.config.RepositionScreen
import me.perplexed.perplexedutils.config.UVDim
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items


class ItemHUD(var pinned: Item = Items.AIR) : HUD("item",false, doubleArrayOf(0.0,0.0),1f) {

    override fun render(ctxt: GuiGraphics, tracker: DeltaTracker) {
        if (!this.active) return

        val inv = Minecraft.getInstance().player?.inventory ?: return
        var counter = 0
        val look: Item = if (pinned != Items.AIR) {
            pinned
        } else {
            if (!inv.getSelected().isEmpty) inv.getSelected().item
            else if (!inv.getItem(Inventory.SLOT_OFFHAND).isEmpty) inv.getItem(Inventory.SLOT_OFFHAND).item
            else return
        }

        for (item in inv.items) {
            if (item.item == look) counter += item.count
        }
        if (inv.getItem(Inventory.SLOT_OFFHAND).item == look) counter+=inv.getItem(Inventory.SLOT_OFFHAND).count

        val rendyPos = intArrayOf((pos[0]*ctxt.guiWidth()).toInt(), ((pos[1]*ctxt.guiHeight()).toInt()))
        ctxt.pose().pushPose()
        ctxt.pose().scale(scale,scale, 1f)

        val qkScale: (Int) -> Int = { (1.0/scale * it).toInt() }

        ctxt.renderFakeItem(ItemStack(look), qkScale(rendyPos[0]), qkScale(rendyPos[1]))
        ctxt.pose().popPose()

        ctxt.pose().pushPose()
        val font = Minecraft.getInstance().font
        ctxt.pose().scale(scale,scale, 1f)
        ctxt.pose().translate((17 - font.width("$counter")).toDouble(), 9.0, 200.0)
        ctxt.drawString(font, "$counter",
            qkScale(rendyPos[0]),
            qkScale(rendyPos[1]), -1, true)
        ctxt.pose().popPose()

    }


    override fun config(group: OptionGroup.Builder) {
        group.option(Option.createBuilder<Item>()
            .name(Component.literal("Pinned"))
            .binding(Items.AIR,{this.pinned}, {this.pinned = it})
            .controller{PinnedItemControllerBuilder(it)}
            .build())
        group.option(
            ButtonOption.createBuilder()
                .name(Component.literal("Edit ItemHUD Position"))
                .text(Component.empty())
                .action{bacl, _ -> Minecraft.getInstance().setScreen(RepositionScreen(GuiTypes.ITEM))}
                .build())
    }

    override fun size(): IntArray {
        return intArrayOf(16,16)
    }

    companion object {
        private const val delta = 7
    }

    override fun save(): JsonObject {
        val item = JsonObject();
        item.addProperty("id",this.id)
        item.addProperty("active",this.active)
        item.add("pos", gson.toJsonTree(pos).asJsonArray)
        item.addProperty("scale",this.scale);
        item.addProperty("pinned",BuiltInRegistries.ITEM.wrapAsHolder(this.pinned).registeredName)
        return item
    }

    override fun load(root: JsonObject) {
        this.active = root["active"].asBoolean
        this.pos[0] = root["pos"].asJsonArray[0].asDouble
        this.pos[1] = root["pos"].asJsonArray[1].asDouble
        this.scale = root["scale"].asFloat
        this.pinned = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(root["pinned"].asString))
    }
}

class PinnedItemController(option: Option<Item?>) : ItemController(option) {
    override fun formatValue(): Component {
        if (option.pendingValue() == Items.AIR) return Component.literal("Nothing here!")
        return super.formatValue()
    }

    override fun setFromString(value: String?) {
        if (value?.lowercase() == "none") return option().requestSet(Items.AIR)
        return option.requestSet(ItemRegistryHelper.getItemFromName(value, option.pendingValue()))
    }

    override fun isValueValid(value: String?): Boolean {
        return ItemRegistryHelper.isRegisteredItem(value) || aliases.contains(value?.lowercase())
    }

    override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>): AbstractWidget {
        return PinnedItemControllerElement(this, screen, widgetDimension)
    }

    companion object {
        private val aliases = listOf("none","nothing")
    }
}

class PinnedItemControllerElement(val contr: PinnedItemController?, screen: YACLScreen, dims: Dimension<Int>) :
    ItemControllerElement(contr, screen, dims) {

    override fun getValueText(): Component {
        if (this.contr == null || this.contr.option().pendingValue() == Items.AIR) return Component.literal("Nothing Here!")
        return super.getValueText()
    }

}

class PinnedItemControllerBuilder(option: Option<Item?>) : AbstractControllerBuilderImpl<Item?>(option), ItemControllerBuilder {
    override fun build(): Controller<Item?> {
        return PinnedItemController(option)
    }
}


class ItemHudWidget(screen: Screen,val hud: ItemHUD)
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

    override fun renderWidgy(ctxt: GuiGraphics) {
        val counter = 42

        val look: Item = if (hud.pinned != Items.AIR) {
            hud.pinned
        } else {
            Items.EXPERIENCE_BOTTLE
        }

        val xy = dim.withResolution(ctxt.guiWidth(), ctxt.guiHeight())

        val rendyPos = intArrayOf(xy.x(), xy.y())
        ctxt.pose().pushPose()
        ctxt.pose().scale(scale(),scale(), 1f)

        val qkScale: (Int) -> Int = { (1.0/scale() * it).toInt() }

        ctxt.renderFakeItem(ItemStack(look), qkScale(rendyPos[0]), qkScale(rendyPos[1]))
        ctxt.pose().popPose()

        ctxt.pose().pushPose()
        val font = Minecraft.getInstance().font
        ctxt.pose().scale(scale(),scale(),1f)
        ctxt.pose().translate((17 - font.width("$counter")).toDouble(), 9.0, 200.0)
        ctxt.drawString(font, "$counter",
            qkScale(rendyPos[0]),
            qkScale(rendyPos[1]), -1, true)
        ctxt.pose().popPose()
    }

    override fun default() {
        this.dim.setX(0.0)
            .setY(0.0)
        this.updateScale(1f)
    }

}
