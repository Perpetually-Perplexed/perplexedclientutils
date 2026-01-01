package me.perplexed.perplexedutils.util

import com.mojang.blaze3d.platform.InputConstants
import me.perplexed.perplexedutils.features.FullBright
import me.perplexed.perplexedutils.features.PeekChat
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import java.util.function.Consumer

val keybinds: MutableList<AdvancedKeyMapping> = mutableListOf()
private val category = KeyMapping.Category.register(Identifier.parse("perplexedutils:keybind"))
private val ksym = InputConstants.Type.KEYSYM

fun registerKeybind(map: AdvancedKeyMapping): KeyMapping {
    KeyBindingHelper.registerKeyBinding(map.mapping)
    keybinds.add(map)
    return map.mapping
}

fun loadKeyBinds() {
    registerKeybind(AdvancedKeyMapping(KeyMapping("Brightness", ksym, InputConstants.KEY_B, category), consume = { FullBright.toggle()}))

    PeekChat.peekChatKey = registerKeybind(AdvancedKeyMapping(KeyMapping("Peek Chat", ksym, InputConstants.KEY_Z, category), release = {
        it.gui.chat.resetChatScroll()
    }))
}

fun keybindListener() {
    ClientTickEvents.END_CLIENT_TICK.register {
        for (keyb in keybinds) keyb.listen()

    }
}


class AdvancedKeyMapping(
    val mapping: KeyMapping,
    private val consume: Consumer<Minecraft> = Consumer {},
    val down: Consumer<Minecraft> = Consumer {},
    val release: Consumer<Minecraft> = Consumer {}
) {

    private var heldLastTick = false

    fun listen() {
        val mc = Minecraft.getInstance()

        if (mapping.consumeClick()) {
            consume.accept(mc)
            heldLastTick = true
            return
        }
        if (mapping.isDown) {
            down.accept(mc)
            heldLastTick = true
            return
        }

        if (heldLastTick) {
            release.accept(mc)
            heldLastTick = false
        }
    }
}