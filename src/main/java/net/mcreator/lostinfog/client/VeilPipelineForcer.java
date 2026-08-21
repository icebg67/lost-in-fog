package net.mcreator.lostinfog.client;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class VeilPipelineForcer {

    private static LightRenderHandle<AreaLightData> dummyLight;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.player != null) {
            if (dummyLight == null) {
                AreaLightData light = new AreaLightData();
                light.setBrightness(0.001f);
                light.setDistance(35.0f);
                light.setAngle(1.0f);
                light.setSize(1.0f, 1.0f);
                light.getOrientation().set(new Quaternionf());
                dummyLight = VeilRenderSystem.renderer().getLightRenderer().addLight(light);
            }
            dummyLight.getLightData().getPosition().set(
                    (float) mc.player.getX(),
                    (float) mc.player.getY() + 1.0f,
                    (float) mc.player.getZ()
            );
        } else {
            if (dummyLight != null) {
                dummyLight.free();
                dummyLight = null;
            }
        }
    }
}