package net.mcreator.lostinfog;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME)
public class EntityHider {
    private static final Random RANDOM = new Random();
    private static final Map<UUID, Integer> messageTimers = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;

        if (player.level().dimension() != ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("lostinfog", "the_fog_forest"))) return;

        if (level.getGameTime() % 20 != 0) return;

        UUID playerUUID = player.getUUID();

        if (messageTimers.containsKey(playerUUID)) {
            int timeLeft = messageTimers.get(playerUUID) - 1;
            if (timeLeft <= 0) {
                player.displayClientMessage(Component.literal("Where did he go? Why is he hiding, or from whom..."), true);
                messageTimers.remove(playerUUID);
            } else {
                messageTimers.put(playerUUID, timeLeft);
            }
        }

        Entity foundEntity = null;
        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(92))) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals("lostinfog:friend")) {
                foundEntity = entity;
                break;
            }
        }

        if (foundEntity != null) {
            double distSqr = foundEntity.distanceToSqr(player);
            if (distSqr > 8464) {
                foundEntity.discard();
            } else if (distSqr <= 256) {
                foundEntity.discard();
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                messageTimers.put(playerUUID, 6);
            }
        } else {
            if (level.getGameTime() - player.getPersistentData().getDouble("spawnCooldown") >= 2400) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("lostinfog", "friend"));
                if (type != null) {
                    Entity entity = type.create(level);
                    if (entity != null) {
                        double angle = RANDOM.nextDouble() * 2 * Math.PI;
                        double dist = 48;
                        double x = player.getX() + Math.cos(angle) * dist;
                        double z = player.getZ() + Math.sin(angle) * dist;
                        entity.setPos(x, player.getY(), z);
                        level.addFreshEntity(entity);
                        player.getPersistentData().putDouble("spawnCooldown", (double) level.getGameTime());
                    }
                }
            }
        }
    }
}