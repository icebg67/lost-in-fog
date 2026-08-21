package net.mcreator.lostinfog;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import io.netty.buffer.ByteBuf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class LostInFogClientTasks {

    public record SyncPacket(int day, int ticks, int c1, int c2, boolean comp, int pCount, boolean hudActive) implements CustomPacketPayload {
        public static final Type<SyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "sync"));
        public static final StreamCodec<ByteBuf, SyncPacket> STREAM_CODEC = StreamCodec.of(
                (buf, packet) -> {
                    buf.writeInt(packet.day);
                    buf.writeInt(packet.ticks);
                    buf.writeInt(packet.c1);
                    buf.writeInt(packet.c2);
                    buf.writeBoolean(packet.comp);
                    buf.writeInt(packet.pCount);
                    buf.writeBoolean(packet.hudActive);
                },
                buf -> new SyncPacket(
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readBoolean(),
                        buf.readInt(),
                        buf.readBoolean()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(SyncPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientData.day = packet.day;
                ClientData.ticks = packet.ticks;
                ClientData.c1 = packet.c1;
                ClientData.c2 = packet.c2;
                ClientData.comp = packet.comp;
                ClientData.pCount = packet.pCount;
                ClientData.hudActive = packet.hudActive;
            });
        }
    }

    public static class ClientData {
        public static int day = 1, ticks = 0, c1 = 0, c2 = 0, pCount = 1;
        public static boolean comp = false, hudActive = false;
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerNetwork(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("lostinfog");
            registrar.playToClient(SyncPacket.TYPE, SyncPacket.STREAM_CODEC, SyncPacket::handle);
        }
    }

    @EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT)
    public static class ClientOverlay {
        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (mc.player.level().dimension().location().equals(ResourceLocation.fromNamespaceAndPath("lostinfog", "the_fog_forest"))) return;
            if (!ClientData.hudActive || ClientData.day == 1) return;

            GuiGraphics g = event.getGuiGraphics();
            int y = g.guiHeight() - 15;
            boolean d = ClientData.comp;
            int pc = ClientData.pCount;

            Component t = switch (ClientData.day) {
                case 2 -> Component.literal((d ? "Wood " + (5 * pc) + "/" + (5 * pc) : "Wood " + ClientData.c1 + "/" + (5 * pc)) + " | " + (d ? "Stone " + (15 * pc) + "/" + (15 * pc) : "Stone " + ClientData.c2 + "/" + (15 * pc))).withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                case 3 -> Component.literal((d ? "Iron " + (5 * pc) + "/" + (5 * pc) : "Iron " + ClientData.c1 + "/" + (5 * pc)) + " | " + (d ? "Food " + (10 * pc) + "/" + (10 * pc) : "Food " + ClientData.c2 + "/" + (10 * pc))).withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                case 4 -> Component.literal(d ? "Survive the day (Completed)" : "Survive the day").withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                case 5 -> Component.literal(d ? "Blocks placed " + (10 * pc) + "/" + (10 * pc) : "Blocks placed " + ClientData.c1 + "/" + (10 * pc)).withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                case 6 -> Component.literal(d ? "Radio found (Completed)" : "Find the radio in the house").withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                case 7 -> Component.literal(d ? "Radio instructions (Completed)" : "Listen to the radio instructions").withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                case 8 -> Component.literal(d ? "Survive the Fog (Completed)" : "Survive the Fog").withStyle(d ? ChatFormatting.GREEN : ChatFormatting.WHITE);
                default -> Component.literal("Task Active").withStyle(ChatFormatting.WHITE);
            };
            g.drawString(mc.font, t, 5, y, 0xFFFFFF, true);
        }
    }
}