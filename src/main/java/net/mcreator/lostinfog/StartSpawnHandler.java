package net.mcreator.lostinfog;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.TickTask;
import net.minecraft.util.datafix.DataFixTypes;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = "lostinfog")
public class StartSpawnHandler {

    private static final ResourceKey<Level> FOG_DIM_KEY =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("lostinfog:the_fog_forest"));
    private static final ResourceLocation TENT_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath("lostinfog", "palatka");
    private static final BlockPos CAMP_ANCHOR = new BlockPos(0, 100, 0);

    private static final AtomicBoolean CAMP_GENERATING = new AtomicBoolean(false);

    public static class HouseSavedData extends SavedData {
        public boolean houseGenerated = false;
        public int spawnX;
        public int spawnY;
        public int spawnZ;

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("houseGenerated", this.houseGenerated);
            tag.putInt("spawnX", this.spawnX);
            tag.putInt("spawnY", this.spawnY);
            tag.putInt("spawnZ", this.spawnZ);
            return tag;
        }

        public static HouseSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            HouseSavedData data = new HouseSavedData();
            data.houseGenerated = tag.getBoolean("houseGenerated");
            data.spawnX = tag.getInt("spawnX");
            data.spawnY = tag.getInt("spawnY");
            data.spawnZ = tag.getInt("spawnZ");
            return data;
        }
    }

    public static class CampSavedData extends SavedData {
        public boolean campGenerated = false;
        public int spawnX;
        public int spawnY;
        public int spawnZ;

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("campGenerated", this.campGenerated);
            tag.putInt("spawnX", this.spawnX);
            tag.putInt("spawnY", this.spawnY);
            tag.putInt("spawnZ", this.spawnZ);
            return tag;
        }

        public static CampSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            CampSavedData data = new CampSavedData();
            data.campGenerated = tag.getBoolean("campGenerated");
            data.spawnX = tag.getInt("spawnX");
            data.spawnY = tag.getInt("spawnY");
            data.spawnZ = tag.getInt("spawnZ");
            return data;
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.dimension() != Level.OVERWORLD) {
            return;
        }

        handleHouseSpawn(serverLevel, player);
    }

    private static void handleHouseSpawn(ServerLevel serverLevel, Player player) {
        HouseSavedData data = serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(HouseSavedData::new, HouseSavedData::load, DataFixTypes.LEVEL),
                "lostinfog_house_data"
        );

        if (!data.houseGenerated) {
            Optional<StructureTemplate> templateOpt = serverLevel.getStructureManager()
                    .get(ResourceLocation.fromNamespaceAndPath("lostinfog", "house"));

            if (templateOpt.isEmpty()) return;

            StructureTemplate template = templateOpt.get();
            BlockPos targetArea = findPlainsPos(serverLevel, player.blockPosition());
            BlockPos basePos = findGround(serverLevel, targetArea);

            prepareAndBlendTerrain(serverLevel, basePos, template.getSize().getX(), template.getSize().getZ(), template.getSize().getY());

            template.placeInWorld(
                    serverLevel,
                    basePos,
                    basePos,
                    new StructurePlaceSettings(),
                    serverLevel.getRandom(),
                    3
            );

            BlockPos tp = basePos.offset(
                    template.getSize().getX() / 2,
                    1,
                    template.getSize().getZ() / 2
            );

            data.houseGenerated = true;
            data.spawnX = tp.getX();
            data.spawnY = tp.getY();
            data.spawnZ = tp.getZ();
            data.setDirty();
            serverLevel.getDataStorage().save();

            serverLevel.setDefaultSpawnPos(tp, 0.0F);
        }

        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.getBoolean("spawned_at_house")) {
            final BlockPos targetSpawn = new BlockPos(data.spawnX, data.spawnY, data.spawnZ);

            if (player instanceof ServerPlayer sp) {
                int targetTick = serverLevel.getServer().getTickCount() + 60;
                serverLevel.getServer().tell(new TickTask(targetTick, () -> {
                    if (sp.connection != null && !sp.isRemoved() && !sp.getPersistentData().getBoolean("lostinfog_in_fog")) {
                        sp.teleportTo(
                                serverLevel,
                                targetSpawn.getX() + 0.5,
                                targetSpawn.getY(),
                                targetSpawn.getZ() + 0.5,
                                sp.getYRot(),
                                sp.getXRot()
                        );
                        persistentData.putBoolean("spawned_at_house", true);
                    }
                }));
            }
        }
    }

    public static BlockPos ensureCampGenerated(ServerLevel fogLevel) {
        CampSavedData campData = fogLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CampSavedData::new, CampSavedData::load, DataFixTypes.LEVEL),
                "lostinfog_camp_data"
        );

        if (campData.campGenerated) {
            return new BlockPos(campData.spawnX, campData.spawnY, campData.spawnZ);
        }

        if (!CAMP_GENERATING.compareAndSet(false, true)) return null;

        try {
            if (campData.campGenerated) {
                return new BlockPos(campData.spawnX, campData.spawnY, campData.spawnZ);
            }

            Optional<StructureTemplate> templateOpt = fogLevel.getStructureManager().get(TENT_STRUCTURE);
            if (templateOpt.isEmpty()) return null;

            StructureTemplate template = templateOpt.get();
            BlockPos targetArea = findCampGround(fogLevel, CAMP_ANCHOR);
            BlockPos basePos = findGround(fogLevel, targetArea);

            prepareAndBlendTerrain(fogLevel, basePos, template.getSize().getX(), template.getSize().getZ(), template.getSize().getY());

            template.placeInWorld(
                    fogLevel,
                    basePos,
                    basePos,
                    new StructurePlaceSettings(),
                    fogLevel.getRandom(),
                    3
            );

            BlockPos tp = basePos.offset(
                    template.getSize().getX() / 2,
                    1,
                    template.getSize().getZ() / 2
            );

            campData.campGenerated = true;
            campData.spawnX = tp.getX();
            campData.spawnY = tp.getY();
            campData.spawnZ = tp.getZ();
            campData.setDirty();
            fogLevel.getDataStorage().save();

            return tp;
        } finally {
            CAMP_GENERATING.set(false);
        }
    }

    private static BlockPos findCampGround(ServerLevel level, BlockPos startPos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int radius = 200;
        for (int r = 0; r <= radius; r += 16) {
            for (int x = -r; x <= r; x += 16) {
                for (int z = -r; z <= r; z += 16) {
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        mutable.set(startPos.getX() + x, startPos.getY(), startPos.getZ() + z);
                        BlockPos groundCheck = findGround(level, mutable.immutable());
                        if (groundCheck.getY() > level.getMinBuildHeight() + 5) {
                            if (isSafeLocation(level, groundCheck, 16)) {
                                return mutable.immutable();
                            }
                        }
                    }
                }
            }
        }
        return startPos;
    }

    private static BlockPos findPlainsPos(ServerLevel level, BlockPos startPos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int radius = 400;
        for (int r = 0; r <= radius; r += 16) {
            for (int x = -r; x <= r; x += 16) {
                for (int z = -r; z <= r; z += 16) {
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        mutable.set(startPos.getX() + x, startPos.getY(), startPos.getZ() + z);
                        if (level.getBiome(mutable).is(Biomes.PLAINS)) {
                            BlockPos groundCheck = findGround(level, mutable.immutable());
                            if (groundCheck.getY() > level.getMinBuildHeight() + 10) {
                                if (isSafeLocation(level, groundCheck, 24)) {
                                    return mutable.immutable();
                                }
                            }
                        }
                    }
                }
            }
        }
        return startPos;
    }

    private static boolean isSafeLocation(ServerLevel level, BlockPos pos, int checkRadius) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int x = -checkRadius; x <= checkRadius; x += 6) {
            for (int z = -checkRadius; z <= checkRadius; z += 6) {
                int groundY = pos.getY();
                boolean waterOrLiquid = false;
                for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
                    checkPos.set(pos.getX() + x, y, pos.getZ() + z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.isSolid() || !level.getFluidState(checkPos).isEmpty()) {
                        if (!level.getFluidState(checkPos).isEmpty() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                            waterOrLiquid = true;
                        }
                        groundY = y;
                        break;
                    }
                }
                if (waterOrLiquid || Math.abs(groundY - pos.getY()) > 6) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockPos findGround(ServerLevel level, BlockPos pos) {
        for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
            BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(check);
            if (state.isSolid() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS) && level.getFluidState(check).isEmpty()) {
                return check.above();
            }
        }
        return pos;
    }

    private static void prepareAndBlendTerrain(ServerLevel level, BlockPos center, int width, int depth, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int margin = 10;
        int clearMargin = 4;

        for (int x = -margin; x <= width + margin; x++) {
            for (int z = -margin; z <= depth + margin; z++) {
                boolean inside = x >= 0 && x <= width && z >= 0 && z <= depth;
                boolean inClearZone = x >= -clearMargin && x <= width + clearMargin && z >= -clearMargin && z <= depth + clearMargin;

                int currentGround = center.getY();
                for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
                    pos.set(center.getX() + x, y, center.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isSolid() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
                        currentGround = y + 1;
                        break;
                    }
                }

                int targetY;
                if (inside) {
                    targetY = center.getY();
                } else {
                    int distX = 0;
                    if (x < 0) distX = -x;
                    else if (x > width) distX = x - width;

                    int distZ = 0;
                    if (z < 0) distZ = -z;
                    else if (z > depth) distZ = z - depth;

                    int dist = Math.max(distX, distZ);
                    double alpha = (double) dist / (double) (margin + 1);
                    targetY = (int) Math.round(center.getY() * (1.0 - alpha) + currentGround * alpha);
                }

                if (inside) {
                    for (int y = center.getY(); y <= center.getY() + height + 2; y++) {
                        pos.set(center.getX() + x, y, center.getZ() + z);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    for (int y = center.getY() - 1; y >= center.getY() - 5; y--) {
                        pos.set(center.getX() + x, y, center.getZ() + z);
                        if (!level.getBlockState(pos).isSolid()) {
                            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    }
                } else {
                    for (int y = level.getMaxBuildHeight() - 1; y >= targetY; y--) {
                        pos.set(center.getX() + x, y, center.getZ() + z);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }

                    if (inClearZone) {
                        for (int y = targetY; y <= targetY + 4; y++) {
                            pos.set(center.getX() + x, y, center.getZ() + z);
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }

                    pos.set(center.getX() + x, targetY - 1, center.getZ() + z);
                    level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);

                    for (int y = targetY - 2; y >= targetY - 6; y--) {
                        pos.set(center.getX() + x, y, center.getZ() + z);
                        if (!level.getBlockState(pos).isSolid()) {
                            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
