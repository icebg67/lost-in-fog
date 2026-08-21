package net.mcreator.lostinfog;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;

@EventBusSubscriber(modid = "lostinfog")
public class EndingDimensionHandler {

    public static class EndingSavedData extends SavedData {
        public boolean bunkerGenerated = false;
        public int spawnX;
        public int spawnY;
        public int spawnZ;

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("bunkerGenerated", this.bunkerGenerated);
            tag.putInt("spawnX", this.spawnX);
            tag.putInt("spawnY", this.spawnY);
            tag.putInt("spawnZ", this.spawnZ);
            return tag;
        }

        public static EndingSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            EndingSavedData data = new EndingSavedData();
            data.bunkerGenerated = tag.getBoolean("bunkerGenerated");
            data.spawnX = tag.getInt("spawnX");
            data.spawnY = tag.getInt("spawnY");
            data.spawnZ = tag.getInt("spawnZ");
            return data;
        }
    }

    private static final ResourceKey<Level> ENDING_DIMENSION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("lostinfog", "ending"));

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getTo().equals(ENDING_DIMENSION) && event.getEntity() instanceof ServerPlayer triggerPlayer) {
            ServerLevel targetLevel = triggerPlayer.serverLevel();
            BlockPos triggerPos = triggerPlayer.blockPosition();

            EndingSavedData data = targetLevel.getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(EndingSavedData::new, EndingSavedData::load, DataFixTypes.LEVEL),
                    "lostinfog_ending_data"
            );

            if (!data.bunkerGenerated) {
                Optional<StructureTemplate> templateOpt = targetLevel.getStructureManager()
                        .get(ResourceLocation.fromNamespaceAndPath("lostinfog", "fogbunker"));

                if (templateOpt.isPresent()) {
                    StructureTemplate template = templateOpt.get();
                    BlockPos placePos = triggerPos.below();

                    template.placeInWorld(
                            targetLevel,
                            placePos,
                            placePos,
                            new StructurePlaceSettings(),
                            targetLevel.getRandom(),
                            3
                    );

                    BlockPos tpPos = placePos.offset(
                            template.getSize().getX() / 2,
                            1,
                            template.getSize().getZ() / 2
                    );

                    data.bunkerGenerated = true;
                    data.spawnX = tpPos.getX();
                    data.spawnY = tpPos.getY();
                    data.spawnZ = tpPos.getZ();
                    data.setDirty();
                    targetLevel.getDataStorage().save();

                    for (ServerPlayer player : targetLevel.getServer().getPlayerList().getPlayers()) {
                        player.teleportTo(
                                targetLevel,
                                data.spawnX + 0.5,
                                data.spawnY,
                                data.spawnZ + 0.5,
                                player.getYRot(),
                                player.getXRot()
                        );
                    }
                }
            } else {
                triggerPlayer.teleportTo(
                        targetLevel,
                        data.spawnX + 0.5,
                        data.spawnY,
                        data.spawnZ + 0.5,
                        triggerPlayer.getYRot(),
                        triggerPlayer.getXRot()
                );
            }
        }
    }
}