package net.mcreator.lostinfog;

import com.mojang.blaze3d.platform.InputConstants;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Flashlight {

    public static final Map<UUID, FlashlightState> playerStates = new ConcurrentHashMap<>();
    private static final Map<UUID, LightRenderData> renderDataMap = new ConcurrentHashMap<>();

    private static boolean localStateChanged = false;
    private static int flashlightToggleCount = 0;
    private static int tipDelayTimer = -1;
    private static int reminderTimer = 0;
    private static int nextReminderTime = 9600;

    private static final Random RANDOM = new Random();

    private static final String[] REMINDER_TEXTS = {
            "Good thing I brought my flashlight (%s)",
            "I should keep my flashlight close (%s)",
            "I can't see anything without my flashlight (%s)",
            "I hope the batteries still have some charge (%s)",
            "I really shouldn't lose my flashlight (%s)",
            "The flashlight makes me feel safer (%s)",
            "I should check the batteries later (%s)",
            "I don't want to be left in the dark (%s)",
            "I need to remember where I put my flashlight (%s)",
            "I should turn on my flashlight (%s)",
            "I hope the bulb doesn't burn out (%s)",
            "Without my flashlight, I'm practically blind (%s)",
            "I haven't been sleeping well lately (%s)",
            "Maybe I'm just tired (%s)",
            "I need to stay focused (%s)",
            "I thought I heard something again (%s)",
            "I don't remember the nights being this dark (%s)",
            "I wonder if the light can see me too (%s)"
    };

    public static final KeyMapping FLASHLIGHT_KEY = new KeyMapping(
            "key.lostinfog.toggle_flashlight",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.lostinfog.keys"
    );

    public static class FlashlightState {
        public boolean isOn = false;
        public float dist = 35.0f;
        public float bright = 1.8f;
        public float angle = 0.6f;
        public float size = 0.2f;
    }

    private static class LightRenderData {
        public LightRenderHandle<AreaLightData> handle;
        public float smoothRotX;
        public float smoothRotY;
    }

    public static FlashlightState getLocalState() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return playerStates.computeIfAbsent(mc.player.getUUID(), k -> new FlashlightState());
        }
        return new FlashlightState();
    }

    public static void updatePlayerState(UUID uuid, boolean isOn, float dist, float bright, float angle, float size) {
        FlashlightState state = playerStates.computeIfAbsent(uuid, k -> new FlashlightState());
        state.isOn = isOn;
        state.dist = dist;
        state.bright = bright;
        state.angle = angle;
        state.size = size;
    }

    private static void sendSyncPacket(FlashlightState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new net.mcreator.lostinfog.network.FlashlightSyncPayload(
                            mc.player.getUUID(),
                            state.isOn,
                            state.dist,
                            state.bright,
                            state.angle,
                            state.size
                    )
            );
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(FLASHLIGHT_KEY);
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            FlashlightState localState = getLocalState();

            if (localStateChanged) {
                sendSyncPacket(localState);
                localStateChanged = false;
            }

            reminderTimer++;

            if (reminderTimer >= nextReminderTime) {
                reminderTimer = 0;
                nextReminderTime = (5 * 60 * 20) + RANDOM.nextInt((15 * 60 * 20) - (5 * 60 * 20) + 1);

                if (!localState.isOn) {
                    String keyName = FLASHLIGHT_KEY.getTranslatedKeyMessage().getString();
                    String text = String.format(REMINDER_TEXTS[RANDOM.nextInt(REMINDER_TEXTS.length)], keyName);
                    mc.gui.setOverlayMessage(Component.literal(text), false);
                }
            }

            if (tipDelayTimer > 0) {
                tipDelayTimer--;
                if (tipDelayTimer == 0) {
                    if (localState.isOn) {
                        mc.gui.setOverlayMessage(Component.literal("Shift + Mouse Wheel - Adjust flashlight"), false);
                    }
                    tipDelayTimer = -1;
                }
            }

            if (mc.screen == null) {
                while (FLASHLIGHT_KEY.consumeClick()) {
                    localState.isOn = !localState.isOn;
                    sendSyncPacket(localState);

                    String sound = localState.isOn ? "lostinfog:flashlighton" : "lostinfog:flashlightoff";

                    mc.level.playLocalSound(
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(sound)),
                            SoundSource.PLAYERS,
                            1.0f,
                            1.0f,
                            false
                    );

                    if (localState.isOn) {
                        flashlightToggleCount++;
                        if (flashlightToggleCount <= 3) {
                            tipDelayTimer = 100;
                        }
                    } else {
                        tipDelayTimer = -1;
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            FlashlightState localState = getLocalState();
            if (!localState.isOn) return;

            Minecraft mc = Minecraft.getInstance();

            if (InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                double delta = event.getScrollDeltaY();

                localState.dist = Math.max(20.0f, Math.min(35.0f, localState.dist + (float) delta * 1.5f));
                localState.bright = Math.max(0.5f, Math.min(2.0f, localState.bright + (float) delta * 0.1f));
                localState.angle = Math.max(0.5f, Math.min(1.5f, localState.angle - (float) delta * 0.05f));
                localState.size = Math.max(0.2f, Math.min(1.0f, localState.size - (float) delta * 0.05f));

                localStateChanged = true;
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

            renderDataMap.entrySet().removeIf(entry -> {
                if (mc.level.getPlayerByUUID(entry.getKey()) == null) {
                    if (entry.getValue().handle != null) {
                        entry.getValue().handle.free();
                    }
                    return true;
                }
                return false;
            });

            for (AbstractClientPlayer player : mc.level.players()) {
                UUID uuid = player.getUUID();
                FlashlightState state = playerStates.get(uuid);

                if (state != null && state.isOn) {
                    LightRenderData renderData = renderDataMap.computeIfAbsent(uuid, k -> new LightRenderData());

                    if (renderData.handle == null) {
                        renderData.handle = VeilRenderSystem.renderer().getLightRenderer().addLight(new AreaLightData());
                        renderData.smoothRotX = player.getViewXRot(partialTicks);
                        renderData.smoothRotY = player.getViewYRot(partialTicks);
                    }

                    float targetX = player.getViewXRot(partialTicks);
                    float targetY = player.getViewYRot(partialTicks);

                    float lerpFactor = 0.0882f;
                    renderData.smoothRotX += (targetX - renderData.smoothRotX) * lerpFactor;
                    renderData.smoothRotY += (targetY - renderData.smoothRotY) * lerpFactor;

                    Vec3 pos = player.getEyePosition(partialTicks);

                    AreaLightData light = renderData.handle.getLightData();
                    light.getPosition().set((float) pos.x, (float) pos.y + 0.12f, (float) pos.z);
                    light.getOrientation().set(new Quaternionf().rotateXYZ(
                            (float) Math.toRadians(-renderData.smoothRotX),
                            (float) Math.toRadians(renderData.smoothRotY),
                            0
                    ));

                    light.setDistance(state.dist);
                    light.setBrightness(state.bright);
                    light.setAngle(state.angle);
                    light.setSize(state.size, state.size);
                } else {
                    LightRenderData renderData = renderDataMap.remove(uuid);
                    if (renderData != null && renderData.handle != null) {
                        renderData.handle.free();
                    }
                }
            }
        }
    }
}