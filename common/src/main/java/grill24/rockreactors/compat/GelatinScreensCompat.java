package grill24.rockreactors.compat;

import grill24.rockreactors.RockReactors;

import java.lang.reflect.Method;

/**
 * Optional integration helper for the Gelatin UI soft dependency - Screens.
 *
 * This class never requires Gelatin UI at compile time. It detects the library
 * via reflection at runtime and falls back to no-op when it's not present.
 * Use this class from common code to call optional UI screen features safely.
 */
public final class GelatinScreensCompat {
    private static final org.slf4j.Logger LOGGER = RockReactors.LOGGER;

    private static boolean initialized = false;

    // Detected Gelatin UI holder and cached method if found
    private static Class<?> gelatinClass = null;

    private static Class<?> gelatinScreens = null;
    private static Method screensInit = null;

    private GelatinScreensCompat() {}

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

            // Try to find GelatinScreens
            try {
                gelatinScreens = Class.forName("grill24.rockreactors.compat.GelatinScreens");
                screensInit = gelatinScreens.getMethod("registerScreens");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                LOGGER.warn("GelatinScreens not found or incompatible: {}", e.toString());
            }

            LOGGER.info("Gelatin UI detected on classpath; optional screen integration enabled.");

            onGelatinUiDetected();
        } catch (Throwable t) {
            // Be defensive: any problem here must not stop the game from loading.
            LOGGER.warn("Failed to initialize Gelatin UI screen integration (continuing without it): {}", t.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static void onGelatinUiDetected() {
        if (screensInit != null) {
            try {
                screensInit.invoke(null);
            } catch (Throwable t) {
                LOGGER.warn("Failed to register Gelatin UI screen registry event listeners: {}", t.toString());
            }
        }
    }
}
