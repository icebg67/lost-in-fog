package net.mcreator.lostinfog.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 5.1.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelunknown<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("lostinfog", "modelunknown"), "main");
	public final ModelPart rightleg;
	public final ModelPart leftleg;
	public final ModelPart body;
	public final ModelPart lefthand;
	public final ModelPart righthand;
	public final ModelPart head;
	public final ModelPart bone6;
	public final ModelPart bone;

	public Modelunknown(ModelPart root) {
		this.rightleg = root.getChild("rightleg");
		this.leftleg = root.getChild("leftleg");
		this.body = root.getChild("body");
		this.lefthand = root.getChild("lefthand");
		this.righthand = root.getChild("righthand");
		this.head = root.getChild("head");
		this.bone6 = root.getChild("bone6");
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition rightleg = partdefinition.addOrReplaceChild("rightleg", CubeListBuilder.create(), PartPose.offset(-3.0F, 17.5F, -23.0F));
		PartDefinition bone_r1 = rightleg.addOrReplaceChild("bone_r1", CubeListBuilder.create().texOffs(12, 26).addBox(-1.13F, -7.91F, -1.13F, 2.26F, 7.91F, 2.26F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1309F));
		PartDefinition bone_r2 = rightleg.addOrReplaceChild("bone_r2", CubeListBuilder.create().texOffs(14, 15).addBox(-1.13F, -9.04F, -1.13F, 2.26F, 9.04F, 2.26F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 7.345F, 1.13F, 0.1309F, 0.0F, 0.0F));
		PartDefinition leftleg = partdefinition.addOrReplaceChild("leftleg", CubeListBuilder.create(), PartPose.offset(3.78F, 17.5F, -23.0F));
		PartDefinition bone2_r1 = leftleg.addOrReplaceChild("bone2_r1", CubeListBuilder.create().texOffs(0, 28).addBox(-1.13F, -7.91F, -1.13F, 2.26F, 7.91F, 2.26F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));
		PartDefinition bone2_r2 = leftleg.addOrReplaceChild("bone2_r2", CubeListBuilder.create().texOffs(16, 0).addBox(-1.13F, -9.04F, -1.13F, 2.26F, 9.04F, 2.26F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 7.345F, 1.13F, 0.1309F, 0.0F, 0.0F));
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.39F, -14.69F, -1.0125F, 6.78F, 7.56F, 2.425F, new CubeDeformation(0.0F)).texOffs(0, 15)
				.addBox(-2.89F, -7.19F, -1.1625F, 5.78F, 4.56F, 2.325F, new CubeDeformation(0.0F)).texOffs(14, 2).addBox(-2.14F, -7.19F, -1.5375F, 4.28F, 4.06F, 1.7F, new CubeDeformation(0.0F)), PartPose.offset(0.39F, 12.415F, -23.0F));
		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.035F, -12.84F, -1.35F, -2.2287F, -1.2294F, 2.0238F));
		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.365F, -12.615F, -1.825F, -1.5708F, 0.0873F, 1.5708F));
		PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.035F, -9.79F, -2.5F, -1.6799F, 0.8029F, 1.5708F));
		PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(34, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.29F, -8.465F, -1.275F, -1.4303F, -0.4437F, 1.6173F));
		PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.19F, -9.64F, -0.75F, -1.5708F, -0.6981F, 1.5708F));
		PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.06F, -11.89F, -1.45F, -1.8413F, -0.6981F, 1.5708F));
		PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7",
				CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)).texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.19F, -10.39F, -0.75F, -1.5708F, -0.6981F, 1.5708F));
		PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.015F, -7.64F, -2.5F, -1.5708F, 1.3526F, 1.5708F));
		PartDefinition cube_r9 = body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.365F, -11.39F, -2.0F, -1.5708F, 0.0873F, 1.5708F));
		PartDefinition cube_r10 = body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.29F, -7.565F, -1.1F, -1.4303F, -0.4437F, 1.6173F));
		PartDefinition bone5_r1 = body.addOrReplaceChild("bone5_r1", CubeListBuilder.create().texOffs(30, 6).addBox(-2.25F, -25.775F, -2.1625F, 3.03F, 7.06F, 1.825F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.64F, 11.585F, 0.25F, 0.0F, 0.3054F, 0.0F));
		PartDefinition bone5_r2 = body.addOrReplaceChild("bone5_r2", CubeListBuilder.create().texOffs(28, 29).addBox(-2.25F, -25.775F, -2.1625F, 3.03F, 7.06F, 1.825F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.11F, 11.585F, 0.5F, 0.0F, -0.3491F, 0.0F));
		PartDefinition lefthand = partdefinition.addOrReplaceChild("lefthand", CubeListBuilder.create(), PartPose.offsetAndRotation(4.91F, -2.275F, -23.0F, 0.0F, 3.1416F, 0.0F));
		PartDefinition bone3_r1 = lefthand.addOrReplaceChild("bone3_r1", CubeListBuilder.create().texOffs(0, 21).addBox(-5.6872F, 0.261F, -1.356F, 4.085F, 5.65F, 2.7403F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.085F, 0.8475F, 0.0F, 0.0F, 0.0F, 0.1745F));
		PartDefinition righthand = partdefinition.addOrReplaceChild("righthand", CubeListBuilder.create(), PartPose.offset(-5.26F, 8.7425F, -23.0F));
		PartDefinition bone4_r1 = righthand.addOrReplaceChild("bone4_r1", CubeListBuilder.create().texOffs(20, 29).addBox(-1.13F, -2.26F, -1.13F, 2.26F, 7.91F, 2.26F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));
		PartDefinition bone4_r2 = righthand.addOrReplaceChild("bone4_r2", CubeListBuilder.create().texOffs(22, 22).addBox(-5.9325F, -1.13F, -1.2147F, 4.335F, 5.65F, 2.5425F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.4975F, -8.7575F, 0.0F, 0.0F, 0.0F, 0.1745F));
		PartDefinition bone4_r3 = righthand.addOrReplaceChild("bone4_r3", CubeListBuilder.create().texOffs(22, 11).addBox(-1.13F, -1.13F, -1.13F, 2.26F, 9.04F, 2.26F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.4125F, -9.605F, 0.0F, 0.0436F, 0.0F, 0.1745F));
		PartDefinition head = partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(24, 0).addBox(-2.26F, -4.52F, -2.26F, 2.77F, 2.02F, 4.52F, new CubeDeformation(0.0F)).texOffs(30, 14).addBox(-0.01F, -4.02F, -1.76F, 2.02F, 1.52F, 3.52F, new CubeDeformation(0.0F)).texOffs(0, 9)
						.addBox(-2.26F, -2.52F, -2.26F, 4.52F, 2.52F, 4.52F, new CubeDeformation(0.0F)).texOffs(30, 18).addBox(-1.13F, -0.565F, -1.13F, 2.26F, 2.26F, 2.26F, new CubeDeformation(0.0F)).texOffs(33, 15)
						.addBox(0.91F, -4.035F, 0.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)).texOffs(33, 15).addBox(1.51F, -4.035F, 0.775F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offset(0.39F, -2.84F, -23.0F));
		PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.06F, -2.385F, -0.75F, -1.5708F, -0.6981F, 1.5708F));
		PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.035F, -3.985F, 1.85F, 1.5708F, -0.3054F, -1.5708F));
		PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.06F, -3.11F, 1.0F, 0.0F, -1.5708F, 0.0F));
		PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(34, 14).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.265F, -3.56F, -0.5F, 0.0F, -1.5708F, 0.0F));
		PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(33, 15).addBox(-1.0F, -0.5F, -1.0F, 0.25F, 0.5F, 0.525F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.935F, -3.56F, 1.0F, 0.0F, -1.5708F, 0.0F));
		PartDefinition bone6 = partdefinition.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(0.39F, -1.145F, -23.0F));
		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-80.0F, 24.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		rightleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		leftleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		lefthand.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		righthand.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		bone6.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}
