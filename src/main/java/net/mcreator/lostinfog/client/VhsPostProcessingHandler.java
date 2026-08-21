package net.mcreator.lostinfog.client;

import net.mcreator.lostinfog.config.ShaderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class VhsPostProcessingHandler {

    public static final ResourceLocation VHS_SHADER = ResourceLocation.fromNamespaceAndPath("lostinfog", "shaders/post/vhs_horror.json");
    public static final ResourceLocation HEAVY_VHS_SHADER = ResourceLocation.fromNamespaceAndPath("lostinfog", "shaders/post/heavy_vhs_horror.json");

    private static ResourceLocation currentShaderLocation = null;
    private static Field effectActiveField = null;

    static {
        try {
            effectActiveField = GameRenderer.class.getDeclaredField("effectActive");
            effectActiveField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                effectActiveField = GameRenderer.class.getDeclaredField("f_109078_");
                effectActiveField.setAccessible(true);
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ShaderConfig.update();

        if (!ShaderConfig.isShaderEnabled() || mc.level == null || mc.player == null) {
            if (mc.gameRenderer.currentEffect() != null) {
                mc.gameRenderer.shutdownEffect();
                currentShaderLocation = null;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (ShaderConfig.isShaderEnabled() && mc.level != null && mc.player != null) {
            String dimName = mc.level.dimension().location().toString();
            ResourceLocation targetShader = null;

            if (dimName.equals("lostinfog:the_fog_forest")) {
                targetShader = HEAVY_VHS_SHADER;
            } else if (dimName.equals("minecraft:overworld") || dimName.equals("minecraft:the_nether") || dimName.equals("minecraft:the_end")) {
                targetShader = VHS_SHADER;
            }

            if (targetShader != null) {
                if (currentShaderLocation == null || !currentShaderLocation.equals(targetShader) || mc.gameRenderer.currentEffect() == null) {
                    mc.gameRenderer.loadEffect(targetShader);
                    currentShaderLocation = targetShader;
                }

                disableVanillaAutoProcess(mc.gameRenderer);

                if (mc.gameRenderer.currentEffect() != null) {
                    mc.gameRenderer.currentEffect().process(event.getPartialTick().getGameTimeDeltaPartialTick(true));
                }
            } else {
                if (mc.gameRenderer.currentEffect() != null) {
                    mc.gameRenderer.shutdownEffect();
                    currentShaderLocation = null;
                }
            }
        }
    }

    private static void disableVanillaAutoProcess(GameRenderer gameRenderer) {
        if (effectActiveField != null) {
            try {
                effectActiveField.setBoolean(gameRenderer, false);
            } catch (Exception ignored) {
            }
        }
    }
}