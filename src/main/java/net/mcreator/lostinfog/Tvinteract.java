package net.mcreator.lostinfog;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

@EventBusSubscriber
public class Tvinteract {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState existingState = level.getBlockState(pos);

        if (existingState.isAir() || existingState.getBlock() == Blocks.AIR)
            return;

        if (level.isClientSide())
            return;

        Player player = event.getEntity();

        if (!player.isShiftKeyDown())
            return;

        String blockId = BuiltInRegistries.BLOCK.getKey(existingState.getBlock()).toString();

        if (!blockId.equals("lostinfog:tvoff") && !blockId.equals("lostinfog:tvon"))
            return;

        long tick = level.getGameTime();
        long lastCooldown = player.getPersistentData().getLong("tv_cd");

        if (tick < lastCooldown)
            return;

        player.getPersistentData().putLong("tv_cd", tick + 5); //исправлен раассинхрон звуков??

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (blockId.equals("lostinfog:tvoff")) {
            playSound(level, pos, "tvvkl");

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                MinecraftServer server = level.getServer();
                if (server != null) {
                    server.execute(() -> {
                        BlockState currentState = level.getBlockState(pos);

                        if (BuiltInRegistries.BLOCK.getKey(currentState.getBlock())
                                .toString().equals("lostinfog:tvoff")) {

                            BlockState newState = BuiltInRegistries.BLOCK
                                    .get(ResourceLocation.fromNamespaceAndPath("lostinfog", "tvon"))
                                    .defaultBlockState();

                            newState = copyBlockProperties(currentState, newState);
                            level.setBlock(pos, newState, 3);
                        }
                    });
                }
            }).start();
        }

        else if (blockId.equals("lostinfog:tvon")) {
            playSound(level, pos, "tvvikl");

            BlockState newState = BuiltInRegistries.BLOCK
                    .get(ResourceLocation.fromNamespaceAndPath("lostinfog", "tvoff"))
                    .defaultBlockState();

            newState = copyBlockProperties(existingState, newState);
            level.setBlock(pos, newState, 3);
        }
    }

    private static BlockState copyBlockProperties(BlockState from, BlockState to) {
        BlockState result = to;

        for (Property<?> property : from.getProperties()) {
            if (result.hasProperty(property)) {
                result = copyProperty(result, from, property);
            }
        }

        return result;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState to, BlockState from, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    private static void playSound(Level level, BlockPos pos, String soundId) {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath("lostinfog", soundId)
        );

        if (sound != null) {
            serverLevel.playSound(
                    null,
                    pos,
                    sound,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
    }
}