package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ShaderCheck {
    public static boolean shown = false;

    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen && !shown) {
            shown = true;
            Minecraft.getInstance().setScreen(new ShaderWarningScreen(event.getScreen()));
        }
    }

    public static class ShaderWarningScreen extends Screen {
        private final Screen parent;
        private static final String MODRINTH_URL = "https://modrinth.com/mod/veil-iris-lights-compat";

        protected ShaderWarningScreen(Screen parent) {
            super(Component.literal("Shader Warning"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            super.init();
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.addRenderableWidget(Button.builder(Component.literal("Open Compatibility Mod Page"), b -> Util.getPlatform().openUri(MODRINTH_URL)).pos(centerX - 125, centerY + 50).size(250, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("I understand, continue"), b -> onClose()).pos(centerX - 125, centerY + 80).size(250, 20).build());
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
            g.fill(0, 0, this.width, this.height, 0xFF000000);
            
            int centerX = width / 2;
            int y = height / 2 - 70;

            g.drawCenteredString(font, "⚠ Shader Incompatibility Detected ⚠", centerX, y, 0xFFFF5555);
            y += 25;
            g.drawCenteredString(font, "This mod does not support shaders well.THEY WILL HELP ELIMINATE THE FOG MECHANICS", centerX, y, 0xFFFFFFFF);
            y += 15;
            g.drawCenteredString(font, "If you want to use the flashlight, please disable shaders.", centerX, y, 0xFFFFFFFF);
            y += 15;
            g.drawCenteredString(font, "If you must use shaders, install this mod: (That won't help keep the fog)", centerX, y, 0xFFFFFFFF);
            y += 15;
            g.drawCenteredString(font, "Veil Iris Lights Compat", centerX, y, 0xFF55FF55);
            y += 25;
            g.drawCenteredString(font, "WARNING: IF YOU USE SHADERS WITHOUT THIS MOD,", centerX, y, 0xFFFFFF00);
            y += 12;
            g.drawCenteredString(font, "YOU WILL ENCOUNTER GRAPHICAL FLASHLIGHT BUGS!", centerX, y, 0xFFFFFF00);
            
            super.render(g, mouseX, mouseY, partialTicks);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        public void renderBackground(GuiGraphics g, int mX, int mY, float pT) {
        }
    }
}