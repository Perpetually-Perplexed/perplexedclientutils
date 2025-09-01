package me.perplexed.perplexedutils.util

import me.perplexed.perplexedutils.features.tickPing
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

fun registerEvents() {
    keybindListener()
    ClientTickEvents.END_CLIENT_TICK.register{ tickPing() }
}

