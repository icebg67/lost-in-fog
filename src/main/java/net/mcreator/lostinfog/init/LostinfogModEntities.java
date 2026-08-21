
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.Registries;

import net.mcreator.lostinfog.entity.WindowentityEntity;
import net.mcreator.lostinfog.entity.WhoisthatEntity;
import net.mcreator.lostinfog.entity.WatcherEntity;
import net.mcreator.lostinfog.entity.ThefogEntity;
import net.mcreator.lostinfog.entity.GlitchEntity;
import net.mcreator.lostinfog.entity.FriendEntity;
import net.mcreator.lostinfog.entity.DavidEntity;
import net.mcreator.lostinfog.entity.CornerEntity;
import net.mcreator.lostinfog.LostinfogMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class LostinfogModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, LostinfogMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<ThefogEntity>> THEFOG = register("thefog",
			EntityType.Builder.<ThefogEntity>of(ThefogEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<DavidEntity>> DAVID = register("david",
			EntityType.Builder.<DavidEntity>of(DavidEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<GlitchEntity>> GLITCH = register("glitch",
			EntityType.Builder.<GlitchEntity>of(GlitchEntity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<WatcherEntity>> WATCHER = register("watcher",
			EntityType.Builder.<WatcherEntity>of(WatcherEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<CornerEntity>> CORNER = register("corner",
			EntityType.Builder.<CornerEntity>of(CornerEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<FriendEntity>> FRIEND = register("friend",
			EntityType.Builder.<FriendEntity>of(FriendEntity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<WindowentityEntity>> WINDOWENTITY = register("windowentity",
			EntityType.Builder.<WindowentityEntity>of(WindowentityEntity::new, MobCategory.AXOLOTLS).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<WhoisthatEntity>> WHOISTHAT = register("whoisthat",
			EntityType.Builder.<WhoisthatEntity>of(WhoisthatEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 1.8f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(RegisterSpawnPlacementsEvent event) {
		ThefogEntity.init(event);
		DavidEntity.init(event);
		GlitchEntity.init(event);
		WatcherEntity.init(event);
		CornerEntity.init(event);
		FriendEntity.init(event);
		WindowentityEntity.init(event);
		WhoisthatEntity.init(event);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(THEFOG.get(), ThefogEntity.createAttributes().build());
		event.put(DAVID.get(), DavidEntity.createAttributes().build());
		event.put(GLITCH.get(), GlitchEntity.createAttributes().build());
		event.put(WATCHER.get(), WatcherEntity.createAttributes().build());
		event.put(CORNER.get(), CornerEntity.createAttributes().build());
		event.put(FRIEND.get(), FriendEntity.createAttributes().build());
		event.put(WINDOWENTITY.get(), WindowentityEntity.createAttributes().build());
		event.put(WHOISTHAT.get(), WhoisthatEntity.createAttributes().build());
	}
}
