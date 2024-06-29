package org.inferis.lifeline;

import net.minecraft.util.Identifier;

public enum HeartType {
    EMPTY("container"),
    REGULAR_FULL("full"),
    REGULAR_HALF("half"),
    ABSORBING_FULL("absorbing_full"),
    ABSORBING_HALF("absorbing_half");

    public final Identifier icon;

    HeartType(String name) {
        icon = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + name + ".png");
    }
}
