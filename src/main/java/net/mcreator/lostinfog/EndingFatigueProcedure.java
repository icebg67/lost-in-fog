package net.mcreator.lostinfog;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber
public class EndingFatigueProcedure {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity _entity) {
            if (_entity.level().dimension().equals(ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("lostinfog", "ending")))) {
                _entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 200, false, false));
            }
        }
    }
}