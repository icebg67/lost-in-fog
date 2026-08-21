package net.mcreator.lostinfog.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME)
public class FlashlightNetworking {

    private static final Map<UUID, FlashlightSyncPayload> SERVER_STATES = new ConcurrentHashMap<>();

    public static void handle(FlashlightSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                if (context.player() instanceof ServerPlayer serverPlayer) {
                    FlashlightSyncPayload broadcastPayload = new FlashlightSyncPayload(
                            serverPlayer.getUUID(),
                            payload.isOn(),
                            payload.dist(),
                            payload.bright(),
                            payload.angle(),
                            payload.size()
                    );
                    SERVER_STATES.put(serverPlayer.getUUID(), broadcastPayload);
                    PacketDistributor.sendToPlayersTrackingEntity(serverPlayer, broadcastPayload);
                }
            } else if (context.flow().isClientbound()) {
                try {
                    Class<?> clazz = Class.forName("net.mcreator.lostinfog.Flashlight");
                    java.lang.reflect.Method method = clazz.getMethod("updatePlayerState", java.util.UUID.class, boolean.class, float.class, float.class, float.class, float.class);
                    method.invoke(null, payload.playerId(), payload.isOn(), payload.dist(), payload.bright(), payload.angle(), payload.size());
                } catch (Exception ignored) {
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FlashlightSyncPayload state = SERVER_STATES.get(player.getUUID());
            if (state != null && state.isOn()) {
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle().normalize();
                double maxDist = state.dist();

                AABB searchBox = player.getBoundingBox().inflate(maxDist);
                player.level().getEntities(player, searchBox, e -> {
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
                    return id != null && id.toString().equals("lostinfog:thefog");
                }).forEach(entity -> {
                    Vec3 entityPos = entity.getBoundingBox().getCenter();
                    Vec3 toEntity = entityPos.subtract(eyePos);
                    double distSq = toEntity.lengthSqr();

                    if (distSq <= maxDist * maxDist) {
                        Vec3 dirToEntity = toEntity.normalize();
                        double dot = lookVec.dot(dirToEntity);

                        double maxAngleRad = Math.toRadians(state.angle() * 25.0);
                        if (dot >= Math.cos(maxAngleRad)) {
                            ClipContext clip = new ClipContext(
                                    eyePos,
                                    entityPos,
                                    ClipContext.Block.COLLIDER,
                                    ClipContext.Fluid.NONE,
                                    player
                            );
                            if (player.level().clip(clip).getType() == HitResult.Type.MISS) {
                                entity.discard();
                            }
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player targetPlayer && event.getEntity() instanceof ServerPlayer tracker) {
            FlashlightSyncPayload state = SERVER_STATES.get(targetPlayer.getUUID());
            if (state != null) {
                PacketDistributor.sendToPlayer(tracker, state);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SERVER_STATES.remove(event.getEntity().getUUID());
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("lostinfog");
            registrar.playBidirectional(
                    FlashlightSyncPayload.TYPE,
                    FlashlightSyncPayload.STREAM_CODEC,
                    FlashlightNetworking::handle
            );
        }
    }
}