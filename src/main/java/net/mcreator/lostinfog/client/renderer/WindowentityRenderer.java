
package net.mcreator.lostinfog.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.mcreator.lostinfog.entity.WindowentityEntity;
import net.mcreator.lostinfog.client.model.Modelwindow;

public class WindowentityRenderer extends MobRenderer<WindowentityEntity, Modelwindow<WindowentityEntity>> {
	public WindowentityRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelwindow<WindowentityEntity>(context.bakeLayer(Modelwindow.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(WindowentityEntity entity) {
		return ResourceLocation.parse("lostinfog:textures/entities/window.png");
	}

	@Override
	protected boolean isShaking(WindowentityEntity entity) {
		return true;
	}
}
