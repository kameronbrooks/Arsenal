package com.arsenal.client;

import com.arsenal.Arsenal;
import com.arsenal.menu.MunitionsBenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Munitions Bench. Draws the container background, the burning flame under the fuel
 * slot, and the casting-progress arrow between the input and output slots.
 */
public class MunitionsBenchScreen extends AbstractContainerScreen<MunitionsBenchMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arsenal.MOD_ID, "textures/gui/munitions_bench.png");

    public MunitionsBenchScreen(MunitionsBenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Left-align the title with the top-left slot; keep the inventory label at the default spot.
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Fuel flame: overlay sprite stored at u=176,v=0 (14x14), drawn bottom-up.
        if (this.menu.isLit()) {
            int flame = this.menu.getLitHeight();
            guiGraphics.blit(TEXTURE, x + 56, y + 36 + (14 - flame), 176, 14 - flame, 14, flame);
        }

        // Casting arrow: filled sprite stored at u=176,v=14 (24x17), revealed left-to-right.
        int arrow = this.menu.getCookArrowWidth();
        if (arrow > 0) {
            guiGraphics.blit(TEXTURE, x + 79, y + 34, 176, 14, arrow, 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
