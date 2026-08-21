package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT)
public class FrostbiteOverlay {
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && mc.level.dimension().location().toString().equals("lostinfog:the_fog_forest")) {
                FrostbiteData data = mc.player.getData(LostinfogModAttachments.FROSTBITE);
                GuiGraphics gui = event.getGuiGraphics();
                
                int color = 0xFFFFFF;
                if (data.warmthTimer > 0) {
                    color = 0xFFDD00;
                } else if (data.freezeTimer > 0) {
                    color = 0x00AAFF;
                }
                
                if (data.frostbite >= 7) {
                    color = 0x0000FF;
                }
                
                gui.drawString(mc.font, "Frostbite: " + data.frostbite + "/10", 10, mc.getWindow().getGuiScaledHeight() - 20, color, true);
            }
        }
    }
}