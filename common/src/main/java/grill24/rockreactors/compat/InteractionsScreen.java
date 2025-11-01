package grill24.rockreactors.compat;

import grill24.rockreactors.data.FluidInteractionData;
import grill24.rockreactors.registry.FluidInteractionRegistry;
import io.github.currenj.gelatinui.GelatinUIScreen;
import io.github.currenj.gelatinui.gui.GelatinMenu;
import io.github.currenj.gelatinui.gui.UI;
import io.github.currenj.gelatinui.gui.components.VBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Optional;

public class InteractionsScreen extends GelatinUIScreen<GelatinMenu> {

    protected InteractionsScreen(GelatinMenu menu, Inventory inventory) {
        super(menu, inventory, Component.literal(""));
    }

    private VBox outerVBox;

    @Override
    protected void buildUI() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        outerVBox = UI.vbox()
                .alignment(VBox.Alignment.CENTER)
                .padding(10).spacing(10)
                .fillWidth(true)
                .fillHeight(true)
                .scaleToHeight(this.height);

        VBox hBox = UI.vbox()
                .alignment(VBox.Alignment.CENTER)
                .fillHeight(true)
                .padding(10).spacing(5);

        Optional<HolderLookup.RegistryLookup<FluidInteractionData>> fluidInteractionLookup = this.minecraft.level.registryAccess().lookup(FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY);
        fluidInteractionLookup.ifPresent(lookup -> {
            lookup.listElements().forEach(holder -> {
                FluidInteractionData data = holder.value();
                RockReactorElement el = new RockReactorElement(this.uiScreen, data);

                hBox.addChild(el);
            });
        });

        outerVBox.addChild(hBox);
        this.uiScreen.setRoot(outerVBox);
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int i, int i1, float v) {

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean anyPressed = super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == 54) {
            this.outerVBox.scaleToHeight(this.height);
            return true;
        } else {
            return false;
        }
    }
}
