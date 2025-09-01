package me.perplexed.perplexedutils.client

import me.perplexed.perplexedutils.config.loadSaveFile
import me.perplexed.perplexedutils.features.FullBright
import me.perplexed.perplexedutils.util.loadKeyBinds
import me.perplexed.perplexedutils.util.registerEvents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException

class PerplexedUtilsClient : ClientModInitializer {

    override fun onInitializeClient() {
        loadKeyBinds()
        registerEvents()
        try {
            loadSaveFile(String(Files.readAllBytes(FabricLoader.getInstance().configDir.resolve("perplexedutils.json"))))
        } catch (e: NoSuchFileException) {
            File(FabricLoader.getInstance().configDir.resolve("perplexedutils.json").toUri()).createNewFile()
        }

        ClientLifecycleEvents.CLIENT_STARTED.register{
            if (FullBright.active()) {
                Minecraft.getInstance().options.gamma().set(16.0)
            }
        }

    }
}
