
package net.mcreator.lostinfog.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;

import net.mcreator.lostinfog.entity.DavidEntity;

public class DavidRenderer extends HumanoidMobRenderer<DavidEntity, HumanoidModel<DavidEntity>> {
	public DavidRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<DavidEntity>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(DavidEntity entity) {
		return ResourceLocation.parse("lostinfog:textures/entities/stiv_orighinalnyi.png");
	}

	@Override
	protected boolean isShaking(DavidEntity entity) {
		return true;
	}
}
