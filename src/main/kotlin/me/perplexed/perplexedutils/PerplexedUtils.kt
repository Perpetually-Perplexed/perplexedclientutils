package me.perplexed.perplexedutils

import com.google.gson.GsonBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

val gson = GsonBuilder().setPrettyPrinting().create()

class PerplexedUtilsMain : ModInitializer {
    override fun onInitialize() {


    }
}
