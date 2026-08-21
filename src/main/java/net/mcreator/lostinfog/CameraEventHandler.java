package net.mcreator.lostinfog.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class CameraEventHandler {

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused()) return;

        if (!mc.options.getCameraType().isFirstPerson()) return;

        CameraHandler.INSTANCE.tick();

        event.setYaw(event.getYaw() + CameraHandler.INSTANCE.yawOffset);
        event.setPitch(event.getPitch() + CameraHandler.INSTANCE.pitchOffset);
        event.setRoll(event.getRoll() + CameraHandler.INSTANCE.rollOffset);
    }
}