package org.inferis.lifeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

public class EntityTracker {
    public static final EntityTracker INSTANCE = new EntityTracker(MinecraftClient.getInstance());

    private ConcurrentHashMap<UUID, Integer> trackedEntities = new ConcurrentHashMap<>();
    private MinecraftClient client;

    public static void tick(MinecraftClient client) {
        INSTANCE.tick();
    }

    public EntityTracker(MinecraftClient client) {
        this.client = client;
    }

    public void tick() {
        if (client.player == null || client.world == null) {
            return;
        }

        if (!LifeLine.CONFIG.renderingEnabled) {
            return;
        }

        trackNewEntities();
        cullTrackedEntities();
    }

    public List<LivingEntity> getTrackedEntities() {
        ArrayList<LivingEntity> entities = new ArrayList<LivingEntity>();

        for (var entity: client.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && trackedEntities.containsKey(entity.getUuid())) {
                entities.add(livingEntity);                
            }
        }

        return entities;
    }

    private void trackNewEntities() {
        // Add new entities if needed
        for (var entity: client.world.getEntitiesByClass(LivingEntity.class, getPlayerBox(), e -> shouldRenderOverEntity(e, client.player))) {
            var uuid = entity.getUuid();
            if (!trackedEntities.containsKey(uuid)) {
                trackedEntities.put(uuid, 1200); // 1 minute (20 * 60 ticks)
            }
        }
    }

    private void cullTrackedEntities() {
        Iterator<Map.Entry<UUID, Integer>> iterator = trackedEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            // decrease TTL 
            entry.setValue(entry.getValue() - 1);
            if (entry.getValue() <= 0) {
                iterator.remove(); 
            }

            // Remove entity if we shouldn't be rendering over it anyway
            var entity = entityFromUUID(entry.getKey());
            if (!shouldRenderOverEntity(entity, client.player)) {
                iterator.remove(); 
            }
        }

        // Don't want unbounded growth, just restart if we go over our limit
        if (trackedEntities.size() >= 1024) {
            trackedEntities.clear();
        }
    }

    private boolean shouldRenderOverEntity(LivingEntity entity, ClientPlayerEntity player) {
        return entity != null &&
            !entity.isRegionUnloaded() &&
            entity.isAlive() &&
            entity.isLiving() &&
            player.getVehicle() != entity &&
            !entity.isInvisibleTo(player);
    }

    private LivingEntity entityFromUUID(UUID uuid) {
        for (var entity: client.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && livingEntity.getUuid() == uuid) {
                return livingEntity;
            }
        }
        return null;
    }

    private Box getPlayerBox() {
        return client.player.getBoundingBox().expand(4 * 16); // 4 chunks
    }
}
