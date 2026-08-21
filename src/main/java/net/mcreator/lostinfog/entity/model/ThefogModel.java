package net.mcreator.lostinfog.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.mcreator.lostinfog.entity.ThefogEntity;

public class ThefogModel extends GeoModel<ThefogEntity> {
	@Override
	public ResourceLocation getAnimationResource(ThefogEntity entity) {
		return ResourceLocation.parse("lostinfog:animations/fogentity.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(ThefogEntity entity) {
		return ResourceLocation.parse("lostinfog:geo/fogentity.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(ThefogEntity entity) {
		return ResourceLocation.parse("lostinfog:textures/entities/" + entity.getTexture() + ".png");
	}

}
