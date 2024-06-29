package org.inferis.lifeline.config;

import org.inferis.lifeline.LifeLine;

public class LifeLineConfig {
    public enum DisplayMode {
        LABEL,
        HEARTS,
        BOTH
    }
    public DisplayMode displayMode = DisplayMode.BOTH;
    public boolean renderingEnabled = true;
}
