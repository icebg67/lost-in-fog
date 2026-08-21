package net.mcreator.lostinfog;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import net.mcreator.lostinfog.entity.WindowentityEntity;

@EventBusSubscriber
public class WindowentityBehaviorHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof WindowentityEntity entity) {
            Player player = entity.level().getNearestPlayer(entity, 128.0D);
            if (player != null) {
                double d0 = player.getX() - entity.getX();
                double d1 = player.getZ() - entity.getZ();
                float f = (float) (Math.atan2(d1, d0) * (180.0 / Math.PI)) - 90.0F;

                entity.setYRot(f);
                entity.setYBodyRot(f);
                entity.setYHeadRot(f);
                entity.yRotO = f;
                entity.yBodyRotO = f;
                entity.yHeadRotO = f;

                double d2 = player.getEyeY() - entity.getEyeY();
                double d3 = Math.sqrt(d0 * d0 + d1 * d1);
                float f1 = (float) (-(Math.atan2(d2, d3) * (180.0 / Math.PI)));

                entity.setXRot(f1);
                entity.xRotO = f1;

                if (!entity.level().isClientSide()) {
                    if (entity.distanceTo(player) <= 1.0D) {
                        Vec3 lookDir = player.getLookAngle().normalize();
                        Vec3 playerToEntity = entity.getEyePosition().subtract(player.getEyePosition()).normalize();
                        
                        if (lookDir.dot(playerToEntity) > 0.7D) {
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 1.0F, 1.0F);
                            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
                            entity.discard();
                        }
                    }
                }
            }
        }
    }
}