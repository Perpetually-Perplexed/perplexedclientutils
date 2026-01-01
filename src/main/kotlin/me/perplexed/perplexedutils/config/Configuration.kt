package me.perplexed.perplexedutils.config

import com.google.gson.JsonObject
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.YetAnotherConfigLib
import me.perplexed.perplexedutils.features.*
import me.perplexed.perplexedutils.gson
import me.perplexed.perplexedutils.hud.hudConfig
import me.perplexed.perplexedutils.hud.registerAndLoadHuds
import me.perplexed.perplexedutils.hud.saveHuds
import me.perplexed.perplexedutils.util.lit
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.io.File


fun configScreenInit(parent: Screen? = null): Screen {
    val yacl = YetAnotherConfigLib.createBuilder()
    yacl.title(lit("PerplexedUtils Configuration"))

    //toggle display
    yacl.category(toggleDisplayConfig())

    //huds
    yacl.category(hudConfig())

    //misc
    val misc = ConfigCategory.createBuilder().name(Component.literal("Miscellaneous"))
    miscConfig(misc)
    yacl.category(misc.build())

    yacl.save { saveConfig() }
    return yacl.build().generateScreen(parent)
}


fun loadSaveFile(data: String) {
    val asJson = gson.fromJson(data,JsonObject::class.java) ?: return
    loadMiscConfig(asJson["misc"].asJsonObject)
    loadPingDisplay(asJson["ping-display"].asJsonObject)
    PotionDisplay.loadConfig(asJson)
    loadToggleDisplay(asJson["toggle-display"].asJsonObject)
    registerAndLoadHuds(asJson["huds"].asJsonArray)
}

fun saveConfig() {
    val json = JsonObject()
    json.add("misc", saveMiscConfig())
    json.add("potion-display",PotionDisplay.saveConfig())
    json.add("toggle-display", saveToggleDisplay())
    json.add("huds", saveHuds())
    json.add("ping-display", savePingDisplay())

    File(FabricLoader.getInstance().configDir.resolve("perplexedutils.json").toUri()).writeText(gson.toJson(json))
}