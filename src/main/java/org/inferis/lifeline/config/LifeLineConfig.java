package org.inferis.lifeline.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.inferis.lifeline.LifeLine;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

public class LifeLineConfig {
	private static final String CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("lifeline.json").toString();

    public enum DisplayMode {
        LABEL,
        HEARTS,
        BOTH
    }
    public DisplayMode displayMode = DisplayMode.BOTH;
    public boolean renderingEnabled = true;
    public enum DisplayCondition {
        ALWAYS,
        DAMAGED
    }
    public DisplayCondition displayCondition = DisplayCondition.DAMAGED;
    public float scale = 1.0f;

    public void save() {
        var file = new File(CONFIG_FILE);
        try {
            var writer = Files.asCharSink(file, Charsets.UTF_8);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(this));
        } catch (IOException e) {
            LifeLine.LOGGER.error("Could not save config", e);
        }
    }

    public boolean load() {
        var file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return false;
        }

        try {
            var json = Files.asCharSource(file, Charsets.UTF_8).read();
            Gson gson = new Gson();
            var loadedConfig = gson.fromJson(json, LifeLineConfig.class);

            renderingEnabled = loadedConfig.renderingEnabled;
            displayMode = loadedConfig.displayMode;
            displayCondition = loadedConfig.displayCondition;
            scale = loadedConfig.scale;

            return true;
        }
        catch (FileNotFoundException e) {
            LifeLine.LOGGER.error("Could not open config", e);
        }
        catch (IOException e) {
            LifeLine.LOGGER.error("Could not read config", e);
        }
        catch (JsonSyntaxException e) {
            LifeLine.LOGGER.error("Could not read config, syntax error", e);
        }
        return false;
    }

    public void initialLoad() {
        // if we didn't load, no file (or a corrupt one) is present, so save
        // our defaults.
        if (!load()) {
            save();
        }
    }
}
