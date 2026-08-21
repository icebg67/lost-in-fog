package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT)
public class DoorHUDHandler {

    private static boolean isLooking = false;
    private static long startTime = 0;
    private static long cooldownUntil = 0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            if (isLooking) {
                isLooking = false;
                cooldownUntil = now + 5000;
            }
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) mc.hitResult;

        if (!(mc.level.getBlockState(blockHit.getBlockPos()).getBlock() instanceof DoorBlock)) {
            if (isLooking) {
                isLooking = false;
                cooldownUntil = now + 5000;
            }
            return;
        }

        if (!isLooking) {
            if (now >= cooldownUntil) {
                isLooking = true;
                startTime = now;
            } else {
                return;
            }
        }

        if (now - startTime < 4000) {
            GuiGraphics graphics = event.getGuiGraphics();
            String text = "Shift + RMB to lock/unlock";
            int x = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(text) / 2;
            int y = mc.getWindow().getGuiScaledHeight() / 2 + 15;
            graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
        }
    }
}