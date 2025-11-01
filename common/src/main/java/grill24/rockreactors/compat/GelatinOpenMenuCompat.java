package grill24.rockreactors.compat;

import grill24.rockreactors.RockReactors;

import java.lang.reflect.Method;

import net.minecraft.server.level.ServerPlayer;

/**
 * Optional integration helper for opening Gelatin UI menus by id.
 *
 * This class never requires Gelatin UI at compile time. It detects the library
 * via reflection at runtime and falls back to no-op when it's not present.
 */
public final class GelatinOpenMenuCompat {
    private static final org.slf4j.Logger LOGGER = RockReactors.LOGGER;

    private static boolean initialized = false;
    private static boolean available = false;

    // Detected Gelatin UI holder and cached method if found
    private static Class<?> gelatinClass = null;

    private static Class<?> gelatinMenus = null;
    private static Method openMenuByIdMethod = null;

    private GelatinOpenMenuCompat() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        try {
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
                available = false;
                LOGGER.info("Gelatin UI not found on classpath; optional open-menu features disabled.");
                return;
            }

            try {
                gelatinMenus = Class.forName("grill24.rockreactors.compat.GelatinMenus");
                openMenuByIdMethod = gelatinMenus.getMethod("openMenuById", ServerPlayer.class, String.class);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                LOGGER.warn("GelatinMenus open-by-id not found or incompatible: {}", e.toString());
            }

            available = true;
            LOGGER.info("Gelatin UI detected on classpath; optional open-menu integration enabled.");
        } catch (Throwable t) {
            available = false;
            LOGGER.warn("Failed to initialize Gelatin UI open-menu integration (continuing without it): {}", t.toString());
        }
    }

    public static boolean isAvailable() {
        if (!available) init();
        return available;
    }

    public static void openRockReactorsMenu(ServerPlayer player) {
        if (!isAvailable()) return;

        try {
            if (openMenuByIdMethod != null) {
                openMenuByIdMethod.invoke(null, player, "rockreactors");
            }
        } catch (Throwable t) {
            Throwable cause = t;
            if (t instanceof java.lang.reflect.InvocationTargetException) {
                cause = ((java.lang.reflect.InvocationTargetException) t).getCause();
            }
            LOGGER.warn("Failed to open Rock Reactors menu: {}", cause.toString());
        }
    }
}

