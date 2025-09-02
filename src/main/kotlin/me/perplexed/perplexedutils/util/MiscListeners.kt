package me.perplexed.perplexedutils.util

import me.perplexed.perplexedutils.features.tickPing
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback

fun registerEvents() {
    keybindListener()
    ClientTickEvents.END_CLIENT_TICK.register{ tickPing() }
}

