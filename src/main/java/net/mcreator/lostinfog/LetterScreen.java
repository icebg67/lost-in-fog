package net.mcreator.lostinfog.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LetterScreen extends Screen {
    private static final ResourceLocation LETTER_TEXTURE = ResourceLocation.fromNamespaceAndPath("lostinfog", "textures/letter/let.png"); //поменяй письмо

    public LetterScreen() {
        super(Component.empty());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int width = 256; 
        int height = 256;
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;
        
        guiGraphics.blit(LETTER_TEXTURE, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}