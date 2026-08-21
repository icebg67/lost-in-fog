
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

import net.mcreator.lostinfog.LostinfogMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class LostinfogModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LostinfogMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(LostinfogModItems.DAVID_SPAWN_EGG.get());
			tabData.accept(LostinfogModItems.GLITCH_SPAWN_EGG.get());
			tabData.accept(LostinfogModItems.WATCHER_SPAWN_EGG.get());
			tabData.accept(LostinfogModItems.CORNER_SPAWN_EGG.get());
			tabData.accept(LostinfogModItems.FRIEND_SPAWN_EGG.get());
			tabData.accept(LostinfogModItems.WINDOWENTITY_SPAWN_EGG.get());
			tabData.accept(LostinfogModItems.WHOISTHAT_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			tabData.accept(LostinfogModBlocks.PALKKA.get().asItem());
			tabData.accept(LostinfogModBlocks.PALKKA_3.get().asItem());
			tabData.accept(LostinfogModBlocks.PALKKA_4.get().asItem());
		}
	}
}
