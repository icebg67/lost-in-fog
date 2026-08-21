package net.mcreator.lostinfog.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.util.RandomSource;
import java.util.stream.IntStream;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class CameraHandler {
    public static final CameraHandler INSTANCE = new CameraHandler();

    public double currentNoiseSpeed = 1.0;
    public double currentAmplitude = 1.5;
    private double noiseY = 0;
    private long lastTime = System.currentTimeMillis();
    private final PerlinNoise noiseSampler;

    private static float prevYaw;
    private static float rotAmount;
    private static float spinRoll;
    private static float strafeRoll;

    public float pitchOffset;
    public float yawOffset;
    public float rollOffset;

    private CameraHandler() {
        this.noiseSampler = PerlinNoise.create(RandomSource.create(), IntStream.of(0));
        CameraConfig.load();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("disableshaking")
            .then(Commands.argument("enabled", BoolArgumentType.bool())
            .executes(context -> {
                boolean input = BoolArgumentType.getBool(context, "enabled");
                CameraConfig.enabled = !input;
                CameraConfig.save();
                context.getSource().sendSuccess(() -> Component.literal("Camera shaking : " + CameraConfig.enabled), false);
                return 1;
            })));
    }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null) {
            this.lastTime = System.currentTimeMillis();
            return;
        }

        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - this.lastTime) / 1000.0;
        this.lastTime = currentTime;

        if (deltaTime > 0.5) deltaTime = 0.016;
        if (!CameraConfig.enabled) {
            this.pitchOffset = 0;
            this.yawOffset = 0;
            this.rollOffset = 0;
            prevYaw = mc.player.getYRot();
            return;
        }

        Player player = mc.player;
        
        float playerSpeed = (float) (player.walkDist - player.walkDistO) * 6f;
        if (player.isFallFlying()) playerSpeed = 0;

        double targetAmp;
        double targetSpeed;

        if (playerSpeed < 0.02) {
            targetAmp = CameraConfig.idleAmp;
            targetSpeed = CameraConfig.idleSpeed;
        } else if (player.isSprinting()) {
            targetAmp = CameraConfig.sprintAmp;
            targetSpeed = CameraConfig.sprintSpeed;
        } else {
            targetAmp = CameraConfig.walkAmp;
            targetSpeed = CameraConfig.walkSpeed;
        }

        this.currentAmplitude = Mth.lerp(3.0 * deltaTime, this.currentAmplitude, targetAmp);
        this.currentNoiseSpeed = Mth.lerp(3.0 * deltaTime, this.currentNoiseSpeed, targetSpeed);
        this.noiseY += (this.currentNoiseSpeed * deltaTime);
        if (this.noiseY >= 1000) this.noiseY = 0;

        float yaw = player.getYRot();
        rotAmount += (yaw - prevYaw);
        while (rotAmount > 180) rotAmount -= 360;
        while (rotAmount < -180) rotAmount += 360;

        float lerpFactorSpin = 1.0f - (float) Math.exp(-15.0f * deltaTime);
        float lerpFactorRot = 1.0f - (float) Math.exp(-12.0f * deltaTime);
        float lerpFactorStrafe = 1.0f - (float) Math.exp(-12.0f * deltaTime);

        spinRoll = Mth.lerp(lerpFactorSpin, spinRoll, rotAmount * 0.05f);
        rotAmount = Mth.lerp(lerpFactorRot, rotAmount, 0f);
        spinRoll = Mth.clamp(spinRoll, -6.0f, 6.0f);

        double velX = player.getDeltaMovement().x;
        double velZ = player.getDeltaMovement().z;
        float angle = (float) Math.toRadians(360.0f - yaw);
        float relativeX = (float) (velX * Math.cos(angle) - velZ * Math.sin(angle));

        strafeRoll = Mth.lerp(lerpFactorStrafe, strafeRoll, -relativeX * 2.4f);
        strafeRoll = Mth.clamp(strafeRoll, -6.0f, 6.0f);

        this.pitchOffset = (float) (this.currentAmplitude * Mth.clamp(noiseSampler.getValue(1, this.noiseY, 0), -1.0, 1.0));
        this.yawOffset = (float) (this.currentAmplitude * Mth.clamp(noiseSampler.getValue(73, this.noiseY, 0), -1.0, 1.0));
        this.rollOffset = ((float) (this.currentAmplitude * Mth.clamp(noiseSampler.getValue(146, this.noiseY, 0), -1.0, 1.0)) * 0.4f) + spinRoll + strafeRoll;

        prevYaw = yaw;
    }
}