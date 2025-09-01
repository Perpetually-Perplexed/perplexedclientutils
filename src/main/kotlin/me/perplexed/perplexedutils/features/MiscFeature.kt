package me.perplexed.perplexedutils.features

import com.google.gson.JsonObject
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import me.perplexed.perplexedutils.config.saveConfig
import me.perplexed.perplexedutils.util.green
import me.perplexed.perplexedutils.util.red
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component



object PeekChat {
    var peekChatKey: KeyMapping? = null
}

object FullBright {
    private var fullbright = false

    fun active() = fullbright

    internal fun load(root: JsonObject) {
        fullbright = root["fullbright"]?.asBoolean?:false
    }

    internal fun save(desired: JsonObject): JsonObject {
        desired.addProperty("fullbright", fullbright)
        return desired
    }

    internal fun config(thingy: ConfigCategory.Builder) {
        thingy.option(
            Option.createBuilder<Boolean>()
            .name(Component.literal("FullBright"))
            .binding(false, { active() }, { toggle() })
            .controller{ BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
            .build())
    }

    fun toggle() {
        val options = Minecraft.getInstance().options
        fullbright = !fullbright
        if (fullbright) options.gamma().set(16.0) else options.gamma().set(1.0)
        val msg =  if (fullbright) green("FullBright is enabled") else red("FullBright is disabled")
        Minecraft.getInstance().gui.setOverlayMessage(msg,false)
        saveConfig()
    }
}

fun loadMiscConfig(jsonElement: JsonObject) {
    FullBright.load(jsonElement)
}

fun saveMiscConfig(): JsonObject {
    val obj = JsonObject()
    FullBright.save(obj)
    return obj
}

fun miscConfig(yacl: ConfigCategory.Builder) {
    FullBright.config(yacl)
    yacl.group(PotionDisplay.yacl())
    yacl.group(pingDisplayConfig())
}

