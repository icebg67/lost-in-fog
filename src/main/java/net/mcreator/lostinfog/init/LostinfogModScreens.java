
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.lostinfog.client.gui.ChestScreen;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LostinfogModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(LostinfogModMenus.CHEST.get(), ChestScreen::new);
	}
}
