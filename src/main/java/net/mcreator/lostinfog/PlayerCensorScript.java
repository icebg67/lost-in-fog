// package net.mcreator.lostinfog;
//
// import net.neoforged.bus.api.SubscribeEvent;
// import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
// import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
// import net.neoforged.api.distmarker.Dist;
// import net.neoforged.fml.common.EventBusSubscriber;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.client.Minecraft;
// import net.minecraft.client.multiplayer.ClientLevel;
// import net.minecraft.world.phys.HitResult;
// import net.minecraft.world.phys.Vec3;
// import net.minecraft.world.level.ClipContext;
// import com.mojang.blaze3d.vertex.PoseStack;
// import com.mojang.blaze3d.vertex.VertexFormat;
// import com.mojang.blaze3d.vertex.DefaultVertexFormat;
// import com.mojang.blaze3d.vertex.BufferBuilder;
// import com.mojang.blaze3d.vertex.Tesselator;
// import com.mojang.blaze3d.systems.RenderSystem;
// import net.minecraft.util.Mth;
// import org.joml.Matrix4f;
// import com.mojang.brigadier.arguments.BoolArgumentType;
// import net.minecraft.commands.Commands;
// import net.minecraft.network.chat.Component;
//
// @EventBusSubscriber(value = Dist.CLIENT)
// public class PlayerCensorScript {
//
//     private static boolean censorshipDisabled = false;
//
//     @SubscribeEvent
//     public static void onRegisterCommands(RegisterClientCommandsEvent event) {
//         event.getDispatcher().register(Commands.literal("disablecensorship")
//             .then(Commands.argument("value", BoolArgumentType.bool())
//                 .executes(context -> {
//                     censorshipDisabled = BoolArgumentType.getBool(context, "value");
//                     String status = censorshipDisabled ? "disabled" : "enabled";
//                     context.getSource().sendSuccess(() -> Component.literal("Censorship is now " + status), false);
//                     return 1;
//                 })
//             )
//         );
//     }
//
//     @SubscribeEvent
//     public static void onRenderWorld(RenderLevelStageEvent event) {
//         if (censorshipDisabled) return;
//         
//         if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
//
//         Minecraft mc = Minecraft.getInstance();
//         ClientLevel level = mc.level;
//         Player localPlayer = mc.player;
//         if (level == null || localPlayer == null) return;
//
//         double camX = mc.getEntityRenderDispatcher().camera.getPosition().x;
//         double camY = mc.getEntityRenderDispatcher().camera.getPosition().y;
//         double camZ = mc.getEntityRenderDispatcher().camera.getPosition().z;
//
//         float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
//
//         for (Player otherPlayer : level.players()) {
//             if (otherPlayer == localPlayer && mc.options.getCameraType().isFirstPerson()) continue;
//
//             double otherX = Mth.lerp(partialTick, otherPlayer.xOld, otherPlayer.getX());
//             double otherY = Mth.lerp(partialTick, otherPlayer.yOld, otherPlayer.getY());
//             double otherZ = Mth.lerp(partialTick, otherPlayer.zOld, otherPlayer.getZ());
//             
//             if (!isPlayerVisible(localPlayer, otherPlayer, level, partialTick)) continue;
//
//             PoseStack poseStack = event.getPoseStack();
//             poseStack.pushPose();
//
//             poseStack.translate(otherX - camX, otherY + (otherPlayer.getEyeHeight() * 1) - camY, otherZ - camZ);
//             poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
//             poseStack.translate(0, 0, 0.05);
//
//             drawCensorSquare(poseStack);
//
//             poseStack.popPose();
//         }
//     }
//
//     private static boolean isPlayerVisible(Player viewer, Player target, ClientLevel level, float partialTick) {
//         Vec3 viewerPos = viewer.getEyePosition(partialTick);
//         Vec3 targetPos = new Vec3(
//             Mth.lerp(partialTick, target.xOld, target.getX()),
//             Mth.lerp(partialTick, target.yOld, target.getY()) + target.getEyeHeight() * 0.5,
//             Mth.lerp(partialTick, target.zOld, target.getZ())
//         );
//         
//         if (viewerPos.distanceTo(targetPos) < 2.0) return true;
//         return level.clip(new ClipContext(viewerPos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer)).getType() == HitResult.Type.MISS;
//     }
//
//     private static void drawCensorSquare(PoseStack poseStack) {
//         RenderSystem.enableBlend();
//         RenderSystem.defaultBlendFunc();
//         RenderSystem.disableDepthTest(); 
//         RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
//         RenderSystem.disableCull();
//
//         Tesselator tesselator = Tesselator.getInstance();
//         BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//         Matrix4f matrix = poseStack.last().pose();
//
//         float size = 0.325f; 
//
//         bufferbuilder.addVertex(matrix, -size, -size, 0).setColor(0, 0, 0, 255);
//         bufferbuilder.addVertex(matrix, size, -size, 0).setColor(0, 0, 0, 255);
//         bufferbuilder.addVertex(matrix, size, size, 0).setColor(0, 0, 0, 255);
//         bufferbuilder.addVertex(matrix, -size, size, 0).setColor(0, 0, 0, 255);
//         
//         com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferbuilder.build());
//         
//         RenderSystem.enableDepthTest();
//         RenderSystem.enableCull();
//         RenderSystem.disableBlend();
//     }
// }