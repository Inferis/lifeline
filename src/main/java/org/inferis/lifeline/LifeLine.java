package org.inferis.lifeline;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.inferis.lifeline.commands.LifeLineCommands;
import org.inferis.lifeline.config.LifeLineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class LifeLine implements ClientModInitializer {
	public static final String MODID = "lifeline";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final LifeLineConfig CONFIG = new LifeLineConfig();

	@Override
	public void onInitializeClient() {
		CONFIG.initialLoad();
		LifeLineCommands.registerCommands();
		ClientTickEvents.END_CLIENT_TICK.register(EntityTracker::tick);
	}
}