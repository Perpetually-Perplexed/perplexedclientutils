package me.perplexed.perplexedutils.util

import net.minecraft.network.chat.Component

fun lit(s: String): Component {
    return Component.literal(s)
}

fun red(s: String): Component {
    return Component.literal(s).withColor(0xc72c2c)
}

fun green(s: String): Component {
    return Component.literal(s).withColor(0x1ae317)
}

fun plain(s: String): Component {
    return Component.literal(s)
}