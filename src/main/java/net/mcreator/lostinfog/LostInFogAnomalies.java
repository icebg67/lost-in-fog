package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.advancements.AdvancementHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.Map;

public class LostInFogAnomalies {

    public static int getClientDay() {
        try {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return 1;
            java.lang.reflect.Field progressField = net.minecraft.client.multiplayer.ClientAdvancements.class.getDeclaredField("progress");
            progressField.setAccessible(true);
            Map<?, ?> progressMap = (Map<?, ?>) progressField.get(connection.getAdvancements());
            if (progressMap == null) return 1;
            int maxDay = 1;
            for (Map.Entry<?, ?> entry : progressMap.entrySet()) {
                if (entry.getKey() instanceof AdvancementHolder holder && entry.getValue() instanceof net.minecraft.advancements.AdvancementProgress progress) {
                    if (progress.isDone()) {
                        String id = holder.id().toString();
                        if (id.contains("day_8")) maxDay = Math.max(maxDay, 8);
                        else if (id.contains("day_7")) maxDay = Math.max(maxDay, 7);
                        else if (id.contains("day_6")) maxDay = Math.max(maxDay, 6);
                        else if (id.contains("day_5")) maxDay = Math.max(maxDay, 5);
                        else if (id.contains("day_4")) maxDay = Math.max(maxDay, 4);
                        else if (id.contains("day_3")) maxDay = Math.max(maxDay, 3);
                        else if (id.contains("day_2")) maxDay = Math.max(maxDay, 2);
                    }
                }
            }
            return maxDay;
        } catch (Exception e) {
            return 1;
        }
    }

    public static int getServerDay(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_8")) return 8;
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_7")) return 7;
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_6")) return 6;
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_5")) return 5;
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_4")) return 4;
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_3")) return 3;
            if (VideoMenu.ServerTracker.hasServerAdvancement(serverPlayer, "lostinfog:day_2")) return 2;
        }
        return 1;
    }

    public static int getMaxLevelDay(Level level) {
        int max = 1;
        for (Player player : level.players()) {
            max = Math.max(max, getServerDay(player));
        }
        return max;
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {
            if (Minecraft.getInstance().player == null) return;
            int day = getClientDay();
            if (day >= 2) {
                float far = 160.0F;
                if (day == 3) far = 120.0F;
                else if (day == 4) far = 20.0F;
                else if (day == 5 || day == 6) far = 90.0F;
                else if (day >= 7) far = 50.0F;
                
                event.setNearPlaneDistance(day == 4 ? 0.0F : 10.0F);
                event.setFarPlaneDistance(far);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onFogColor(ViewportEvent.ComputeFogColor event) {
            if (Minecraft.getInstance().player == null) return;
            int day = getClientDay();
            if (day >= 2) {
                float colorMod = 0.65F;
                if (day == 3) colorMod = 0.55F;
                else if (day == 4) colorMod = 0.20F;
                else if (day == 5 || day == 6) colorMod = 0.45F;
                else if (day >= 7) colorMod = 0.35F;
                
                event.setRed(colorMod);
                event.setGreen(colorMod);
                event.setBlue(colorMod + 0.05F);
            }
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME)
    public static class ServerEvents {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            Level level = player.level();
            if (level.isClientSide) return;
            int day = getServerDay(player);
            
            if (day >= 5 && level.getGameTime() % 20 == 0) {
                if (level.getGameTime() % 100 == 0) {
                    level.getEntities((Entity) null, player.getBoundingBox().inflate(64), e -> e instanceof Animal).forEach(Entity::discard);
                }
            }
            if (day >= 6 && level.getGameTime() % 20 == 0) {
                BlockPos pos = player.blockPosition();
                BlockPos.betweenClosed(pos.offset(-15, -3, -15), pos.offset(15, 5, 15)).forEach(checkPos -> {
                    BlockState state = level.getBlockState(checkPos);
                    if (state.getBlock() instanceof CropBlock) {
                        IntegerProperty ageProp = BlockStateProperties.AGE_7;
                        if (state.hasProperty(ageProp) && state.getValue(ageProp) > 0) level.setBlock(checkPos, state.setValue(ageProp, 0), 3);
                    }
                });
                BlockPos.betweenClosed(pos.offset(-16, -5, -16), pos.offset(16, 10, 16)).forEach(checkPos -> {
                    if (level.getBlockState(checkPos).is(Blocks.NETHER_PORTAL)) level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
                });
            }
        }

        @SubscribeEvent
        public static void onEntitySpawn(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Animal && event.getLevel().players().stream().anyMatch(p -> getServerDay(p) >= 5)) event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
            if (event.getLevel().players().stream().anyMatch(p -> getServerDay(p) >= 6)) {
                if (event.getState().getBlock() instanceof CropBlock || event.getState().getBlock() instanceof SaplingBlock) event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onBonemeal(BonemealEvent event) {
            if (event.getLevel().players().stream().anyMatch(p -> getServerDay(p) >= 6)) event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
            if (event.getLevel().players().stream().anyMatch(p -> getServerDay(p) >= 6)) event.setCanceled(true);
        }
    }
}