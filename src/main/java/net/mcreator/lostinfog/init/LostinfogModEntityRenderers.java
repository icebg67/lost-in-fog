
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.lostinfog.client.renderer.WindowentityRenderer;
import net.mcreator.lostinfog.client.renderer.WhoisthatRenderer;
import net.mcreator.lostinfog.client.renderer.WatcherRenderer;
import net.mcreator.lostinfog.client.renderer.ThefogRenderer;
import net.mcreator.lostinfog.client.renderer.GlitchRenderer;
import net.mcreator.lostinfog.client.renderer.FriendRenderer;
import net.mcreator.lostinfog.client.renderer.DavidRenderer;
import net.mcreator.lostinfog.client.renderer.CornerRenderer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LostinfogModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(LostinfogModEntities.THEFOG.get(), ThefogRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.DAVID.get(), DavidRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.GLITCH.get(), GlitchRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.WATCHER.get(), WatcherRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.CORNER.get(), CornerRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.FRIEND.get(), FriendRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.WINDOWENTITY.get(), WindowentityRenderer::new);
		event.registerEntityRenderer(LostinfogModEntities.WHOISTHAT.get(), WhoisthatRenderer::new);
	}
}
