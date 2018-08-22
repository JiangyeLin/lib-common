package com.trc.android.common.dev;

public class DevConfig {
    private boolean debugMode;
    public static DevConfig instance;

    private DevConfig() {

    }

    public static DevConfig get() {
        if (null == instance) {
            synchronized (DevConfig.class) {
                instance = new DevConfig();
            }
        }
        return instance;
    }

    public void setDebugMode(boolean isDebug) {
        debugMode = isDebug;
    }

    public static boolean isDebug() {
        return instance.debugMode;
    }
}
