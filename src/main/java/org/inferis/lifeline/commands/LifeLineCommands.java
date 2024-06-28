package org.inferis.lifeline.commands;

import org.inferis.lifeline.EntityTracker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class LifeLineCommands {
    @Environment(EnvType.CLIENT)
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommandManager.literal("lllist")
                // .then(ClientCommandManager.literal("list"))
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    var entities = EntityTracker.INSTANCE.getTrackedEntities();
                    player.sendMessage(Text.of(entities.size() + " entities tracked:"));
                    for (var entity: entities) {
                        player.sendMessage(Text.of(entity.getUuid() + ": " + entity.getName()));
                    }                  
                    return 1;
                }));
        });
	}
}
