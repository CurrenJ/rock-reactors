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
    private static final String GELATIN_MENUS_CLASS = "grill24.rockreactors.compat.GelatinMenus";
    private static final String GELATIN_MENUS_OPEN_METHOD = "openMenuById";

    public static void openRockReactorsMenu(ServerPlayer player) {
        boolean success = CompatUtil.invokeIfDependencyPresent(
            CompatUtil.GELATIN_UI_CLASS,
            GELATIN_MENUS_CLASS,
            GELATIN_MENUS_OPEN_METHOD,
            player,
            "rockreactors"
        );

        if (success) {
            RockReactors.LOGGER.info("Opened Rock Reactors menu for player {} via Gelatin UI.", player.getName().getString());
        } else {
            RockReactors.LOGGER.warn("Failed to open Rock Reactors menu; Gelatin UI not present.");
        }
    }
}
