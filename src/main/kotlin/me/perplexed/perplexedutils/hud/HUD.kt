package me.perplexed.perplexedutils.hud

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import me.perplexed.perplexedutils.gson
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

internal val huds = listOf(
    ArrowHUD(),
    ItemHUD()
)

fun hudById(id: String): HUD { return huds.filter { it.id == id }[0] }

fun registerAndLoadHuds(save: JsonArray) {
    for (hud in save) {
        hudById(hud.asJsonObject["id"]!!.asString).load(hud.asJsonObject)
    }

    for (hud in huds) {
        HudElementRegistry.addFirst(
            Identifier.parse("perplexedutils:${hud.id}_hud"),hud
        )
    }
}


fun saveHuds(): JsonElement {
    val hudRoot = JsonArray()
    for (hud in huds) {
        hudRoot.add(hud.save())
    }
    return hudRoot
}


fun hudConfig(): ConfigCategory {
    val desired = ConfigCategory.createBuilder()
        .name(Component.literal("HUD"))

    for (hud in huds) {
        desired.group(hud.group())
    }

    return desired.build()
}

abstract class HUD(val id: String,var active: Boolean,val pos: DoubleArray,var scale: Float) : HudElement {
    //todo durability

    open fun save():JsonObject {
        return gson.toJsonTree(this).asJsonObject
    }

    open fun load(root: JsonObject) {
        active = root["active"].asBoolean
        pos[0] = root["pos"].asJsonArray[0].asDouble
        pos[1] = root["pos"].asJsonArray[1].asDouble
        scale = root["scale"].asFloat
    }

    fun group(): OptionGroup {
        val desired = OptionGroup.createBuilder()
        desired.name(Component.literal(StringBuilder()
            .append(this.id[0].minus(32))
            .append(this.id.slice(1 until id.length))
            .append(" HUD").toString()))


        desired.option(
            Option.createBuilder<Boolean>()
            .name(Component.literal("Active"))
            .binding(false, { this.active}, { this.active = it})
            .controller{ BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
            .build())

        desired.option(Option.createBuilder<Float>()
            .name(Component.literal("Scale"))
            .binding(1.0f,{this.scale},{this.scale = it})
            .controller{FloatSliderControllerBuilder.create(it)
                .step(0.1f)
                .range(0.1f,5f)}
            .build())

        config(desired)
        return desired.build()
    }

    protected open fun config(group: OptionGroup.Builder) {}

    abstract fun size(): IntArray
}
