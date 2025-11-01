package grill24.rockreactors.compat;

import grill24.rockreactors.RockReactors;
import io.github.currenj.gelatinui.registration.menu.ScreenRegistrationEvent;

public class GelatinScreens {
    /**
     * On NeoForge, invoked by reflection in {@link GelatinScreensCompat} to force static initializer execution.
     * On Fabric, use this method as gelatinui:screen_registration entrypoint.
     * Only safe to load this class if Gelatin UI is present.
     */
    public static void registerScreens() {
        ScreenRegistrationEvent.registerListener(GelatinScreens::_registerScreens);
        RockReactors.LOGGER.info("Registered Rock Reactors screen registration listener with Gelatin UI.");
    }

    private static void _registerScreens(ScreenRegistrationEvent.ScreenRegistrar registrar) {
        registrar.register("rockreactors", InteractionsScreen::new);
        RockReactors.LOGGER.info("Registered Rock Reactors screen with Gelatin UI.");
    }
}
