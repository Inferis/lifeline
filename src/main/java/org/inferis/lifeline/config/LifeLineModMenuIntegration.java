package org.inferis.lifeline.config;

import org.inferis.lifeline.LifeLine;
import org.inferis.lifeline.config.LifeLineConfig.DisplayCondition;
import org.inferis.lifeline.config.LifeLineConfig.DisplayMode;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LifeLineModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> configScreen(parent);
    }

    public Screen configScreen(Screen parentScreen) {
        var config = LifeLine.CONFIG;

        var builder = ConfigBuilder.create()
            .setParentScreen(parentScreen)
            .setTitle(Text.translatable("lifeline.config.title"));

        var entryBuilder = builder.entryBuilder();

        var category = builder.getOrCreateCategory(Text.translatable("lifeline.config.common"));
        category
            .addEntry(entryBuilder.startBooleanToggle(Text.translatable("lifeline.config.rendering_enabled"), config.renderingEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> { config.renderingEnabled = value; config.save(); })
                .build())
            .addEntry(entryBuilder.startEnumSelector(Text.translatable("lifeline.config.display_mode"), DisplayMode.class, config.displayMode)
                .setDefaultValue(DisplayMode.HEARTS)
                .setSaveConsumer(value -> { config.displayMode = value; config.save(); })
                .build())
            .addEntry(entryBuilder.startEnumSelector(Text.translatable("lifeline.config.display_condition"), DisplayCondition.class, config.displayCondition)
                .setTooltip(Text.translatable("lifeline.config.display_condition.tooltip"))
                .setDefaultValue(DisplayCondition.DAMAGED)
                .setSaveConsumer(value -> { config.displayCondition = value; config.save(); })
                .build())
            .addEntry(entryBuilder.startIntSlider(Text.translatable("lifeline.config.scale"), (int)(config.scale * 100), 10, 200)
                .setDefaultValue(100)
                .setSaveConsumer(value -> { config.scale = value / 100.0f; config.save(); })
                .build());

        return builder.build();
    }
}
