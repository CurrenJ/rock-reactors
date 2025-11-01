package grill24.rockreactors.compat;

import grill24.rockreactors.RockReactors;

import java.lang.reflect.Method;

/**
 * Optional integration helper for the Gelatin UI soft dependency - Menus.
 *
 * This class never requires Gelatin UI at compile time. It detects the library
 * via reflection at runtime and falls back to no-op when it's not present.
 * Use this class from common code to call optional UI menu features safely.
 */
public final class GelatinMenusCompat {
    private static final org.slf4j.Logger LOGGER = RockReactors.LOGGER;

    private static boolean initialized = false;

    // Detected Gelatin UI holder and cached method if found
    private static Class<?> gelatinClass = null;

    private static Class<?> gelatinMenus = null;
    private static Method menusInit = null;

    private GelatinMenusCompat() {}

    /**
     * Initialize detection. Safe to call multiple times.
     */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        try {
            // Try several likely class names; the exact API may vary between platforms/versions.
            String[] candidates = new String[] {
                "io.github.currenj.gelatinui.GelatinUi",
                "io.github.currenj.gelatinui.GelatinUI",
                "io.github.currenj.gelatinui.Gelatin"
            };

            for (String cname : candidates) {
                try {
                    gelatinClass = Class.forName(cname);
                    break;
                } catch (ClassNotFoundException ignored) {
                    // try next
                }
            }

            if (gelatinClass == null) {
                LOGGER.info("Gelatin UI not found on classpath; optional UI features disabled.");
                return;
            }

            // Try to find GelatinMenus registration helper
            try {
                gelatinMenus = Class.forName("grill24.rockreactors.compat.GelatinMenus");
                menusInit = gelatinMenus.getMethod("registerMenus");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                LOGGER.warn("GelatinMenus registration helper not found or incompatible: {}", e.toString());
            }

            LOGGER.info("Gelatin UI detected on classpath; optional menu integration enabled.");

            onGelatinUiDetected();
        } catch (Throwable t) {
            // Be defensive: any problem here must not stop the game from loading.
            LOGGER.warn("Failed to initialize Gelatin UI menu integration (continuing without it): {}", t.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static void onGelatinUiDetected() {
        if (menusInit != null) {
            try {
                menusInit.invoke(null);
            } catch (Throwable t) {
                LOGGER.warn("Failed to register Gelatin UI menu registry event listeners: {}", t.toString());
            }
        }
    }
}
