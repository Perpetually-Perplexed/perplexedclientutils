package me.perplexed.perplexedutils.features

import com.google.gson.JsonObject
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import net.minecraft.network.chat.Component
import java.util.function.Function
import kotlin.math.roundToInt


object PotionDisplay {
    var displayType: DisplayType = DisplayType.MINUTE_SECOND
    var active: Boolean = true

    fun loadConfig(root: JsonObject) {
        displayType = DisplayType.valueOf(root["display-type"]?.asString?:"MINUTE_SECOND")
        active = root["active"]?.asBoolean?:true
    }

    fun saveConfig(): JsonObject {
        return JsonObject().apply {
            addProperty("active", active)
            addProperty("display-type", displayType.name)
        }
    }

    fun yacl(): OptionGroup {
        val desired = OptionGroup.createBuilder()

        desired.name(Component.literal("Potion Display"))

        desired.option(
            Option.createBuilder<Boolean>()
                .name(Component.literal("Active"))
                .binding(false, { this.active}, { this.active = it})
                .controller{ BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)}
                .build())

        desired.option(Option.createBuilder<DisplayType>()
            .name(Component.literal("Display Type"))
            .binding(DisplayType.MINUTE_SECOND, {this.displayType}, {this.displayType = it})
            .controller{ num -> EnumControllerBuilder.create(num).enumClass(DisplayType::class.java).formatValue{it.configName}}
            .build())

        return desired.build()
    }
}

enum class DisplayType(val configName: Component, val tick2str: Function<Int, String>) {
    DECIMAL(Component.literal("Decimal Display"), formatter@{ tick ->
        val sec = tick/20
        var min: Double = sec/60+sec%60.0/60
        min = (min*10).roundToInt()/10.0
        return@formatter "${min}m"
    }),
    MINUTE_SECOND(Component.literal("Stopwatch Display"), formatter@{ tick ->
        val sec = tick/20
        return@formatter "${sec / 60}:${if (sec % 60 < 10) "0${sec % 60}" else "${sec % 60}"}"
    });

}