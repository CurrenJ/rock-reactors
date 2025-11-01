package grill24.rockreactors.neoforge;

import grill24.rockreactors.RockReactors;
import grill24.rockreactors.compat.GelatinScreensCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = RockReactors.MOD_ID, dist = Dist.CLIENT)
public final class RockReactorsNeoForgeClient {
    public RockReactorsNeoForgeClient() {
        // Try to register GelatinUI screens, if GelatinUI is present.
        GelatinScreensCompat.init();
    }
}
