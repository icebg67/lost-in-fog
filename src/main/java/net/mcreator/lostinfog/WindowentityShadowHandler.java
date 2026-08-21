package net.mcreator.lostinfog;

import net.mcreator.lostinfog.init.LostinfogModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Field;
import java.util.Map;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class WindowentityShadowHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(WindowentityShadowHandler::disableShadow);
    }

    @SuppressWarnings("unchecked")
    private static void disableShadow() {
        try {
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            Field renderersField = EntityRenderDispatcher.class.getDeclaredField("renderers");
            renderersField.setAccessible(true);
            Map<?, EntityRenderer<?>> renderers = (Map<?, EntityRenderer<?>>) renderersField.get(dispatcher);
            EntityRenderer<?> renderer = renderers.get(LostinfogModEntities.WINDOWENTITY.get());
            if (renderer != null) {
                Field shadowField = EntityRenderer.class.getDeclaredField("shadowRadius");
                shadowField.setAccessible(true);
                shadowField.setFloat(renderer, 0.0f);
            }
        } catch (ReflectiveOperationException failure) {
            failure.printStackTrace();
        }
    }
}