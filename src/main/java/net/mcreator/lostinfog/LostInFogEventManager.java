package net.mcreator.lostinfog;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import net.mcreator.lostinfog.entity.ThefogEntity;
import net.mcreator.lostinfog.entity.WindowentityEntity;
import net.mcreator.lostinfog.init.LostinfogModEntities;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "lostinfog")
public class LostInFogEventManager {

    private static final Random RANDOM = new Random();

    private static final int MINOR_MIN_TICKS = 4000;
    private static final int MINOR_JITTER_TICKS = 4000;
    private static final int MAJOR_MIN_TICKS = 9000;
    private static final int MAJOR_JITTER_TICKS = 6000;
    private static final int WATCHER_MIN_TICKS = 9000;
    private static final int WATCHER_JITTER_TICKS = 6000;
    private static final int CORNER_MIN_TICKS = 9000;
    private static final int CORNER_JITTER_TICKS = 6000;

    private static final Map<UUID, Integer> minorTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> majorTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> watcherTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cornerTimer = new ConcurrentHashMap<>();

    private static final Map<UUID, Integer> pendingKnockMessage = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> ambientSilenceFadeTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> ambientIsSilent = new ConcurrentHashMap<>();

    private static final Map<UUID, GlitchTracker> glitchTrackers = new ConcurrentHashMap<>();
    private static final Map<UUID, DavidTrackingData> activeDavids = new ConcurrentHashMap<>();
    private static final Map<UUID, CornerData> activeCorners = new ConcurrentHashMap<>();
    private static final Map<UUID, TeleportEventData> activeTeleports = new ConcurrentHashMap<>();
    private static final Map<UUID, SicknessData> activeSickness = new ConcurrentHashMap<>();

    private static final Map<UUID, UUID> activeWatchers = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> watcherSoundTimers = new ConcurrentHashMap<>();
    private static final Map<UUID, WhoIsThatData> activeWhoIsThat = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cameraFreezeTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Float[]> cameraFreezeRot = new ConcurrentHashMap<>();

    private static final TagKey<Block> C_GLASS = BlockTags.create(ResourceLocation.parse("c:glass_blocks"));
    private static final TagKey<Block> C_PANES = BlockTags.create(ResourceLocation.parse("c:glass_panes"));
    private static final TagKey<Block> FORGE_GLASS = BlockTags.create(ResourceLocation.parse("forge:glass"));

    private static final String[] CORNER_TEXTS = {
        "I think I saw something?..",
        "Was someone just there?",
        "Did that shadow just move?",
        "I'm not alone here...",
        "Something is watching me...",
        "My eyes are playing tricks on me.",
        "Who is there?",
        "It disappeared so fast...",
        "I swear someone was standing right there.",
        "Just my imagination... right?",
        "What was that in the dark?"
    };

    private static final String[] SICKNESS_TEXTS = {
        "I don't feel so good...",
        "I can't breathe...",
        "Something is wrong with me...",
        "My chest... it hurts...",
        "I feel so sick..."
    };

    private static final String[] KNOCK_MESSAGES = {
        "That's not their voice",
        "The main thing to remember is that they can't be here",
        "Who is that, anyway??",
        "How do they know I'm here?",
        "Who are they?",
        "These voices sound very familiar."
    };

    private static final String[] TELEPORT_MESSAGES = {
        "Where am I?",
        "How did I get here?",
        "I think I'm lost..."
    };

    private static final String[] GLITCH_TEXTS = {
        "ERR_0x0000F5",
        "SYSTEM_CRITICAL_FAILURE",
        "LOST_IN_FOG_GLITCH",
        "YOU_CANNOT_RUN",
        "BEHIND_YOU",
        "666_666_666"
    };

