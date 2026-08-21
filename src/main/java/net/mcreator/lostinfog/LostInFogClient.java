package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.mcreator.lostinfog.block.PalkkaBlock;
import net.mcreator.lostinfog.block.Palkka3Block;
import net.mcreator.lostinfog.block.Palkka4Block;

@EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT)
public class LostInFogClient {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult) mc.hitResult;
                Block block = mc.level.getBlockState(hit.getBlockPos()).getBlock();
                if (block instanceof PalkkaBlock || block instanceof Palkka3Block || block instanceof Palkka4Block) {
                    GuiGraphics graphics = event.getGuiGraphics();
                    int x = mc.getWindow().getGuiScaledWidth() / 2;
                    int y = mc.getWindow().getGuiScaledHeight() / 2 + 15;
                    String text = "RMB to pick up the stick";
                    int width = mc.font.width(text);
                    graphics.drawString(mc.font, text, x - width / 2, y, 0xFFFFFF, true);
                }
            }
        }
    }
}