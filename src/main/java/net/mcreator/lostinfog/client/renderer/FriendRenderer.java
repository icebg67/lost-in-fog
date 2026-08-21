
package net.mcreator.lostinfog.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;

import net.mcreator.lostinfog.entity.FriendEntity;

public class FriendRenderer extends HumanoidMobRenderer<FriendEntity, HumanoidModel<FriendEntity>> {
	public FriendRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<FriendEntity>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(FriendEntity entity) {
		return ResourceLocation.parse("lostinfog:textures/entities/stiv_orighinalnyi.png");
	}
}
