package net.mcreator.lostinfog;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT)
public class RadioVhsLoopClient {
    
    private static final ResourceLocation RADIO_ON_ID = ResourceLocation.fromNamespaceAndPath("lostinfog", "radio_2");
    private static final ResourceLocation VHS_ID = ResourceLocation.fromNamespaceAndPath("lostinfog", "radiovhs");
    private static final int RANGE = 8;
    private static final int SCAN_INTERVAL = 5;
    private static final Map<BlockPos, RadioLoopSound> ACTIVE_SOUNDS = new HashMap<>();
    private static int scanTicks;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            stopAllSounds();
            return;
        }

        cleanupStoppedSounds();

        if (++scanTicks < SCAN_INTERVAL) {
            return;
        }

        SoundEvent vhsSound = getVhsSound();
        if (vhsSound == null) {
            return;
        }

        scanTicks = 0;
        ClientLevel level = minecraft.level;
        Block radioOnBlock = BuiltInRegistries.BLOCK.get(RADIO_ON_ID);
        Set<BlockPos> foundPositions = new HashSet<>();
        BlockPos playerPos = minecraft.player.blockPosition();
        BlockPos min = playerPos.offset(-RANGE, -RANGE, -RANGE);
        BlockPos max = playerPos.offset(RANGE, RANGE, RANGE);

        for (BlockPos scanPos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(scanPos).getBlock() == radioOnBlock) {
                BlockPos soundPos = scanPos.immutable();
                foundPositions.add(soundPos);

                if (!ACTIVE_SOUNDS.containsKey(soundPos)) {
                    RadioLoopSound sound = new RadioLoopSound(soundPos, vhsSound);
                    ACTIVE_SOUNDS.put(soundPos, sound);
                    minecraft.getSoundManager().play(sound);
                }
            }
        }

        Iterator<Map.Entry<BlockPos, RadioLoopSound>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, RadioLoopSound> entry = iterator.next();
            if (!foundPositions.contains(entry.getKey())) {
                entry.getValue().stopLoop();
                iterator.remove();
            }
        }
    }

    private static void cleanupStoppedSounds() {
        ACTIVE_SOUNDS.entrySet().removeIf(entry -> entry.getValue().isDone());
    }

    private static void stopAllSounds() {
        for (RadioLoopSound sound : ACTIVE_SOUNDS.values()) {
            sound.stopLoop();
        }
        ACTIVE_SOUNDS.clear();
    }

    private static boolean isRadioOn(ClientLevel level, BlockPos pos) {
        Block radioOnBlock = BuiltInRegistries.BLOCK.get(RADIO_ON_ID);
        return level.getBlockState(pos).getBlock() == radioOnBlock;
    }

    private static class RadioLoopSound extends AbstractTickableSoundInstance {
        private final BlockPos pos;

        private RadioLoopSound(BlockPos pos, SoundEvent sound) {
            super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.pos = pos.immutable();
            this.x = pos.getX() + 0.5D;
            this.y = pos.getY() + 0.5D;
            this.z = pos.getZ() + 0.5D;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.looping = true;
            this.delay = 0;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.relative = false;
        }

        @Override
        public void tick() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || !isRadioOn(minecraft.level, pos)) {
                stopLoop();
            }
        }

        private void stopLoop() {
            stop();
        }

        private boolean isDone() {
            return isStopped();
        }
    }

    private static SoundEvent getVhsSound() {
        return BuiltInRegistries.SOUND_EVENT.get(VHS_ID);
    }
}