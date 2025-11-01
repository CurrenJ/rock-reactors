package grill24.rockreactors.compat;

import grill24.rockreactors.RockReactors;
import grill24.rockreactors.data.FluidInteractionData;
import io.github.currenj.gelatinui.GelatinUi;
import io.github.currenj.gelatinui.gui.UI;
import io.github.currenj.gelatinui.gui.UIElement;
import io.github.currenj.gelatinui.gui.UIScreen;
import io.github.currenj.gelatinui.gui.components.*;
import io.github.currenj.gelatinui.gui.effects.WanderEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RockReactorElement extends VBox {
    private UIScreen screen;

    public RockReactorElement(UIScreen screen, FluidInteractionData interactionData) {
        super();

        this.screen = screen;

        this.padding(5).spacing(3);

        // Get fluid item
        ItemStack fluidItem = switch (interactionData.getFluidType()) {
            case "lava_source", "lava_flowing", "any_lava" -> new ItemStack(Items.LAVA_BUCKET);
            default -> ItemStack.EMPTY;
        };

        // Get condition item
        ItemStack conditionItem = interactionData.getCondition().getBlock()
            .map(block -> new ItemStack(block.asItem()))
            .orElse(ItemStack.EMPTY);

        // Get result item
        ItemStack resultItem = new ItemStack(interactionData.getResultBlock().asItem());

        // Create inputs HBox
        HBox interactionitems = UI.hbox().spacing(5).alignment(HBox.Alignment.CENTER);
        ItemRenderer<ItemRenderer.ItemRendererImpl> fluidRenderer = createItemRenderer(fluidItem);

        interactionitems.addChild(fluidRenderer);
        if (!conditionItem.isEmpty()) {
            ItemRenderer<ItemRenderer.ItemRendererImpl> conditionRenderer = createItemRenderer(conditionItem);
            interactionitems.addChild(conditionRenderer);
        }

        // Create arrow
        SpriteRectangle<SpriteRectangle.SpriteRectangleImpl> arrow = UI.spriteRectangle(16, 16,
                        ResourceLocation.fromNamespaceAndPath(RockReactors.MOD_ID, "textures/arrow.png"));
        hoverScale(arrow, 1.5f);
        interactionitems.addChild(arrow);

        // Create output
        ItemRenderer<ItemRenderer.ItemRendererImpl> outputRenderer = createItemRenderer(resultItem);
        WanderEffect wanderEffect = new WanderEffect();
        wanderEffect.setRadius(2f);
        wanderEffect.setSpeed(1f);
        outputRenderer.addEffect(wanderEffect);
        interactionitems.addChild(outputRenderer);

        // Add to this VBox
        this.addChild(interactionitems);

//        this.backgroundSprite(SpriteData.texture(panelTex)
//                .uv(0, 0, 16, 16)
//                .tileScale(2f)
//                .slice(6, 6, 6, 6));
    }

    private <T extends UIElement<?>> T hoverScale(T element, float scale) {
        element.onMouseEnter(e -> element.setTargetScale(scale, true));
        element.onMouseExit(e -> element.setTargetScale(1.0f, true));
        return element;
    }

    private SpriteRectangle.SpriteRectangleImpl createTooltip(ItemStack itemStack) {
        final ResourceLocation panelTex = ResourceLocation.fromNamespaceAndPath(GelatinUi.MOD_ID, "textures/gui/panel.png");
        SpriteRectangle.SpriteRectangleImpl tooltip = UI
                .spriteRectangle(16, 16, UI.rgb(0, 0, 0))
                .padding(5, 5)
                .autoSize(true).text(itemStack.getHoverName().getString(), UI.rgb(255, 255, 255))
                .texture(SpriteData
                        .texture(panelTex)
                        .uv(0, 0, 15, 15)
                        .textureSize(16, 16)
                        .slice(11, 4, 7, 8)
                        .tileScale(1f)
                        .renderMode(SpriteRenderMode.TILE)
                );
        return tooltip;
    }

    private ItemRenderer.ItemRendererImpl createItemRenderer(ItemStack itemStack) {
        ItemRenderer.ItemRendererImpl itemRenderer = UI.itemRenderer(itemStack);

        hoverScale(itemRenderer, 1.5f);
        SpriteRectangle.SpriteRectangleImpl tooltip = createTooltip(itemStack);
        itemRenderer.tooltip(screen, tooltip);

        return itemRenderer;
    }
}
