package net.mcreator.lostinfog.procedures;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;

import java.util.Optional;
import java.util.Random;

@EventBusSubscriber
public class RadioProcedure {

    private static final double[] FREQUENCIES = {101.2, 102.4, 103.5, 104.1, 105.5, 106.8, 107.4, 108.9, 109.1, 110.0};
    private static final String[] NOISE_FRAMES = {"-", "\\", "|", "/"};
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState existingState = level.getBlockState(pos);

        if (existingState.isAir() || existingState.getBlock() == Blocks.AIR) {
            return;
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(existingState.getBlock()).toString();
        if (!blockId.equals("lostinfog:radio") && !blockId.equals("lostinfog:radio_2")) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (level.isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        long tick = level.getGameTime();
        long lastCooldown = player.getPersistentData().getLong("radio_cd");

        if (tick < lastCooldown) {
            return;
        }

        player.getPersistentData().putLong("radio_cd", tick + 5);
        boolean isOn = blockId.equals("lostinfog:radio_2");

        if (player.isShiftKeyDown()) {
            if (isOn) {
                double freq = FREQUENCIES[RANDOM.nextInt(FREQUENCIES.length)];
                boolean is1055 = (freq == 105.5);
                String coords = null;
                boolean hasDay7 = false;

                if (is1055 && player instanceof ServerPlayer serverPlayer) {
                    MinecraftServer server = serverPlayer.getServer();
                    if (server != null) {
                        AdvancementHolder adv = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("lostinfog", "day_7"));
                        if (adv != null && serverPlayer.getAdvancements().getOrStartProgress(adv).isDone()) {
                            hasDay7 = true;
                            if (level instanceof ServerLevel serverLevel) {
                                Optional<Holder.Reference<Structure>> structure = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(ResourceLocation.fromNamespaceAndPath("lostinfog", "vhod"));
                                if (structure.isPresent()) {
                                    Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, HolderSet.direct(structure.get()), pos, 250, false);
                                    if (pair != null) {
                                        BlockPos shelterPos = pair.getFirst();
                                        int distance = (int) Math.sqrt(pos.distSqr(shelterPos));
                                        coords = ChatFormatting.GREEN + "[X: " + shelterPos.getX() + " | Z: " + shelterPos.getZ() + "] " + ChatFormatting.YELLOW + "Dist: " + distance + "m";
                                    }
                                }
                            }
                        }
                    }
                }
                
                long animId = System.currentTimeMillis();
                player.getPersistentData().putLong("radio_anim_id", animId);
                animateText(player, freq, coords, is1055, animId, level, pos, hasDay7);
            }
        } else {
            playSound(level, pos, "rad");
            
            String nextId = isOn ? "radio" : "radio_2";
            BlockState newState = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("lostinfog", nextId)).defaultBlockState();
            newState = copyBlockProperties(existingState, newState);
            level.setBlock(pos, newState, 3);
        }
    }

    private static void playSound(Level level, BlockPos pos, String soundId) {
        if (level instanceof ServerLevel serverLevel) {
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("lostinfog", soundId));
            if (sound != null) {
                serverLevel.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
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

    private static boolean checkAnimId(Player player, long animId) {
        return player.getPersistentData().getLong("radio_anim_id") == animId;
    }

    private static boolean checkRadioAndStopSoundOnMainThread(MinecraftServer server, Player player, long animId, Level level, BlockPos pos) {
        if (!checkAnimId(player, animId)) {
            return false;
        }
        final boolean[] result = {true};
        try {
            server.submit(() -> {
                if (level instanceof ServerLevel serverLevel) {
                    BlockState state = serverLevel.getBlockState(pos);
                    String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                    if (!id.equals("lostinfog:radio_2")) {
                        String cmd = String.format("stopsound @a[x=%d,y=%d,z=%d,distance=..32] block lostinfog:radiosound", pos.getX(), pos.getY(), pos.getZ());
                        serverLevel.getServer().getCommands().performPrefixedCommand(
                                serverLevel.getServer().createCommandSourceStack()
                                        .withLevel(serverLevel)
                                        .withPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ())),
                                cmd
                        );
                        result[0] = false;
                    }
                }
            }).get();
        } catch (Exception e) {
            return false;
        }
        return result[0];
    }

    private static void animateText(Player player, double freq, String shelterCoords, boolean is1055, long animId, Level level, BlockPos pos, boolean hasDay7) {
        new Thread(() -> {
            try {
                if (!(level instanceof ServerLevel serverLevel)) {
                    return;
                }
                MinecraftServer server = serverLevel.getServer();
                if (server == null) {
                    return;
                }

                if (is1055 && hasDay7) {
                    server.execute(() -> {
                        playSound(level, pos, "radiosound");
                        if (player instanceof ServerPlayer sPlayer) {
                            AdvancementHolder taskAdv = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("lostinfog", "radiotask"));
                            if (taskAdv != null) {
                                AdvancementProgress progress = sPlayer.getAdvancements().getOrStartProgress(taskAdv);
                                if (!progress.isDone()) {
                                    for (String criterion : progress.getRemainingCriteria()) {
                                        sPlayer.getAdvancements().award(taskAdv, criterion);
                                    }
                                }
                            }
                        }
                    });

                    for (int i = 0; i < 20; i++) {
                        if (!checkRadioAndStopSoundOnMainThread(server, player, animId, level, pos)) return;
                        String noise = NOISE_FRAMES[i % NOISE_FRAMES.length];
                        Component msg = Component.literal(ChatFormatting.DARK_GREEN + "[ " + noise + " ] " + ChatFormatting.GREEN + "SCANNING FM: " + ChatFormatting.WHITE + "105.5 MHz");
                        server.execute(() -> player.displayClientMessage(msg, true));
                        Thread.sleep(300);
                    }

                    for (int i = 0; i < 4; i++) {
                        if (!checkRadioAndStopSoundOnMainThread(server, player, animId, level, pos)) return;
                        Component msg = Component.literal(ChatFormatting.DARK_RED + "CONNECTION ESTABLISHED...");
                        server.execute(() -> player.displayClientMessage(msg, true));
                        Thread.sleep(800);
                    }

                    String outCoords = (shelterCoords != null) ? shelterCoords : (ChatFormatting.RED + "SIGNAL LOST");
                    Component finalMsg = Component.literal(ChatFormatting.GOLD + "<BROADCAST> " + ChatFormatting.GRAY + "Nearest shelter: " + outCoords);

                    for (int i = 0; i < 6; i++) {
                        if (!checkRadioAndStopSoundOnMainThread(server, player, animId, level, pos)) return;
                        server.execute(() -> player.displayClientMessage(finalMsg, true));
                        Thread.sleep(1500);
                    }
                } else {
                    for (int i = 0; i < 6; i++) {
                        if (!checkAnimId(player, animId)) return;
                        String noise = NOISE_FRAMES[i % NOISE_FRAMES.length];
                        Component msg = Component.literal(ChatFormatting.DARK_GRAY + "[ " + noise + " ] " + ChatFormatting.GRAY + "TUNING: " + freq + " MHz");
                        server.execute(() -> player.displayClientMessage(msg, true));
                        Thread.sleep(400);
                    }

                    Component finalMsg;
                    if (is1055 && !hasDay7) {
                        finalMsg = Component.literal(ChatFormatting.GRAY + "FM " + freq + " MHz " + ChatFormatting.DARK_GRAY + "[ ENCRYPTED SIGNAL... ]");
                    } else {
                        finalMsg = Component.literal(ChatFormatting.GRAY + "FM " + freq + " MHz " + ChatFormatting.DARK_GRAY + "[ STATIC NOISE ]");
                    }

                    for (int i = 0; i < 4; i++) {
                        if (!checkAnimId(player, animId)) return;
                        server.execute(() -> player.displayClientMessage(finalMsg, true));
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}