    private enum MinorEvent { BLOCK_SOUND, AMBIENT_MIX, KNOCK, DOOR_ENV, KNOCKBACK, CAMERA_FREEZE, BEHIND_STEPS, FLICKER_LIGHTS }
    private enum MajorEvent { SICKNESS, TELEPORT, PIT, DAVID, GLITCH, WHOISTHAT, CAVE_CLOSING_IN }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("lostinfogevent")
            .then(Commands.literal("1").executes(context -> {
                triggerGlitch(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("2").executes(context -> {
                triggerDavid(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("3").executes(context -> {
                triggerPitEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("4").executes(context -> {
                triggerTeleportEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("5").executes(context -> {
                triggerSicknessEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("6").executes(context -> {
                doBlockSoundEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("7").executes(context -> {
                doAmbientMixEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("8").executes(context -> {
                doKnockEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("9").executes(context -> {
                doDoorEnvEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("10").executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                spawnWatcher((ServerLevel) player.level(), player);
                return 1;
            }))
            .then(Commands.literal("11").executes(context -> {
                spawnCornerEntity(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("12").executes(context -> {
                spawnWindowentity(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("13").executes(context -> {
                triggerWhoIsThatEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("14").executes(context -> {
                triggerCaveClosingInEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("15").executes(context -> {
                doKnockbackEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("16").executes(context -> {
                doCameraFreezeEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("17").executes(context -> {
                doBehindStepsEvent(context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("18").executes(context -> {
                doLightFlickerEvent(context.getSource().getPlayerOrException());
                return 1;
            })));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        UUID uuid = player.getUUID();

        if (level.isClientSide()) {
            tickAmbientSilenceFade(uuid);
            return;
        }

        if (cameraFreezeTimer.getOrDefault(uuid, 0) > 0) {
            int ticks = cameraFreezeTimer.get(uuid);
            Float[] rot = cameraFreezeRot.get(uuid);
            if (rot != null && player instanceof ServerPlayer sp) {
                sp.connection.teleport(sp.getX(), sp.getY(), sp.getZ(), rot[0], rot[1]);
            }
            if (ticks - 1 <= 0) {
                cameraFreezeTimer.remove(uuid);
                cameraFreezeRot.remove(uuid);
            } else {
                cameraFreezeTimer.put(uuid, ticks - 1);
            }
        }

        tickPendingKnockMessage(player, uuid);
        tickMinorEvents(player, uuid);

        if (level.dimension() == Level.OVERWORLD) {
            tickMajorEvents(player, uuid);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            tickWatcherSystem((ServerLevel) level, serverPlayer, uuid);
            tickCornerSystem(serverPlayer, uuid);
        }

        runGlitchLogic(player);
        runDavidLogic(player);
        runTeleportLogic(player);
        runSicknessLogic(player);
        runWhoIsThatLogic(player);
    }

    private static int randomMinorInterval() {
        return MINOR_MIN_TICKS + RANDOM.nextInt(MINOR_JITTER_TICKS + 1);
    }

    private static int randomMajorInterval() {
        return MAJOR_MIN_TICKS + RANDOM.nextInt(MAJOR_JITTER_TICKS + 1);
    }

    private static int randomWatcherInterval() {
        return WATCHER_MIN_TICKS + RANDOM.nextInt(WATCHER_JITTER_TICKS + 1);
    }

    private static int randomCornerInterval() {
        return CORNER_MIN_TICKS + RANDOM.nextInt(CORNER_JITTER_TICKS + 1);
    }

    private static void tickMinorEvents(Player player, UUID uuid) {
        int ticksLeft = minorTimer.computeIfAbsent(uuid, k -> randomMinorInterval());

        if (ticksLeft <= 0) {
            minorTimer.put(uuid, randomMinorInterval());
            MinorEvent[] options = MinorEvent.values();
            MinorEvent chosen = options[RANDOM.nextInt(options.length)];

            switch (chosen) {
                case BLOCK_SOUND -> {
                    if (player instanceof ServerPlayer serverPlayer) doBlockSoundEvent(serverPlayer);
                }
                case AMBIENT_MIX -> doAmbientMixEvent(player);
                case KNOCK -> doKnockEvent(player);
                case DOOR_ENV -> doDoorEnvEvent(player);
                case KNOCKBACK -> doKnockbackEvent(player);
                case CAMERA_FREEZE -> doCameraFreezeEvent(player);
                case BEHIND_STEPS -> doBehindStepsEvent(player);
                case FLICKER_LIGHTS -> doLightFlickerEvent(player);
            }
        } else {
            minorTimer.put(uuid, ticksLeft - 1);
        }
    }

    private static void tickMajorEvents(Player player, UUID uuid) {
        int ticksLeft = majorTimer.computeIfAbsent(uuid, k -> randomMajorInterval());

        if (VideoMenu.ServerTracker.isTvActiveNear(player)) {
            majorTimer.put(uuid, 2400);
            return;
        }

        if (ticksLeft <= 0) {
            majorTimer.put(uuid, randomMajorInterval());
            MajorEvent[] options = MajorEvent.values();
            MajorEvent chosen = options[RANDOM.nextInt(options.length)];

            switch (chosen) {
                case SICKNESS -> triggerSicknessEvent(player);
                case TELEPORT -> triggerTeleportEvent(player);
                case PIT -> triggerPitEvent(player);
                case DAVID -> triggerDavid(player);
                case GLITCH -> triggerGlitch(player);
                case WHOISTHAT -> triggerWhoIsThatEvent(player);
                case CAVE_CLOSING_IN -> triggerCaveClosingInEvent(player);
            }
        } else {
            majorTimer.put(uuid, ticksLeft - 1);
        }
    }

    private static void doKnockbackEvent(Player player) {
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        double xKnock = Math.cos(angle) * 0.8;
        double zKnock = Math.sin(angle) * 0.8;
        player.hurt(player.level().damageSources().generic(), 1.0F);
        player.setDeltaMovement(player.getDeltaMovement().add(xKnock, 0.4, zKnock));
        player.hurtMarked = true;
    }

    private static void doCameraFreezeEvent(Player player) {
        float newYRot = player.getYRot() + (RANDOM.nextFloat() * 180f - 90f);
        float newXRot = (RANDOM.nextFloat() * 180f) - 90f;
        cameraFreezeRot.put(player.getUUID(), new Float[]{newYRot, newXRot});
        cameraFreezeTimer.put(player.getUUID(), 40);
    }

    private static void doBehindStepsEvent(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 back = view.scale(-1.0);

        for (int i = 0; i < 5; i++) {
            int stepDelay = i * 8;
            double stepDist = 10.0 - (i * 2.0);
            Vec3 soundPos = player.position().add(back.scale(stepDist));

            serverPlayer.getServer().tell(new TickTask(serverPlayer.getServer().getTickCount() + stepDelay, () -> {
                player.level().playSound(null, soundPos.x, soundPos.y, soundPos.z, SoundEvents.STONE_STEP, SoundSource.PLAYERS, 1.0F, 0.8F);
            }));
        }
    }

    private static void doLightFlickerEvent(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.5F);
    }

    private static void doBlockSoundEvent(ServerPlayer player) {
        int count = 1 + RANDOM.nextInt(3);
        triggerBlockSoundChain(player, count, 0);
    }

    private static void triggerBlockSoundChain(ServerPlayer player, int remaining, int tickDelay) {
        if (remaining <= 0) return;

        player.getServer().tell(new TickTask(player.getServer().getTickCount() + tickDelay, () -> {
            BlockPos target = findValidNoiseBlock(player);
            if (target != null) {
                player.serverLevel().levelEvent(2001, target, Block.getId(player.level().getBlockState(target)));
            }
            triggerBlockSoundChain(player, remaining - 1, 20);
        }));
    }

    private static BlockPos findValidNoiseBlock(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = 5 + RANDOM.nextDouble() * 10;
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            int y = (int) (player.getY() + (RANDOM.nextInt(10) - 5));

            BlockPos pos = new BlockPos(x, y, z);

            if (level.isEmptyBlock(pos) || level.getBlockEntity(pos) != null) continue;
            if (isUnderPlayer(player, pos)) continue;
            if (isPlayerLookingAtBlock(player, pos)) continue;

            return pos;
        }
        return null;
    }

    private static boolean isUnderPlayer(ServerPlayer player, BlockPos pos) {
        return Math.abs(player.getX() - pos.getX()) < 5 &&
               Math.abs(player.getZ() - pos.getZ()) < 5 &&
               pos.getY() < player.getBlockY();
    }

    private static boolean isPlayerLookingAtBlock(ServerPlayer player, BlockPos pos) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toBlock = new Vec3(pos.getX() - player.getX(), pos.getY() - player.getY(), pos.getZ() - player.getZ()).normalize();
        return view.dot(toBlock) > 0.85;
    }

    private static void doAmbientMixEvent(Player player) {
        int choice = RANDOM.nextInt(100);

        if (choice < 25) {
            playAmbientSound(player, ResourceLocation.fromNamespaceAndPath("lostinfog", "ambient"));
        } else if (choice < 50) {
            playAmbientSound(player, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.AMBIENT_CAVE.value()));
        } else {
            triggerAmbientSilence(player);
        }
    }

    private static void playAmbientSound(Player player, ResourceLocation sound) {
        Level level = player.level();
        int range = 7 + RANDOM.nextInt(9);
        int x = (int) (player.getX() + (RANDOM.nextBoolean() ? range : -range));
        int y = (int) (player.getY() + RANDOM.nextInt(3) - 1);
        int z = (int) (player.getZ() + (RANDOM.nextBoolean() ? range : -range));

        level.playSound(null, new BlockPos(x, y, z), BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.AMBIENT, 1.0f, 1.0f);
    }

    private static void triggerAmbientSilence(Player player) {
        if (player.level().isClientSide()) {
            setClientVolumes(0.0, 0.0, 0.0, 0.0, 0.2);
            ambientIsSilent.put(player.getUUID(), true);
        }
    }

    private static void tickAmbientSilenceFade(UUID uuid) {
        if (!Boolean.TRUE.equals(ambientIsSilent.get(uuid))) return;

        int fadeTicks = ambientSilenceFadeTimer.getOrDefault(uuid, 0) + 1;
        if (fadeTicks >= 200) {
            setClientVolumes(1.0, 1.0, 1.0, 1.0, 1.0);
            ambientIsSilent.put(uuid, false);
            ambientSilenceFadeTimer.put(uuid, 0);
        } else {
            ambientSilenceFadeTimer.put(uuid, fadeTicks);
        }
    }

    private static void setClientVolumes(double ambient, double hostile, double neutral, double players, double master) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.getSoundSourceOptionInstance(SoundSource.AMBIENT).set(ambient);
        mc.options.getSoundSourceOptionInstance(SoundSource.HOSTILE).set(hostile);
        mc.options.getSoundSourceOptionInstance(SoundSource.NEUTRAL).set(neutral);
        mc.options.getSoundSourceOptionInstance(SoundSource.PLAYERS).set(players);
        mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(master);
        mc.options.save();
    }

    private static void tickPendingKnockMessage(Player player, UUID uuid) {
        if (!pendingKnockMessage.containsKey(uuid)) return;

        int ticks = pendingKnockMessage.get(uuid);
        if (ticks <= 0) {
            player.displayClientMessage(Component.literal(KNOCK_MESSAGES[RANDOM.nextInt(KNOCK_MESSAGES.length)]), true);
            pendingKnockMessage.remove(uuid);
        } else {
            pendingKnockMessage.put(uuid, ticks - 1);
        }
    }

    private static void doKnockEvent(Player player) {
        Level level = player.level();
        boolean checkGlass = RANDOM.nextBoolean();
        int radius = checkGlass ? 16 : 24;

        BlockPos playerPos = player.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos closestPos = null;
        double minDistSqr = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutablePos.setWithOffset(playerPos, x, y, z);
                    double distSqr = mutablePos.distSqr(playerPos);

                    if (distSqr <= 1.0) continue;

                    BlockState state = level.getBlockState(mutablePos);
                    boolean isValid = false;

                    if (checkGlass) {
                        if (state.is(C_GLASS) || state.is(C_PANES) || state.is(FORGE_GLASS)) isValid = true;
                    } else {
                        if (state.is(BlockTags.DOORS)) isValid = true;
                    }

                    if (isValid && distSqr < minDistSqr) {
                        minDistSqr = distSqr;
                        closestPos = mutablePos.immutable();
                    }
                }
            }
        }

        if (closestPos == null) return;

        if (checkGlass) {
            ResourceLocation soundRes = ResourceLocation.parse("lostinfog:stukokno");
            SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundRes);
            if (soundEvent != null) level.playSound(null, closestPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            ResourceLocation doorRes = ResourceLocation.parse("lostinfog:stukdoor");
            SoundEvent doorSound = BuiltInRegistries.SOUND_EVENT.get(doorRes);
            if (doorSound != null) level.playSound(null, closestPos, doorSound, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (RANDOM.nextFloat() < 0.3f) {
                ResourceLocation sarahRes = ResourceLocation.parse("lostinfog:sarah");
                SoundEvent sarahSound = BuiltInRegistries.SOUND_EVENT.get(sarahRes);
                if (sarahSound != null) {
                    Vec3 playerVec = player.position();
                    Vec3 doorVec = Vec3.atCenterOf(closestPos);
                    Vec3 direction = doorVec.subtract(playerVec).normalize();
                    Vec3 soundTarget = doorVec.add(direction.scale(5));

                    level.playSound(null, soundTarget.x, soundTarget.y, soundTarget.z, sarahSound, SoundSource.PLAYERS, 1.0F, 1.0F);
                    pendingKnockMessage.put(player.getUUID(), 120);
                }
            }
        }
    }

    private static void doDoorEnvEvent(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        int radius = 10;

        if (RANDOM.nextFloat() < 0.5F) {
            List<BlockPos> validBlocks = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);

                        if (state.getBlock() instanceof DoorBlock) {
                            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                validBlocks.add(pos);
                            }
                        } else if (state.getBlock() instanceof TrapDoorBlock) {
                            validBlocks.add(pos);
                        }
                    }
                }
            }

            if (validBlocks.isEmpty()) return;

            Collections.shuffle(validBlocks);
            BlockPos targetPos = validBlocks.get(0);
            BlockState state = level.getBlockState(targetPos);

            if (state.getBlock() instanceof DoorBlock) {
                boolean isOpen = state.getValue(DoorBlock.OPEN);
                level.setBlock(targetPos, state.setValue(DoorBlock.OPEN, !isOpen), 3);

                BlockPos upperPos = targetPos.above();
                BlockState upperState = level.getBlockState(upperPos);
                if (upperState.getBlock() instanceof DoorBlock) {
                    level.setBlock(upperPos, upperState.setValue(DoorBlock.OPEN, !isOpen), 3);
                }

                level.playSound(null, targetPos, isOpen ? SoundEvents.WOODEN_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else if (state.getBlock() instanceof TrapDoorBlock) {
                boolean isOpen = state.getValue(TrapDoorBlock.OPEN);
                level.setBlock(targetPos, state.setValue(TrapDoorBlock.OPEN, !isOpen), 3);

                level.playSound(null, targetPos, isOpen ? SoundEvents.WOODEN_TRAPDOOR_CLOSE : SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            List<BlockPos> validGlass = new ArrayList<>();
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 lookVec = player.getViewVector(1.0F);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();

                        if (path.contains("glass")) {
                            Vec3 targetVec = Vec3.atCenterOf(pos).subtract(eyePos).normalize();
                            double dot = lookVec.dot(targetVec);

                            if (dot < 0.4) {
                                validGlass.add(pos);
                            }
                        }
                    }
                }
            }

            if (validGlass.isEmpty()) return;

            Collections.shuffle(validGlass);
            BlockPos targetPos = validGlass.get(0);

            level.destroyBlock(targetPos, false);

            ItemEntity cobble = new ItemEntity(level, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, new ItemStack(Items.COBBLESTONE, 1));
            level.addFreshEntity(cobble);

            if (RANDOM.nextFloat() < 0.2F) {
                spawnWindowentity(player);
            }
        }
    }

    private static void spawnWindowentity(Player player) {
        Level level = player.level();
        WindowentityEntity entity = new WindowentityEntity(LostinfogModEntities.WINDOWENTITY.get(), level);
        Vec3 spawnPos = player.position().add(player.getViewVector(1.0F).scale(3.5D));
        entity.moveTo(spawnPos.x, player.getY(), spawnPos.z, 0, 0);
        level.addFreshEntity(entity);
    }

    private static void triggerSicknessEvent(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        activeSickness.put(player.getUUID(), new SicknessData());
    }

    private static void runSicknessLogic(Player player) {
        UUID uuid = player.getUUID();
        if (!activeSickness.containsKey(uuid)) return;
        SicknessData data = activeSickness.get(uuid);

        player.setAirSupply(-20);
        if (data.ticks % 20 == 0) {
            player.hurt(player.level().damageSources().drown(), 1.0F);
        }

        if (!data.soundPlayed) {
            BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse("lostinfog:khekhe")).ifPresent(s -> {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), s, SoundSource.PLAYERS, 1.0f, 1.0f);
                data.soundPlayed = true;
            });
        }

        if (data.ticks == 60) {
            player.displayClientMessage(Component.literal(SICKNESS_TEXTS[RANDOM.nextInt(SICKNESS_TEXTS.length)]).withStyle(ChatFormatting.WHITE), true);
        }

        data.ticks++;

        if (data.ticks >= 100) {
            player.setAirSupply(300);
            activeSickness.remove(uuid);
        }
    }

    private static void runTeleportLogic(Player player) {
        UUID uuid = player.getUUID();
        if (!activeTeleports.containsKey(uuid)) return;
        TeleportEventData data = activeTeleports.get(uuid);

        if (data.ticks == 0) {
            player.displayClientMessage(Component.literal(TELEPORT_MESSAGES[RANDOM.nextInt(TELEPORT_MESSAGES.length)]).withStyle(ChatFormatting.WHITE), true);
        }

        data.ticks++;

        if (data.ticks >= 1200) {
            player.teleportTo(data.startPos.x, data.startPos.y, data.startPos.z);
            player.displayClientMessage(Component.literal("Familiar place... Have I been here before?").withStyle(ChatFormatting.WHITE), true);
            activeTeleports.remove(uuid);
        }
    }

    private static void triggerTeleportEvent(Player player) {
        Vec3 startPos = player.position();
        double yaw = Math.toRadians(player.getYRot() + 90);
        double dx = Math.cos(yaw) * 200;
        double dz = Math.sin(yaw) * 200;
        player.teleportTo(player.getX() + dx, player.getY(), player.getZ() + dz);
        activeTeleports.put(player.getUUID(), new TeleportEventData(startPos));
    }

    private static void tickCornerSystem(ServerPlayer player, UUID uuid) {
        ResourceLocation day3 = ResourceLocation.fromNamespaceAndPath("lostinfog", "day_3");
        AdvancementHolder adv3 = player.getServer().getAdvancements().get(day3);
        if (adv3 != null && player.getAdvancements().getOrStartProgress(adv3).isDone()) return;

        CornerData data = activeCorners.get(uuid);

        if (data != null && data.isWaiting) {
            data.messageTicks--;
            if (data.messageTicks <= 0) {
                player.displayClientMessage(Component.literal(CORNER_TEXTS[RANDOM.nextInt(CORNER_TEXTS.length)]).withStyle(ChatFormatting.WHITE), true);
                activeCorners.remove(uuid);
            }
            return;
        }

        if (data == null) {
            int ticksLeft = cornerTimer.computeIfAbsent(uuid, k -> randomCornerInterval());
            if (ticksLeft <= 0) {
                cornerTimer.put(uuid, randomCornerInterval());
                spawnCornerEntity(player);
            } else {
                cornerTimer.put(uuid, ticksLeft - 1);
            }
        } else {
            Entity corner = ((ServerLevel) player.level()).getEntity(data.uuid);
            if (corner == null || !corner.isAlive()) {
                activeCorners.remove(uuid);
                return;
            }

            Vec3 eyePos = player.getEyePosition();
            Vec3 targetPos = corner.getEyePosition();
            Vec3 lookVec = player.getViewVector(1.0f);
            Vec3 dirToCorner = targetPos.subtract(eyePos).normalize();

            if (lookVec.dot(dirToCorner) > 0.95 && isVisible(player, corner)) {
                BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse("lostinfog:corner")).ifPresent(s ->
                    player.level().playSound(null, corner.getX(), corner.getY(), corner.getZ(), s, SoundSource.HOSTILE, 1.0f, 1.0f)
                );
                corner.discard();
                data.isWaiting = true;
                data.messageTicks = 100;
            }
        }
    }

    private static boolean isVisible(Player player, Entity target) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.getEyePosition();
        Level level = player.level();
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        Vec3 step = direction.normalize().scale(0.1);
        int steps = (int) (distance / 0.1);

        for (int i = 0; i < steps; i++) {
            Vec3 current = start.add(step.scale(i));
            BlockPos pos = BlockPos.containing(current);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !isTransparent(state)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTransparent(BlockState state) {
        return state.is(Blocks.GLASS) || state.is(Blocks.TINTED_GLASS) ||
               state.is(Blocks.OAK_LEAVES) || state.is(Blocks.BIRCH_LEAVES) ||
               state.is(Blocks.SPRUCE_LEAVES) || state.is(Blocks.JUNGLE_LEAVES) ||
               state.is(Blocks.ACACIA_LEAVES) || state.is(Blocks.DARK_OAK_LEAVES) ||
               state.is(Blocks.CHERRY_LEAVES) || state.is(Blocks.AZALEA_LEAVES) ||
               state.is(Blocks.FLOWERING_AZALEA_LEAVES) || state.is(Blocks.MANGROVE_LEAVES) ||
               state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) ||
               state.is(Blocks.LARGE_FERN);
    }

    private static void spawnCornerEntity(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < 50; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = 5 + RANDOM.nextDouble() * 7;
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            int y = (int) player.getY();

            Vec3 spawnPos = new Vec3(x + 0.5, y, z + 0.5);
            Vec3 toSpawn = spawnPos.subtract(player.getEyePosition()).normalize();
            if (player.getViewVector(1.0f).dot(toSpawn) > 0.5) continue;

            BlockPos p = new BlockPos(x, y, z);
            if (!level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir()) continue;
            if (!level.getBlockState(p.below()).isSolidRender(level, p.below())) continue;

            boolean n = !level.getBlockState(p.north()).isAir();
            boolean s = !level.getBlockState(p.south()).isAir();
            boolean e = !level.getBlockState(p.east()).isAir();
            boolean w = !level.getBlockState(p.west()).isAir();

            if ((n && e) || (n && w) || (s && e) || (s && w)) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("lostinfog:corner")).ifPresent(type -> {
                    Entity corner = type.create(level);
                    if (corner != null) {
                        corner.moveTo(x + 0.5, y, z + 0.5);
                        level.addFreshEntity(corner);
                        activeCorners.put(player.getUUID(), new CornerData(corner.getUUID()));
                    }
                });
                break;
            }
        }
    }

    private static void triggerWhoIsThatEvent(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        List<UUID> entityIds = new ArrayList<>();
        int count = 3 + RANDOM.nextInt(3);

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));

        BuiltInRegistries.SOUND_EVENT.getOptional(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.AMBIENT_CAVE.value())).ifPresent(s ->
            level.playSound(null, player.getX(), player.getY(), player.getZ(), s, SoundSource.AMBIENT, 1.0F, 1.0F)
        );

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 / count) * i + RANDOM.nextDouble() * 0.3;
            double dist = 3 + RANDOM.nextDouble() * 2;
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            double y = player.getY();

            BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("lostinfog:whoisthat")).ifPresent(type -> {
                    Entity entity = type.create(level);
                    if (entity != null) {
                        entity.moveTo(x + 0.5, y, z + 0.5, 0, 0);
                        if (entity instanceof Monster monster) monster.setNoAi(true);
                        level.addFreshEntity(entity);
                        entityIds.add(entity.getUUID());
                    }
                });
            }
        }

        activeWhoIsThat.put(player.getUUID(), new WhoIsThatData(entityIds));
    }

    private static void runWhoIsThatLogic(Player player) {
        UUID uuid = player.getUUID();
        WhoIsThatData data = activeWhoIsThat.get(uuid);
        if (data == null) return;

        ServerLevel level = (ServerLevel) player.level();
        data.ticks++;

        if (data.ticks >= 60 || data.entityIds.isEmpty()) {
            for (UUID entityId : data.entityIds) {
                Entity entity = level.getEntity(entityId);
                if (entity != null) entity.discard();
            }
            activeWhoIsThat.remove(uuid);
        }
    }

    private static void triggerCaveClosingInEvent(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        int[] distances = { 50, 38, 26, 14, 3 };
        scheduleCaveStep(serverPlayer, distances, 0);
    }

    private static void scheduleCaveStep(ServerPlayer player, int[] distances, int index) {
        if (index >= distances.length) return;

        int delayTicks = index == 0 ? 0 : 100;
        player.getServer().tell(new TickTask(player.getServer().getTickCount() + delayTicks, () -> {
            if (!player.isAlive()) return;
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = distances[index];
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;

            ResourceLocation soundRes = index == distances.length - 1
                ? ResourceLocation.parse("lostinfog:scream")
                : BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.AMBIENT_CAVE.value());

            BuiltInRegistries.SOUND_EVENT.getOptional(soundRes).ifPresent(s ->
                player.level().playSound(null, x, player.getY(), z, s, SoundSource.AMBIENT, 1.0F, 0.8F)
            );

            scheduleCaveStep(player, distances, index + 1);
        }));
    }

    private static void triggerGlitch(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        GlitchTracker tracker = glitchTrackers.computeIfAbsent(player.getUUID(), k -> new GlitchTracker());
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) { lightning.moveTo(player.getX(), player.getY(), player.getZ()); level.addFreshEntity(lightning); }
        BlockPos pPos = player.blockPosition();
        BlockPos[] directions = { pPos.east(), pPos.west(), pPos.south(), pPos.north() };
        List<BlockPos> validPositions = new ArrayList<>();
        for (BlockPos pos : directions) if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) validPositions.add(pos);
        if (validPositions.isEmpty()) return;
        BlockPos spawnPos = validPositions.get(RANDOM.nextInt(validPositions.size()));
        BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("lostinfog:glitch")).ifPresent(entityType -> {
            Entity glitch = entityType.create(level);
            if (glitch != null) {
                glitch.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                level.addFreshEntity(glitch);
                tracker.glitchUuid = glitch.getUUID();
                tracker.activeTimer = 40;
                BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse("lostinfog:scream")).ifPresent(s -> level.playSound(null, glitch, s, SoundSource.HOSTILE, 1.0F, 1.0F));
            }
        });
    }

    private static void triggerDavid(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        EntityType<?> davidType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("lostinfog", "david"));
        if (davidType != null) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            Entity david = davidType.spawn(serverLevel, BlockPos.containing(player.getX() + Math.cos(angle) * 12, player.getY(), player.getZ() + Math.sin(angle) * 12), MobSpawnType.COMMAND);
            if (david != null) activeDavids.put(david.getUUID(), new DavidTrackingData(david.getUUID(), player.getUUID()));
        }
    }

    private static void triggerPitEvent(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos playerPos = player.blockPosition().below();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = playerPos.offset(x, 0, z);
                level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
            }
        }
        BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse("lostinfog:event3")).ifPresent(sound ->
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F)
        );
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
    }

    private static void runGlitchLogic(Player player) {
        UUID uuid = player.getUUID();
        GlitchTracker tracker = glitchTrackers.computeIfAbsent(uuid, k -> new GlitchTracker());
        if (tracker.activeTimer > 0) {
            tracker.activeTimer--;
            Entity glitch = ((ServerLevel) player.level()).getEntity(tracker.glitchUuid);
            if (glitch != null && glitch.isAlive()) {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, glitch.getEyePosition());
                glitch.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
                for (int i = 0; i < 5; i++) player.sendSystemMessage(Component.literal(GLITCH_TEXTS[RANDOM.nextInt(GLITCH_TEXTS.length)]).withStyle(ChatFormatting.RED, ChatFormatting.OBFUSCATED));
            }
            if (tracker.activeTimer == 0) {
                if (glitch != null && glitch.isAlive()) glitch.discard();
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
        }
    }

    private static void runDavidLogic(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        UUID playerUuid = player.getUUID();
        Iterator<Map.Entry<UUID, DavidTrackingData>> iterator = activeDavids.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, DavidTrackingData> entry = iterator.next();
            DavidTrackingData data = entry.getValue();
            if (!data.playerId.equals(playerUuid)) continue;

            Entity david = serverLevel.getEntity(data.davidId);
            if (david == null || !david.isAlive() || !player.isAlive() || data.lifeTicks++ > 400 || david.position().distanceTo(player.position()) > 32.0) {
                if (david != null) david.discard();
                iterator.remove();
                continue;
            }
            Vec3 dir = player.position().subtract(david.position()).normalize();
            float yaw = (float) (Math.atan2(dir.z, dir.x) * (180 / Math.PI)) - 90;
            david.setYRot(yaw); david.setYHeadRot(yaw);
            if (!data.isAttacking && david.position().distanceTo(player.position()) <= 2.0) data.isAttacking = true;
            if (data.isAttacking) {
                if (data.attackTicks++ >= 1.5) { david.discard(); iterator.remove(); continue; }
                if (david.position().distanceTo(player.position()) <= 2.5 && data.attackTicks % 10 == 0) player.hurt(serverLevel.damageSources().generic(), 4.0F);
            }
            david.setDeltaMovement(dir.x * 0.38, david.getDeltaMovement().y, dir.z * 0.38);
            int fx = (int) Math.floor(david.getX() + dir.x * 0.8), fz = (int) Math.floor(david.getZ() + dir.z * 0.8), cy = (int) Math.floor(david.getY());
            for (int y = 0; y <= 2; y++) { BlockPos p = new BlockPos(fx, cy + y, fz); if (!serverLevel.getBlockState(p).isAir()) serverLevel.destroyBlock(p, true, david); }
            if ((serverLevel.getBlockState(new BlockPos(fx, cy - 1, fz)).isAir()) && player.getY() >= david.getY() - 1.0) serverLevel.setBlockAndUpdate(new BlockPos(fx, cy - 1, fz), Blocks.OAK_PLANKS.defaultBlockState());
        }
    }

    private static void tickWatcherSystem(ServerLevel level, ServerPlayer player, UUID uuid) {
        if (watcherSoundTimers.containsKey(uuid)) {
            int time = watcherSoundTimers.get(uuid);
            if (time <= 0) {
                Vec3 view = player.getViewVector(1.0f);
                Vec3 right = new Vec3(-view.z, 0, view.x).normalize();
                double sideOffset = (RANDOM.nextDouble() * 2.0 - 1.0);
                Vec3 pos = player.getEyePosition().subtract(view.scale(6.0)).add(right.scale(sideOffset));
                level.playSound(null, pos.x, pos.y, pos.z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("lostinfog:itsme")), SoundSource.NEUTRAL, 1.0f, 1.0f);
                watcherSoundTimers.remove(uuid);
            } else {
                watcherSoundTimers.put(uuid, time - 1);
            }
        }

        if (activeWatchers.containsKey(uuid)) {
            Entity watcher = level.getEntity(activeWatchers.get(uuid));
            if (watcher != null && watcher.isAlive()) {
                double dist = player.distanceTo(watcher);

                Vec3 eyePos = player.getEyePosition();
                Vec3 targetPos = watcher.getEyePosition();

                boolean hasLineOfSight = level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;

                if (hasLineOfSight && isLookingAtWatcher(player, watcher)) {
                    watcher.discard();
                    activeWatchers.remove(uuid);
                    watcherSoundTimers.put(uuid, 120);
                } else if (dist <= 16.0) {
                    watcher.discard();
                    activeWatchers.remove(uuid);
                }
            } else {
                activeWatchers.remove(uuid);
            }
            return;
        }

        int ticksLeft = watcherTimer.computeIfAbsent(uuid, k -> randomWatcherInterval());
        if (ticksLeft <= 0) {
            watcherTimer.put(uuid, randomWatcherInterval());
            spawnWatcher(level, player);
        } else {
            watcherTimer.put(uuid, ticksLeft - 1);
        }
    }

    private static void spawnWatcher(ServerLevel level, Player player) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("lostinfog:watcher"));
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        double dist = 32 + RANDOM.nextDouble() * 32;
        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos((int) x, 0, (int) z));
        Entity watcher = type.create(level);
        if (watcher != null) {
            watcher.moveTo(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(watcher);
            activeWatchers.put(player.getUUID(), watcher.getUUID());
        }
    }

    private static boolean isLookingAtWatcher(Player player, Entity target) {
        Vec3 view = player.getViewVector(1.0f).normalize();
        Vec3 toTarget = target.position().subtract(player.getEyePosition()).normalize();
        return view.dot(toTarget) > 0.97;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        boolean isGlass = event.getState().is(Blocks.GLASS) ||
                          event.getState().is(Blocks.GLASS_PANE) ||
                          event.getState().is(Blocks.TINTED_GLASS) ||
                          event.getState().is(Blocks.WHITE_STAINED_GLASS) ||
                          event.getState().is(Blocks.WHITE_STAINED_GLASS_PANE);

        if (isGlass) {
            if (event.getLevel() instanceof ServerLevel level) {
                event.getPlayer().hurt(level.damageSources().generic(), 1.0f);
                level.sendParticles(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f),
                        event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                        10, 0.2, 0.2, 0.2, 0.0);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof DoorBlock)) return;

        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        if (level.isClientSide) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        DoorLockManager manager = DoorLockManager.get(level);
        if (manager == null) return;

        boolean isLocked = manager.isLocked(pos);
        Direction face = event.getFace();
        String faceName = face != null ? face.getName() : "unknown";

        if (isLocked) {
            if (event.getEntity().isShiftKeyDown()) {
                String lockedFace = manager.getLockedFace(pos);
                if (faceName.equals(lockedFace)) {
                    manager.unlock(pos);
                    event.getEntity().displayClientMessage(Component.literal("Unlocked"), true);
                    level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.5F);
                } else {
                    event.getEntity().displayClientMessage(Component.literal("Cannot unlock from this side"), true);
                }
            } else {
                event.getEntity().displayClientMessage(Component.literal("Locked"), true);
                level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 2.0F);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else if (event.getEntity().isShiftKeyDown()) {
            if (state.getValue(BlockStateProperties.OPEN)) return;

            manager.lock(pos, faceName);
            event.getEntity().displayClientMessage(Component.literal("Locked"), true);
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.5F);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else {
            boolean isOpen = state.getValue(BlockStateProperties.OPEN);

            level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, !isOpen), 10);
            BlockPos upperPos = pos.above();
            BlockState upperState = level.getBlockState(upperPos);

            if (upperState.getBlock() == state.getBlock() && upperState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                level.setBlock(upperPos, upperState.setValue(BlockStateProperties.OPEN, !isOpen), 10);
            }

            level.playSound(null, pos, isOpen ? SoundEvents.WOODEN_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof ThefogEntity mob) {
            CompoundTag data = mob.getPersistentData();
            LivingEntity target = mob.getTarget();

            boolean isPlaying = data.getBoolean("IsPlaying");
            int cooldown = data.getInt("SoundCooldown");
            int dropTimer = data.getInt("AggroDropTimer");

            int soundLengthInTicks = 100;

            ResourceLocation soundLoc = ResourceLocation.fromNamespaceAndPath("lostinfog", "attack");

            if (target instanceof Player && target.isAlive()) {
                data.putInt("AggroDropTimer", 20);

                if (!isPlaying || cooldown <= 0) {
                    SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundLoc);
                    if (soundEvent != null) {
                        Holder<SoundEvent> soundHolder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent);
                        for (ServerPlayer p : mob.level().getEntitiesOfClass(ServerPlayer.class, mob.getBoundingBox().inflate(64.0))) {
                            p.connection.send(new ClientboundSoundEntityPacket(soundHolder, SoundSource.HOSTILE, mob, 1.0f, 1.0f, mob.getRandom().nextLong()));
                        }
                    }
                    data.putBoolean("IsPlaying", true);
                    data.putInt("SoundCooldown", soundLengthInTicks);
                } else {
                    data.putInt("SoundCooldown", cooldown - 1);
                }
            } else {
                if (isPlaying) {
                    if (dropTimer > 0) {
                        data.putInt("AggroDropTimer", dropTimer - 1);
                    } else {
                        for (ServerPlayer p : mob.level().getEntitiesOfClass(ServerPlayer.class, mob.getBoundingBox().inflate(64.0))) {
                            p.connection.send(new ClientboundStopSoundPacket(soundLoc, SoundSource.HOSTILE));
                        }
                        data.putBoolean("IsPlaying", false);
                        data.putInt("SoundCooldown", 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getSource().getEntity() instanceof ThefogEntity mob) {
                ResourceLocation soundLoc = ResourceLocation.fromNamespaceAndPath("lostinfog", "attack");
                for (ServerPlayer p : mob.level().getEntitiesOfClass(ServerPlayer.class, mob.getBoundingBox().inflate(64.0))) {
                    p.connection.send(new ClientboundStopSoundPacket(soundLoc, SoundSource.HOSTILE));
                }
                mob.discard();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        minorTimer.remove(uuid);
        majorTimer.remove(uuid);
        watcherTimer.remove(uuid);
        cornerTimer.remove(uuid);
        pendingKnockMessage.remove(uuid);
        ambientSilenceFadeTimer.remove(uuid);
        ambientIsSilent.remove(uuid);
        glitchTrackers.remove(uuid);
        activeDavids.entrySet().removeIf(e -> e.getValue().playerId.equals(uuid));
        activeCorners.remove(uuid);
        activeTeleports.remove(uuid);
        activeSickness.remove(uuid);
        activeWatchers.remove(uuid);
        watcherSoundTimers.remove(uuid);
        cameraFreezeTimer.remove(uuid);
        cameraFreezeRot.remove(uuid);

        WhoIsThatData whoIsThatData = activeWhoIsThat.remove(uuid);
        if (whoIsThatData != null && event.getEntity().level() instanceof ServerLevel level) {
            for (UUID entityId : whoIsThatData.entityIds) {
                Entity entity = level.getEntity(entityId);
                if (entity != null) entity.discard();
            }
        }
    }

    private static class GlitchTracker { int activeTimer = 0; UUID glitchUuid = null; }
    private static class DavidTrackingData {
        final UUID davidId, playerId;
        boolean isAttacking = false;
        int attackTicks = 0, lifeTicks = 0;
        DavidTrackingData(UUID davidId, UUID playerId) { this.davidId = davidId; this.playerId = playerId; }
    }
    private static class CornerData {
        final UUID uuid;
        boolean isWaiting = false;
        int messageTicks = 0;
        CornerData(UUID uuid) { this.uuid = uuid; }
    }
    private static class TeleportEventData {
        final Vec3 startPos;
        int ticks = 0;
        TeleportEventData(Vec3 startPos) { this.startPos = startPos; }
    }
    private static class SicknessData {
        int ticks = 0;
        boolean soundPlayed = false;
    }
    private static class WhoIsThatData {
        List<UUID> entityIds;
        int ticks = 0;
        WhoIsThatData(List<UUID> entityIds) { this.entityIds = entityIds; }
    }
}