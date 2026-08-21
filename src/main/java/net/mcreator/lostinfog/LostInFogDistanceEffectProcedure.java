package net.mcreator.lostinfog;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

public class LostInFogDistanceEffectProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, net.minecraft.world.entity.Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player player) {
			boolean hasDay4 = false;
			boolean hasDay5 = false;
			if (player instanceof ServerPlayer serverPlayer) {
				var server = serverPlayer.server;
				AdvancementHolder adv4 = server.getAdvancements().get(ResourceLocation.parse("lostinfog:day_4"));
				if (adv4 != null && serverPlayer.getAdvancements().getOrStartProgress(adv4).isDone()) {
					hasDay4 = true;
				}
				AdvancementHolder adv5 = server.getAdvancements().get(ResourceLocation.parse("lostinfog:day_5"));
				if (adv5 != null && serverPlayer.getAdvancements().getOrStartProgress(adv5).isDone()) {
					hasDay5 = true;
				}
			}
			if (hasDay4 && !hasDay5) {
				boolean found = false;
				BlockPos center = BlockPos.containing(x, y, z);
				int radius = 100;
				int minX = center.getX() - radius;
				int minY = Math.max(world.getMinBuildHeight(), center.getY() - radius);
				int minZ = center.getZ() - radius;
				int maxX = center.getX() + radius;
				int maxY = Math.min(world.getMaxBuildHeight() - 1, center.getY() + radius);
				int maxZ = center.getZ() + radius;
				
				BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
				for (int px = minX; px <= maxX; px++) {
					for (int py = minY; py <= maxY; py++) {
						for (int pz = minZ; pz <= maxZ; pz++) {
							mutPos.set(px, py, pz);
							var state = world.getBlockState(mutPos);
							var id = state.getBlock().builtInRegistryHolder().key().location().toString();
							if (id.equals("lostinfog:tvon") || id.equals("lostinfog:tvoff")) {
								found = true;
								break;
							}
						}
						if (found) break;
					}
					if (found) break;
				}
				if (!found) {
					if (entity instanceof LivingEntity _entity)
						_entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, false));
				}
			}
		}
	}
}