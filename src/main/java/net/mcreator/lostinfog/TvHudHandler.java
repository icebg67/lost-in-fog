package net.mcreator.lostinfog;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber(value = Dist.CLIENT)
public class TvHudHandler {

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
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

        if (!blockId.equals("lostinfog:tvoff") && !blockId.equals("lostinfog:tvon")) {
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

        long duration = blockId.equals("lostinfog:tvoff") ? 10000 : 15000;

        if (now - startTime < duration) {
            GuiGraphics graphics = event.getGuiGraphics();
            String text = blockId.equals("lostinfog:tvoff") ? "SHIFT + RMB to turn on the TV" : "SHIFT + RMB to turn off the TV | RMB watch tv";
            int x = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(text) / 2;
            int y = mc.getWindow().getGuiScaledHeight() / 2 + 15;
            graphics.drawString(mc.font, text, x, y, 0xFFFFFF);
        }
    }
}