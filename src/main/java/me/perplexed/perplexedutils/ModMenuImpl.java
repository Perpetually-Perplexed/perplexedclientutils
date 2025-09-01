package me.perplexed.perplexedutils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.perplexed.perplexedutils.config.ConfigurationKt;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigurationKt::configScreenInit;
    }
}
